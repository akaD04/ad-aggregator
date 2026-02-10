package vn.flinters.adagg;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import vn.flinters.adagg.domain.MutableAdRow;
import vn.flinters.adagg.domain.ReportRow;
import vn.flinters.adagg.io.UnivocityCsvRowReader;
import vn.flinters.adagg.io.base.CsvRowReader;
import vn.flinters.adagg.reports.AdRowMapper;
import vn.flinters.adagg.io.ReportCsvWriter;
import vn.flinters.adagg.io.base.CsvWriter;
import vn.flinters.adagg.io.base.RowMapper;
import vn.flinters.adagg.reports.processor.AdReportProcessor;
import vn.flinters.adagg.reports.CampaignAggregator;
import vn.flinters.adagg.reports.base.Aggregator;
import vn.flinters.adagg.reports.base.ReportProcessor;
import vn.flinters.adagg.reports.strategy.ReportStrategyFactory;

public final class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ReportProcessor.class)
                .to(AdReportProcessor.class)
                .in(Scopes.SINGLETON);

        bind(new TypeLiteral<RowMapper<String[], MutableAdRow>>() {})
                .to(AdRowMapper.class)
                .in(Scopes.SINGLETON);

        bind(new TypeLiteral<Aggregator<MutableAdRow>>() {})
                .to(CampaignAggregator.class);

        bind(new TypeLiteral<CsvWriter<ReportRow>>() {})
                .to(ReportCsvWriter.class)
                .in(Scopes.SINGLETON);

        bind(ReportStrategyFactory.class)
                .in(Scopes.SINGLETON);

        bind(CsvRowReader.class)
                .to(UnivocityCsvRowReader.class)
                .in(Scopes.SINGLETON);
    }
}
