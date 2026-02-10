package vn.flinters.adagg.report.strategy;

import org.junit.jupiter.api.Test;
import vn.flinters.adagg.domain.CampaignTotals;
import vn.flinters.adagg.domain.ReportType;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class TopCpaReportStrategyTest {

    @Test
    void generate_returnsLowestCpa_andSkipsZeroConversions() {
        Map<String, CampaignTotals> totals = new HashMap<>();

        totals.put("A", totals(100, 10, 10.0, 5)); // cpa=2
        totals.put("B", totals(100, 10, 9.0, 3));  // cpa=3
        totals.put("Z", totals(100, 10, 999.0, 0)); // skipped

        TopCpaReportStrategy s = new TopCpaReportStrategy(Path.of("top10_cpa.csv"));
        var result = s.generate(totals);

        assertEquals(ReportType.TOP10_CPA, result.type());
        assertEquals(2, result.rows().size());

        assertEquals("A", result.rows().get(0).campaignId()); // lowest CPA first
        assertEquals("B", result.rows().get(1).campaignId());
    }

    @Test
    void generate_tieBreakByIdAscending() {
        Map<String, CampaignTotals> totals = new HashMap<>();
        totals.put("B", totals(100, 10, 10.0, 5)); // cpa=2
        totals.put("A", totals(100, 10, 10.0, 5)); // cpa=2

        TopCpaReportStrategy s = new TopCpaReportStrategy(Path.of("top10_cpa.csv"));
        var result = s.generate(totals);

        assertEquals("A", result.rows().get(0).campaignId());
        assertEquals("B", result.rows().get(1).campaignId());
    }

    private static CampaignTotals totals(long imp, long clicks, double spend, long conv) {
        CampaignTotals t = new CampaignTotals();
        t.impressions = imp;
        t.clicks = clicks;
        t.spend = spend;
        t.conversions = conv;
        return t;
    }
}
