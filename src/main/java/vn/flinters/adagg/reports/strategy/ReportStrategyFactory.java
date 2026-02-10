package vn.flinters.adagg.reports.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.flinters.adagg.domain.ReportType;
import vn.flinters.adagg.reports.base.ReportStrategy;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ReportStrategyFactory {
    private static final Logger log = LoggerFactory.getLogger(ReportStrategyFactory.class);

    public List<ReportStrategy> create(List<ReportType> types, Path outputDir) {
        log.debug("Creating report strategies: types={}, outputDir={}", types, outputDir);

        List<ReportStrategy> list = new ArrayList<>(types.size());
        for (ReportType type : types) {
            switch (type) {
                case TOP10_CTR -> list.add(new TopCtrReportStrategy(outputDir.resolve("top10_ctr.csv")));
                case TOP10_CPA -> list.add(new TopCpaReportStrategy(outputDir.resolve("top10_cpa.csv")));
            }
        }
        return list;
    }
}
