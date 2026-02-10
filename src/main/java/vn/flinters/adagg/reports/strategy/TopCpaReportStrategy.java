package vn.flinters.adagg.reports.strategy;

import vn.flinters.adagg.domain.CampaignTotals;
import vn.flinters.adagg.domain.ReportRow;
import vn.flinters.adagg.domain.ReportType;
import vn.flinters.adagg.reports.ReportResult;
import vn.flinters.adagg.reports.base.ReportStrategy;
import vn.flinters.adagg.util.Metrics;
import vn.flinters.adagg.util.TopK;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class TopCpaReportStrategy implements ReportStrategy {

    private final Path outputFile;

    public TopCpaReportStrategy(Path outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public ReportType type() {
        return ReportType.TOP10_CPA;
    }

    @Override
    public ReportResult generate(Map<String, CampaignTotals> totals) {
        // We want LOWEST CPA => keep a TopK of "best (lowest)" => use minK
        TopK<Candidate> top = TopK.minK(10, Candidate::compareByCpaThenId);

        for (var e : totals.entrySet()) {
            String id = e.getKey();
            CampaignTotals t = e.getValue();

            if (t.conversions <= 0) continue; // required

            double ctr = Metrics.ctr(t.clicks, t.impressions);
            double cpa = Metrics.cpa(t.spend, t.conversions);
            top.offer(new Candidate(id, t, ctr, cpa));
        }

        List<ReportRow> rows = top.toSortedListAscending().stream()
                .map(Candidate::toReportRow)
                .toList();

        return new ReportResult(type(), outputFile, rows);
    }

    private record Candidate(String id, CampaignTotals t, double ctr, double cpa) {
        ReportRow toReportRow() {
            return new ReportRow(id, t.impressions, t.clicks, t.spend, t.conversions, ctr, cpa);
        }

        static int compareByCpaThenId(Candidate a, Candidate b) {
            int c = Double.compare(a.cpa, b.cpa);
            if (c != 0) return c;
            return a.id.compareTo(b.id);
        }
    }
}
