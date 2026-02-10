package vn.flinters.adagg.io.base;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface CsvWriter<T> {
    void write(Path file, List<T> rows) throws IOException;
}
