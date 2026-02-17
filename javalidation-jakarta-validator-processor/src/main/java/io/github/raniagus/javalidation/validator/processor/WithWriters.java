package io.github.raniagus.javalidation.validator.processor;

import java.util.List;
import org.jspecify.annotations.Nullable;

public interface WithWriters {

    default void writeNestedFieldsTo(
            @Nullable NullSafeWriter nullSafeWriter,
            List<NullUnsafeWriter> nullUnsafeWriters,
            ValidationOutput out
    ) {
        if (nullSafeWriter != null) {
            nullSafeWriter.writeBodyTo(out);
        }
        if (!nullUnsafeWriters.isEmpty()) {
            out.write("""
                if (%s != null) {\
                """.formatted(out.getVariable()));

            out.incrementIndentationLevel();
            nullUnsafeWriters.forEach(writer -> writer.writeBodyTo(out));
            out.decrementIndentationLevel();

            out.write("}");
        }
    }
}
