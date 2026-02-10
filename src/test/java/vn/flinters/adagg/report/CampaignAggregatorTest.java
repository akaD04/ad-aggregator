package vn.flinters.adagg.report;

import org.junit.jupiter.api.Test;
import vn.flinters.adagg.domain.CampaignTotals;
import vn.flinters.adagg.domain.MutableAdRow;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class CampaignAggregatorTest {

    @Test
    void accept_aggregatesByCampaignId() {
        CampaignAggregator agg = new CampaignAggregator();

        MutableAdRow r1 = new MutableAdRow();
        r1.campaignId = "A";
        r1.impressions = 10;
        r1.clicks = 2;
        r1.spend = 1.5;
        r1.conversions = 1;

        MutableAdRow r2 = new MutableAdRow();
        r2.campaignId = "A";
        r2.impressions = 5;
        r2.clicks = 1;
        r2.spend = 2.0;
        r2.conversions = 0;

        MutableAdRow r3 = new MutableAdRow();
        r3.campaignId = "B";
        r3.impressions = 7;
        r3.clicks = 0;
        r3.spend = 0.5;
        r3.conversions = 0;

        agg.accept(r1);
        agg.accept(r2);
        agg.accept(r3);

        Map<String, CampaignTotals> snap = agg.snapshot();
        assertEquals(2, snap.size());

        CampaignTotals a = snap.get("A");
        assertNotNull(a);
        assertEquals(15, a.impressions);
        assertEquals(3, a.clicks);
        assertEquals(3.5, a.spend, 1e-12);
        assertEquals(1, a.conversions);

        CampaignTotals b = snap.get("B");
        assertNotNull(b);
        assertEquals(7, b.impressions);
    }

    @Test
    void snapshot_isUnmodifiable() {
        CampaignAggregator agg = new CampaignAggregator();
        assertThrows(UnsupportedOperationException.class, () -> agg.snapshot().put("X", new CampaignTotals()));
    }
}