package vn.flinters.adagg.report.base;

import vn.flinters.adagg.domain.ReportType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ReportProcessor {
    void process(Path input, Path outputDir, List<ReportType> types)
            throws IOException, InterruptedException;
}