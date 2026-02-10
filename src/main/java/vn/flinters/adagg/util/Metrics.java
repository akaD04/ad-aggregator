package vn.flinters.adagg.util;

public final class Metrics {
    private Metrics() {}

    public static double ctr(long clicks, long impressions) {
        if (impressions <= 0) return 0.0;
        return (double) clicks / (double) impressions;
    }

    public static double cpa(double spend, long conversions) {
        if (conversions <= 0) return 0.0;
        return spend / (double) conversions;
    }

    public static Double cpaOrNull(double spend, long conversions) {
        if (conversions <= 0) return null;
        return cpa(spend, conversions);
    }
}
