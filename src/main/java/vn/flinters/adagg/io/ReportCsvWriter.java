package vn.flinters.adagg.io;

import vn.flinters.adagg.domain.ReportRow;
import vn.flinters.adagg.io.base.CsvWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.List;

public final class ReportCsvWriter implements CsvWriter<ReportRow> {
    private static final DecimalFormat CTR_FMT = new DecimalFormat("0.0000");
    private static final DecimalFormat MONEY_FMT = new DecimalFormat("0.00");

    @Override
    public void write(Path file, List<ReportRow> rows) throws IOException {
        Files.createDirectories(file.getParent());
        try (BufferedWriter w = Files.newBufferedWriter(file)) {
            w.write("campaign_id,total_impressions,total_clicks,total_spend,total_conversions,CTR,CPA\n");
            for (ReportRow r : rows) {
                String cpa = (r.cpa() == null) ? "" : MONEY_FMT.format(r.cpa());
                w.write(r.campaignId() + "," +
                        r.totalImpressions() + "," +
                        r.totalClicks() + "," +
                        MONEY_FMT.format(r.totalSpend()) + "," +
                        r.totalConversions() + "," +
                        CTR_FMT.format(r.ctr()) + "," +
                        cpa + "\n");
            }
        }
    }
}
