package vn.flinters.adagg.io.base;

import java.io.IOException;
import java.io.Reader;
import java.util.function.Consumer;

public interface CsvRowReader {
    void readAll(Reader reader, Consumer<String[]> rowConsumer) throws IOException;
}
