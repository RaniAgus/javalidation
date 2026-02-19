package io.github.raniagus.javalidation.validator.processor;

import java.util.List;
import org.jspecify.annotations.Nullable;

public interface WithNestedObjectWriters {

    @Nullable NullSafeWriter nullSafeWriter();

    List<NullUnsafeWriter> nullUnsafeWriters();

    default void writeNestedFieldsTo(
            ValidationOutput out
    ) {
        NullSafeWriter nullSafeWriter = nullSafeWriter();
        if (nullSafeWriter != null) {
            nullSafeWriter.writeBodyTo(out);
        } else {
            out.write("if (%s == null) return;".formatted(out.getVariable()));
        }
        nullUnsafeWriters().forEach(writer -> writer.writeBodyTo(out));
    }
}
