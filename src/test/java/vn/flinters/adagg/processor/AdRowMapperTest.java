package vn.flinters.adagg.processor;

import org.junit.jupiter.api.Test;
import vn.flinters.adagg.domain.MutableAdRow;
import vn.flinters.adagg.report.AdRowMapper;

import static org.junit.jupiter.api.Assertions.*;

final class AdRowMapperTest {

    @Test
    void mapInto_parsesValues_andIgnoresDateColumn() {
        AdRowMapper mapper = new AdRowMapper();
        MutableAdRow out = new MutableAdRow();

        String[] cols = {"CMP001", "2025-01-01", "12000", "300", "45.50", "12"};
        mapper.mapInto(cols, out);

        assertEquals("CMP001", out.campaignId);
        assertEquals(12000, out.impressions);
        assertEquals(300, out.clicks);
        assertEquals(45.50, out.spend, 1e-12);
        assertEquals(12, out.conversions);
    }

    @Test
    void mapInto_blankNumbersBecomeZero() {
        AdRowMapper mapper = new AdRowMapper();
        MutableAdRow out = new MutableAdRow();

        String[] cols = {"CMP002", "2025-01-01", " ", "", "   ", null};
        mapper.mapInto(cols, out);

        assertEquals("CMP002", out.campaignId);
        assertEquals(0, out.impressions);
        assertEquals(0, out.clicks);
        assertEquals(0.0, out.spend, 0.0);
        assertEquals(0, out.conversions);
    }
}
