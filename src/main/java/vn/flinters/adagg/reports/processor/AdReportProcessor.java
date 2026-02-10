package vn.flinters.adagg.reports.processor;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.flinters.adagg.domain.CampaignTotals;
import vn.flinters.adagg.domain.MutableAdRow;
import vn.flinters.adagg.domain.ReportRow;
import vn.flinters.adagg.domain.ReportType;
import vn.flinters.adagg.io.base.CsvRowReader;
import vn.flinters.adagg.io.base.CsvWriter;
import vn.flinters.adagg.io.base.RowMapper;
import vn.flinters.adagg.reports.ReportResult;
import vn.flinters.adagg.reports.base.Aggregator;
import vn.flinters.adagg.reports.base.ReportProcessor;
import vn.flinters.adagg.reports.base.ReportStrategy;
import vn.flinters.adagg.reports.strategy.ReportStrategyFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public final class AdReportProcessor implements ReportProcessor {

    private static final Logger log = LoggerFactory.getLogger(AdReportProcessor.class);

    private final RowMapper<String[], MutableAdRow> mapper;
    private final Aggregator<MutableAdRow> aggregator;
    private final CsvWriter<ReportRow> writer;
    private final ReportStrategyFactory strategyFactory;
    private final CsvRowReader rowReader;

    @Inject
    public AdReportProcessor(
            RowMapper<String[], MutableAdRow> mapper,
            Aggregator<MutableAdRow> aggregator,
            CsvWriter<ReportRow> writer,
            ReportStrategyFactory strategyFactory,
            CsvRowReader rowReader
    ) {
        this.mapper = mapper;
        this.aggregator = aggregator;
        this.writer = writer;
        this.strategyFactory = strategyFactory;
        this.rowReader = rowReader;
    }

    @Override
    public void process(Path input, Path outputDir, List<ReportType> types)
            throws IOException, InterruptedException {

        long t0 = System.nanoTime();
        logStart(input, outputDir, types);

        ReadAndAggregateResult readResult = readAndAggregate(input);

        List<ReportStrategy> strategies = createStrategies(types, outputDir);
        if (strategies.isEmpty()) {
            log.warn("No report strategies created for reports={}. Nothing to do.", types);
            return;
        }

        ExecutorService pool = createPool(strategies.size());

        try {
            List<ReportResult> results = generateReports(strategies, readResult.totals(), pool);
            writeReports(results);
        } catch (CompletionException e) {
            Throwable cause = (e.getCause() != null) ? e.getCause() : e;
            throw new RuntimeException("Report generation failed", cause);
        } finally {
            shutdownPool(pool);
        }

        logFinish(t0, readResult.readNanos());
    }

    private void logStart(Path input, Path outputDir, List<ReportType> types) {
        log.info("Process started: input={}, outputDir={}, reports={}", input, outputDir, types);
    }

    private void logFinish(long t0, long readNanos) {
        long totalNanos = System.nanoTime() - t0;
        log.info("Process finished: readMs={}, totalMs={}", nanosToMs(readNanos), nanosToMs(totalNanos));
    }

    private ReadAndAggregateResult readAndAggregate(Path input) throws IOException {
        long readStart = System.nanoTime();
        ReadStats stats = readCsv(input);
        Map<String, CampaignTotals> totals = aggregator.snapshot();
        long readEnd = System.nanoTime();

        long readNanos = readEnd - readStart;
        log.info(
                "CSV read completed: rows={}, badRows={}, campaigns={}, tookMs={}",
                stats.rows.sum(), stats.badRows.sum(), totals.size(), nanosToMs(readNanos)
        );

        return new ReadAndAggregateResult(totals, readNanos);
    }

    private List<ReportStrategy> createStrategies(List<ReportType> types, Path outputDir) {
        return strategyFactory.create(types, outputDir);
    }

    private ExecutorService createPool(int strategyCount) {
        int n = Math.max(1, strategyCount);
        return Executors.newFixedThreadPool(n, r -> {
            Thread t = new Thread(r);
            t.setName("report-gen-" + t.getId());
            t.setDaemon(false);
            return t;
        });
    }

    private List<ReportResult> generateReports(
            List<ReportStrategy> strategies,
            Map<String, CampaignTotals> totals,
            ExecutorService pool
    ) {
        long reportStart = System.nanoTime();

        List<CompletableFuture<ReportResult>> futures = strategies.stream()
                .map(s -> CompletableFuture.supplyAsync(() -> generateOne(s, totals), pool))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<ReportResult> results = futures.stream().map(CompletableFuture::join).toList();

        log.info("All reports generated: count={}, tookMs={}", results.size(), nanosToMs(System.nanoTime() - reportStart));
        return results;
    }

    private ReportResult generateOne(ReportStrategy s, Map<String, CampaignTotals> totals) {
        long s0 = System.nanoTime();
        try {
            ReportResult rr = s.generate(totals);
            log.info("Report generation finished: type={}, rows={}, tookMs={}",
                    rr.type(), rr.rows().size(), nanosToMs(System.nanoTime() - s0));
            return rr;
        } catch (RuntimeException e) {
            log.error("Report generation failed", e);
            throw e;
        }
    }

    private void writeReports(List<ReportResult> results) throws IOException {
        for (ReportResult r : results) {
            long w0 = System.nanoTime();
            writer.write(r.outputFile(), r.rows());
            log.info(
                    "Report written: type={}, file={}, rows={}, tookMs={}",
                    r.type(), r.outputFile(), r.rows().size(), nanosToMs(System.nanoTime() - w0)
            );
        }
    }

    private void shutdownPool(ExecutorService pool) throws InterruptedException {
        pool.shutdown();
        if (!pool.awaitTermination(1, TimeUnit.MINUTES)) {
            log.warn("Report pool did not terminate in time; forcing shutdownNow()");
            pool.shutdownNow();
        }
    }

    private ReadStats readCsv(Path input) throws IOException {
        MutableAdRow buf = new MutableAdRow();
        ReadStats stats = new ReadStats();
        LongAdder rowNum = new LongAdder(); // 1-based data row number (header skipped)

        try (BufferedReader br = Files.newBufferedReader(input)) {
            rowReader.readAll(br, cols -> {
                stats.rows.increment();
                rowNum.increment();
                long n = rowNum.sum();

                try {
                    mapper.mapInto(cols, buf);
                    aggregator.accept(buf);
                } catch (RuntimeException ex) {
                    stats.badRows.increment();
                }
            });
        }

        return stats;
    }

    private static long nanosToMs(long nanos) {
        return TimeUnit.NANOSECONDS.toMillis(nanos);
    }

    private static final class ReadStats {
        final LongAdder rows = new LongAdder();
        final LongAdder badRows = new LongAdder();
    }

    private record ReadAndAggregateResult(Map<String, CampaignTotals> totals, long readNanos) {}
}
