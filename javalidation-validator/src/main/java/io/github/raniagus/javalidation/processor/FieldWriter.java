package io.github.raniagus.javalidation.processor;

import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

public record FieldWriter(
        String field,
        ValidationWriter.@Nullable NullSafeWriter nullSafeWriter,
        List<ValidationWriter.NullUnsafeWriter> nullUnsafeWriters
) {

    public Stream<String> imports() {
        return Stream.concat(
                Stream.ofNullable(nullSafeWriter).flatMap(ValidationWriter::imports),
                nullUnsafeWriters.stream().flatMap(ValidationWriter::imports)
        );
    }

    public void writePropertiesTo(ValidationOutput out) {
        if (nullSafeWriter != null) {
            nullSafeWriter.writePropertiesTo(out, field);
        }
        nullUnsafeWriters.forEach(writer -> writer.writePropertiesTo(out, field));
    }

    public void writeBodyTo(ValidationOutput out) {
        if (nullSafeWriter != null) {
            nullSafeWriter.writeBodyTo(out, field);
        }
        if (!nullUnsafeWriters.isEmpty()) {
            out.write("""
                if (%s.%s() != null) {\
                """.formatted(out.getVariable(), field));

            out.incrementIndentationLevel();
            nullUnsafeWriters.forEach(writer -> writer.writeBodyTo(out, field));
            out.decrementIndentationLevel();

            out.write("}");
        }
    }
}
