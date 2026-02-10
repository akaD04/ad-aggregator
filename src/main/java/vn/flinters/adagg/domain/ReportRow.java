package vn.flinters.adagg.domain;

public record ReportRow(
        String campaignId,
        long totalImpressions,
        long totalClicks,
        double totalSpend,
        long totalConversions,
        double ctr,
        Double cpa
) {}
