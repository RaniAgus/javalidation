package io.github.raniagus.javalidation.validator.processor;

import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

public record FieldWriter(
        String field,
        ValidationWriter.@Nullable NullSafeWriter nullSafeWriter,
        List<ValidationWriter.NullUnsafeWriter> nullUnsafeWriters
) implements WithFieldWriters {

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
        out.registerVariable(field);
        out.write("var %sValidation = Validation.create();".formatted(out.getVariable()));

        writeNestedFieldsTo(field, nullSafeWriter, nullUnsafeWriters, out);

        out.removeVariable();
        out.write("%1sValidation.addAll(%2$sValidation.finish(), new Object[]{\"%2$s\"});".formatted(out.getVariable(), field));
        out.write("");
    }
}
