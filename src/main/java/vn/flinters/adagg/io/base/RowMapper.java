package vn.flinters.adagg.io.base;

public interface RowMapper<I, O> {
    void mapInto(I input, O out);
}
