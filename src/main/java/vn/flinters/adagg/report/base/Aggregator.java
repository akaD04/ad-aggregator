package vn.flinters.adagg.report.base;

import vn.flinters.adagg.domain.CampaignTotals;

import java.util.Map;

public interface Aggregator<T> {
    void accept(T row);
    Map<String, CampaignTotals> snapshot();
}
