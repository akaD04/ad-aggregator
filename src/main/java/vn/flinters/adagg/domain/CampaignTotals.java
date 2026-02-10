package vn.flinters.adagg.domain;

public final class CampaignTotals {
    public long impressions;
    public long clicks;
    public double spend;
    public long conversions;

    public void add(MutableAdRow r) {
        impressions += r.impressions;
        clicks += r.clicks;
        spend += r.spend;
        conversions += r.conversions;
    }
}
