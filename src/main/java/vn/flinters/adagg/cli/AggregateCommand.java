package vn.flinters.adagg.cli;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import vn.flinters.adagg.domain.ReportType;

import vn.flinters.adagg.report.base.ReportProcessor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "ad-aggregator",
        mixinStandardHelpOptions = true,
        description = "Aggregate ad_data.csv and generate reports."
)
public final class AggregateCommand implements Callable<Integer> {

    private final ReportProcessor processor;
    private static final Logger log = LoggerFactory.getLogger(AggregateCommand.class);

    @Inject
    public AggregateCommand(ReportProcessor processor) {
        this.processor = processor;
    }

    @Option(names = {"--input"}, required = true, description = "Input CSV file path")
    private Path input;

    @Option(names = {"--output"}, required = true, description = "Output directory")
    private Path outputDir;

    @Option(
            names = {"--reports"},
            description = "Comma-separated report types. Supported: ${COMPLETION-CANDIDATES}. Default: all",
            split = ","
    )
    private ReportType[] reports;

    @Override
    public Integer call() throws Exception {
        List<ReportType> selected = (reports == null || reports.length == 0)
                ? List.of(ReportType.values())
                : Arrays.asList(reports);
        log.info("CLI args: input={}, outputDir={}, reports={}", input, outputDir, selected);

        processor.process(input, outputDir, selected);
        return 0;
    }
}
