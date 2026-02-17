package io.github.raniagus.javalidation.processor;

import java.util.List;
import org.jspecify.annotations.Nullable;

public interface WithFieldWriters {

    default void writeNestedFieldsTo(
            String field,
            ValidationWriter.@Nullable NullSafeWriter nullSafeWriter,
            List<ValidationWriter.NullUnsafeWriter> nullUnsafeWriters,
            ValidationOutput out
    ) {
        if (nullSafeWriter != null) {
            nullSafeWriter.writeBodyTo(out);
        }
        if (!nullUnsafeWriters.isEmpty()) {
            out.write("""
                if (java.util.Objects.nonNull(%s)) {\
                """.formatted(out.getVariable()));

            out.incrementIndentationLevel();
            nullUnsafeWriters.forEach(writer -> writer.writeBodyTo(out));
            out.decrementIndentationLevel();

            out.write("}");
        }
    }
}
