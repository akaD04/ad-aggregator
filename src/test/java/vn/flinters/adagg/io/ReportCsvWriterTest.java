package vn.flinters.adagg.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.flinters.adagg.domain.ReportRow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ReportCsvWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void write_writesHeaderAndRows_andBlankCpaWhenNull() throws IOException {
        ReportCsvWriter w = new ReportCsvWriter();

        Path out = tempDir.resolve("r.csv");

        List<ReportRow> rows = List.of(
                new ReportRow("A", 100, 10, 12.345, 2, 0.1, 6.1725),
                new ReportRow("B", 200, 20, 1.0, 0, 0.1, null)
        );

        w.write(out, rows);

        String text = Files.readString(out);
        assertTrue(text.startsWith("campaign_id,total_impressions,total_clicks,total_spend,total_conversions,CTR,CPA\n"));

        // basic checks (format is DecimalFormat-based)
        assertTrue(text.contains("A,100,10,12.35,2,0.1000,6.17\n"));
        assertTrue(text.contains("B,200,20,1.00,0,0.1000,\n"));
    }
}
