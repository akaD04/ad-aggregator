package vn.flinters.adagg.report;

import vn.flinters.adagg.domain.CampaignTotals;
import vn.flinters.adagg.domain.MutableAdRow;
import vn.flinters.adagg.report.base.Aggregator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class CampaignAggregator implements Aggregator<MutableAdRow> {
    private final Map<String, CampaignTotals> totals = new HashMap<>(1 << 16);

    @Override
    public void accept(MutableAdRow row) {
        CampaignTotals t = totals.computeIfAbsent(row.campaignId, k -> new CampaignTotals());
        t.add(row);
    }

    @Override
    public Map<String, CampaignTotals> snapshot() {
        return Collections.unmodifiableMap(totals);
    }}
