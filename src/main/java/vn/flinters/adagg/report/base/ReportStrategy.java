package vn.flinters.adagg.report.base;

import vn.flinters.adagg.domain.CampaignTotals;
import vn.flinters.adagg.domain.ReportType;
import vn.flinters.adagg.report.ReportResult;

import java.util.Map;

public interface ReportStrategy {
    ReportType type();
    ReportResult generate(Map<String, CampaignTotals> totals);
}
