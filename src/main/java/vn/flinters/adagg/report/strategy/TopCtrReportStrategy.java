package vn.flinters.adagg.report.strategy;

import vn.flinters.adagg.domain.CampaignTotals;
import vn.flinters.adagg.domain.ReportRow;
import vn.flinters.adagg.domain.ReportType;
import vn.flinters.adagg.report.ReportResult;
import vn.flinters.adagg.report.base.ReportStrategy;
import vn.flinters.adagg.util.Metrics;
import vn.flinters.adagg.util.TopK;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class TopCtrReportStrategy implements ReportStrategy {

    private final Path outputFile;

    public TopCtrReportStrategy(Path outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public ReportType type() {
        return ReportType.TOP10_CTR;
    }

    @Override
    public ReportResult generate(Map<String, CampaignTotals> totals) {
        TopK<Candidate> top = TopK.maxK(10, Candidate::compareByCtrThenId);

        for (var e : totals.entrySet()) {
            String id = e.getKey();
            CampaignTotals t = e.getValue();

            double ctr = Metrics.ctr(t.clicks, t.impressions);
            Double cpa = Metrics.cpaOrNull(t.spend, t.conversions);

            top.offer(new Candidate(id, t, ctr, cpa));
        }

        List<Candidate> candidates = top.toSortedListDescending();

        // enforce CTR desc + id asc on ties
        candidates.sort((a, b) -> {
            int c = Double.compare(b.ctr, a.ctr);
            if (c != 0) return c;
            return a.id.compareTo(b.id);
        });

        List<ReportRow> rows = candidates.stream()
                .map(Candidate::toReportRow)
                .toList();

        return new ReportResult(type(), outputFile, rows);
    }

    private record Candidate(String id, CampaignTotals t, double ctr, Double cpa) {
        ReportRow toReportRow() {
            return new ReportRow(id, t.impressions, t.clicks, t.spend, t.conversions, ctr, cpa);
        }

        static int compareByCtrThenId(Candidate a, Candidate b) {
            int c = Double.compare(a.ctr, b.ctr);
            if (c != 0) return c;
            // deterministic: campaign_id ascending
            return a.id.compareTo(b.id);
        }
    }
}
