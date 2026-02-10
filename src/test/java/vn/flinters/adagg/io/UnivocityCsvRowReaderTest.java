package vn.flinters.adagg.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.flinters.adagg.io.base.CsvRowReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UnivocityCsvRowReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void readsRows_andSkipsHeader() throws IOException {
        Path csv = tempDir.resolve("in.csv");
        Files.writeString(csv,
                """
                        campaign_id,date,impressions,clicks,spend,conversions
                        CMP1,2025-01-01,10,1,1.5,0
                        CMP2,2025-01-02,20,2,2.5,1
                        """
        );

        CsvRowReader r = new UnivocityCsvRowReader();

        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(csv)) {
            r.readAll(br, rows::add);
        }

        assertEquals(2, rows.size());
        assertArrayEquals(new String[]{"CMP1","2025-01-01","10","1","1.5","0"}, rows.get(0));
        assertArrayEquals(new String[]{"CMP2","2025-01-02","20","2","2.5","1"}, rows.get(1));
    }
}
