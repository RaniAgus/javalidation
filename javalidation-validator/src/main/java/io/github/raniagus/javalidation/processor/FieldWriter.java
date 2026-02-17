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
        if (nullSafeWriter == null && nullUnsafeWriters.isEmpty()) {
            return;
        }
        out.write("var %s = %s.%s();".formatted(field, out.getVariable(), field));
        out.createVariable(field);
        out.addKey(field);
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
        out.removeVariable();
        out.removeKey();
    }
}
