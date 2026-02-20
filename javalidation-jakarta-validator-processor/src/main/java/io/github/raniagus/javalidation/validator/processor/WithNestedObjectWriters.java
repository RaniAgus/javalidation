package io.github.raniagus.javalidation.validator.processor;

import java.util.List;
import org.jspecify.annotations.Nullable;

public interface WithNestedObjectWriters {

    default void writeNestedFieldsTo(
            @Nullable NullSafeWriter nullSafeWriter,
            List<NullUnsafeWriter> nullUnsafeWriters,
            ValidationOutput out
    ) {
        if (nullSafeWriter != null) {
            nullSafeWriter.writeBodyTo(out);
        } else {
            out.write("if (%s == null) return;".formatted(out.getVariable()));
        }
        nullUnsafeWriters.forEach(writer -> writer.writeBodyTo(out));
    }
}
