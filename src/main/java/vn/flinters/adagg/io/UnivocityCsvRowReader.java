package vn.flinters.adagg.io;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.flinters.adagg.io.base.CsvRowReader;

import java.io.Reader;
import java.util.function.Consumer;

public final class UnivocityCsvRowReader implements CsvRowReader {

    private final CsvParserSettings settings;
    private static final Logger log = LoggerFactory.getLogger(UnivocityCsvRowReader.class);

    public UnivocityCsvRowReader() {
        CsvParserSettings s = new CsvParserSettings();
        s.setHeaderExtractionEnabled(true);
        s.setLineSeparatorDetectionEnabled(true);
        s.setIgnoreLeadingWhitespaces(true);
        s.setIgnoreTrailingWhitespaces(true);
        this.settings = s;
    }

    @Override
    public void readAll(Reader reader, Consumer<String[]> rowConsumer) {
        CsvParser parser = new CsvParser(settings);
        parser.beginParsing(reader);
        try {
            String[] row;
            while ((row = parser.parseNext()) != null) {
                rowConsumer.accept(row);
            }
        } catch (RuntimeException e) {
            log.error("CSV parsing failed", e);
            throw e;
        } finally {
            parser.stopParsing();
        }
    }
}
