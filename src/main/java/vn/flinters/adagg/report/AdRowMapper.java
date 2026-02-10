package vn.flinters.adagg.report;

import vn.flinters.adagg.domain.MutableAdRow;
import vn.flinters.adagg.io.base.RowMapper;

public final class AdRowMapper implements RowMapper<String[], MutableAdRow> {

    @Override
    public void mapInto(String[] c, MutableAdRow out) {
        // campaign_id,date,impressions,clicks,spend,conversions
        out.campaignId = c[0];
        out.impressions = parseInt(c[2]);
        out.clicks = parseInt(c[3]);
        out.spend = parseDouble(c[4]);
        out.conversions = parseInt(c[5]);
    }

    private static int parseInt(String s) {
        if (s == null || s.isBlank()) return 0;
        return Integer.parseInt(s.trim());
    }

    private static double parseDouble(String s) {
        if (s == null || s.isBlank()) return 0.0;
        return Double.parseDouble(s.trim());
    }
}
