package vn.flinters.adagg.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

final class MetricsTest {

    @Test
    void ctr_zeroWhenImpressionsNonPositive() {
        assertEquals(0.0, Metrics.ctr(10, 0), 0.0);
        assertEquals(0.0, Metrics.ctr(10, -1), 0.0);
    }

    @Test
    void ctr_normal() {
        assertEquals(0.25, Metrics.ctr(25, 100), 1e-12);
    }

    @Test
    void cpa_zeroWhenConversionsNonPositive() {
        assertEquals(0.0, Metrics.cpa(10.0, 0), 0.0);
        assertEquals(0.0, Metrics.cpa(10.0, -2), 0.0);
    }

    @Test
    void cpa_normal() {
        assertEquals(2.5, Metrics.cpa(10.0, 4), 1e-12);
    }

    @Test
    void cpaOrNull_nullWhenConversionsNonPositive() {
        assertNull(Metrics.cpaOrNull(10.0, 0));
        assertNull(Metrics.cpaOrNull(10.0, -1));
    }

    @Test
    void cpaOrNull_valueWhenConversionsPositive() {
        assertEquals(2.0, Metrics.cpaOrNull(10.0, 5), 1e-12);
    }
}