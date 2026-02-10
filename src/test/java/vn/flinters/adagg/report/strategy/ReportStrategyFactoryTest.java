package vn.flinters.adagg.report.strategy;

import org.junit.jupiter.api.Test;
import vn.flinters.adagg.domain.ReportType;
import vn.flinters.adagg.report.base.ReportStrategy;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class ReportStrategyFactoryTest {

    @Test
    void create_buildsStrategiesForSelectedTypes() {
        ReportStrategyFactory f = new ReportStrategyFactory();
        Path outDir = Path.of("out");

        List<ReportStrategy> list = f.create(List.of(ReportType.TOP10_CTR, ReportType.TOP10_CPA), outDir);

        assertEquals(2, list.size());
        assertEquals(ReportType.TOP10_CTR, list.get(0).type());
        assertEquals(ReportType.TOP10_CPA, list.get(1).type());
    }

    @Test
    void create_emptyTypesProducesEmptyList() {
        ReportStrategyFactory f = new ReportStrategyFactory();
        assertTrue(f.create(List.of(), Path.of("out")).isEmpty());
    }
}
