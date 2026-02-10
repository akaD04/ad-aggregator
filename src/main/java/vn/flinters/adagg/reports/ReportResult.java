package vn.flinters.adagg.reports;

import vn.flinters.adagg.domain.ReportRow;
import vn.flinters.adagg.domain.ReportType;

import java.nio.file.Path;
import java.util.List;

public record ReportResult(
        ReportType type,
        Path outputFile,
        List<ReportRow> rows
) {}
