package io.github.raniagus.javalidation.processor;

import java.util.List;
import java.util.stream.Stream;

public record FieldWriter(
        String field,
        List<ValidationWriter.NullSafeWriter> nullSafeWriters,
        List<ValidationWriter.NullUnsafeWriter> nullUnsafeWriters
) {

    public Stream<String> imports() {
        return Stream.concat(
                nullSafeWriters.stream().flatMap(ValidationWriter::imports),
                nullUnsafeWriters.stream().flatMap(ValidationWriter::imports)
        );
    }

    public void writePropertiesTo(ValidationOutput out) {
        nullSafeWriters.forEach(writer -> writer.writePropertiesTo(out, field));
        nullUnsafeWriters.forEach(writer -> writer.writePropertiesTo(out, field));
    }

    public void writeBodyTo(ValidationOutput out) {
        nullSafeWriters.forEach(writer -> writer.writeBodyTo(out, field));
        if (nullUnsafeWriters.isEmpty()) {
            return;
        }
        out.write("""
                if (%s.%s() != null) {\
                """.formatted(out.getVariable(), field));
        out.incrementIndentationLevel();
        nullUnsafeWriters.forEach(writer -> writer.writeBodyTo(out, field));
        out.decrementIndentationLevel();
        out.write("}");
    }
}
