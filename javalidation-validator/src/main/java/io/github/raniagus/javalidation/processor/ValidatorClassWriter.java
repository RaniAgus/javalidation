package io.github.raniagus.javalidation.processor;

import java.util.List;
import java.util.stream.Stream;

public record ValidatorClassWriter(
        String packageName,
        String validatorName, // "RecordNameValidator or EnclosingClass$RecordNameValidator"
        String enclosingClassPrefix, // "EnclosingClass." or ""
        String recordName,
        String recordFullName,
        List<FieldWriter> fieldWriters
) implements ClassWriter {
    @Override
    public Stream<String> imports() {
        return Stream.concat(
                Stream.of(
                        "io.github.raniagus.javalidation.Validation",
                        "io.github.raniagus.javalidation.ValidationErrors",
                        "io.github.raniagus.javalidation.validator.Validator",
                        "org.jspecify.annotations.Nullable"
                ),
                fieldWriters.stream().flatMap(FieldWriter::imports)
        );
    }

    @Override
    public String fullName() {
        return packageName + "." + validatorName;
    }

    @Override
    public void writeBody(ValidationOutput out) {
        out.write(
                """
                public class %s implements Validator<%s%s> {
                """.formatted(validatorName, enclosingClassPrefix, recordName));
        out.incrementIndentationLevel();
        for (FieldWriter writer : fieldWriters) {
            writer.writePropertiesTo(out);
        }
        out.createVariable();
        out.write("@Override");
        out.write("""
                public ValidationErrors validate(%s@Nullable %s %s) {\
                """.formatted(enclosingClassPrefix, recordName, out.getVariable()));
        out.incrementIndentationLevel();
        out.write("Validation validation = Validation.create();");
        for (FieldWriter writer : fieldWriters) {
            writer.writeBodyTo(out);
        }
        out.write("return validation.finish();");
        out.decrementIndentationLevel();
        out.write("}");
        out.decrementIndentationLevel();
        out.write("}");
    }
}
