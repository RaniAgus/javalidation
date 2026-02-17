package io.github.raniagus.javalidation.validator.processor;

import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

public interface FieldWriter extends ValidationWriter {
    @Override
    default Stream<String> imports() {
        return Stream.concat(
                Stream.ofNullable(nullSafeWriter()).flatMap(ValidationWriter::imports),
                nullUnsafeWriters().stream().flatMap(ValidationWriter::imports)
        );
    }

    default void writePropertiesTo(ValidationOutput out) {
        NullSafeWriter nullSafeWriter = nullSafeWriter();
        if (nullSafeWriter != null) {
            nullSafeWriter.writePropertiesTo(out, field());
        }

        for (NullUnsafeWriter writer : nullUnsafeWriters()) {
            writer.writePropertiesTo(out, field());
        }
    }

    String field();

    default @Nullable NullSafeWriter nullSafeWriter() {
        return null;
    }

    List<NullUnsafeWriter> nullUnsafeWriters();
}
