package io.github.raniagus.javalidation.processor;

import java.util.List;

public record FieldWriter(
        String field,
        List<ValidationWriter.NullSafeWriter> nullSafeWriters,
        List<ValidationWriter.NullUnsafeWriter> nullUnsafeWriters
) {

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
