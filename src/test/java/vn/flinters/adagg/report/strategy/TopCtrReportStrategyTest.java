package vn.flinters.adagg.report.strategy;

import org.junit.jupiter.api.Test;
import vn.flinters.adagg.domain.CampaignTotals;
import vn.flinters.adagg.domain.ReportType;
import vn.flinters.adagg.report.ReportResult;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class TopCtrReportStrategyTest {

    @Test
    void generate_returnsTopByCtr_thenIdAscendingForTies() {
        Map<String, CampaignTotals> totals = new HashMap<>();

        // A and B tie on CTR = 10/100 = 0.1 -> expect A before B (id asc)
        totals.put("B", totals(100, 10, 1.0, 1));
        totals.put("A", totals(100, 10, 2.0, 1));

        // C has CTR = 50/100 = 0.5 -> must be first
        totals.put("C", totals(100, 50, 5.0, 1));

        ReportResult result = new TopCtrReportStrategy(Path.of("top10_ctr.csv")).generate(totals);

        assertEquals(ReportType.TOP10_CTR, result.type());
        assertEquals(3, result.rows().size());

        assertEquals("C", result.rows().get(0).campaignId()); // highest CTR first
        assertEquals("A", result.rows().get(1).campaignId()); // tie -> id asc
        assertEquals("B", result.rows().get(2).campaignId());
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