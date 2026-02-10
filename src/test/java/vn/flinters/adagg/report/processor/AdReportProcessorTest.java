package vn.flinters.adagg.report.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.flinters.adagg.domain.CampaignTotals;
import vn.flinters.adagg.domain.MutableAdRow;
import vn.flinters.adagg.domain.ReportRow;
import vn.flinters.adagg.domain.ReportType;
import vn.flinters.adagg.io.UnivocityCsvRowReader;
import vn.flinters.adagg.io.base.CsvWriter;
import vn.flinters.adagg.io.base.RowMapper;
import vn.flinters.adagg.report.AdRowMapper;
import vn.flinters.adagg.report.CampaignAggregator;
import vn.flinters.adagg.report.ReportResult;
import vn.flinters.adagg.report.base.Aggregator;
import vn.flinters.adagg.report.base.ReportStrategy;
import vn.flinters.adagg.report.strategy.ReportStrategyFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
final class AdReportProcessorTest {

    @Mock CsvWriter<ReportRow> writer;
    @Mock
    ReportStrategyFactory factory;
    @Mock ReportStrategy strategy;

    private AdReportProcessor processor;

    private Path tempDir;
    private Path inputCsv;
    private Path outputCsv;

    @BeforeEach
    void setUp() throws Exception {
        tempDir = Files.createTempDirectory("adagg-test");
        inputCsv = tempDir.resolve("ad_data.csv");
        outputCsv = tempDir.resolve("top10_ctr.csv");

        Files.writeString(inputCsv,
                """
                        campaign_id,date,impressions,clicks,spend,conversions
                        A,2025-01-01,100,10,10.00,5
                        A,2025-01-02,200,20,30.00,5
                        B,2025-01-01,100,1,5.00,0
                        """
        );

        RowMapper<String[], MutableAdRow> mapper = new AdRowMapper();
        Aggregator<MutableAdRow> aggregator = new CampaignAggregator();

        processor = new AdReportProcessor(mapper, aggregator, writer, factory, new UnivocityCsvRowReader());
    }

    @Test
    void process_verifiesAllInputsExactly() throws Exception {
        // Arrange
        List<ReportType> requestedTypes = List.of(ReportType.TOP10_CTR);

        when(factory.create(requestedTypes, tempDir))
                .thenReturn(List.of(strategy));

        ArgumentCaptor<Map<String, CampaignTotals>> totalsCaptor =
                ArgumentCaptor.forClass(Map.class);

        when(strategy.generate(totalsCaptor.capture()))
                .thenReturn(new ReportResult(
                        ReportType.TOP10_CTR,
                        outputCsv,
                        List.of(
                                new ReportRow("A", 300, 30, 40.0, 10, 0.1, 4.0),
                                new ReportRow("B", 100, 1, 5.0, 0, 0.01, null)
                        )
                ));

        // Act
        processor.process(inputCsv, tempDir, requestedTypes);

        // Assert: factory input params
        verify(factory).create(requestedTypes, tempDir);

        // Assert: aggregated totals content
        Map<String, CampaignTotals> totals = totalsCaptor.getValue();
        assertEquals(2, totals.size());

        CampaignTotals a = totals.get("A");
        assertEquals(300, a.impressions);
        assertEquals(30, a.clicks);
        assertEquals(40.0, a.spend, 1e-12);
        assertEquals(10, a.conversions);

        CampaignTotals b = totals.get("B");
        assertEquals(100, b.impressions);
        assertEquals(1, b.clicks);
        assertEquals(5.0, b.spend, 1e-12);
        assertEquals(0, b.conversions);

        // Assert: writer input params EXACT
        verify(writer).write(
                outputCsv,
                List.of(
                        new ReportRow("A", 300, 30, 40.0, 10, 0.1, 4.0),
                        new ReportRow("B", 100, 1, 5.0, 0, 0.01, null)
                )
        );

        verifyNoMoreInteractions(writer, factory, strategy);
    }

    @Test
    void process_exceptionPath_noWriterInvocation() {
        // Arrange
        List<ReportType> requestedTypes = List.of(ReportType.TOP10_CTR);

        when(factory.create(requestedTypes, tempDir))
                .thenReturn(List.of(strategy));

        ArgumentCaptor<Map<String, CampaignTotals>> captor =
                ArgumentCaptor.forClass(Map.class);

        when(strategy.generate(captor.capture()))
                .thenThrow(new IllegalStateException("boom"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> processor.process(inputCsv, tempDir, requestedTypes)
        );

        assertEquals("boom", ex.getCause().getMessage());

        Map<String, CampaignTotals> totals = captor.getValue();
        assertEquals(2, totals.size());

        CampaignTotals a = totals.get("A");
        assertNotNull(a);
        assertEquals(300, a.impressions);
        assertEquals(30, a.clicks);
        assertEquals(40.0, a.spend, 1e-12);
        assertEquals(10, a.conversions);

        verifyNoInteractions(writer);
    }
}