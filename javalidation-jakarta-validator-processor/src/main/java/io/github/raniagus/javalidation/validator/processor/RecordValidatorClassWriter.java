package io.github.raniagus.javalidation.validator.processor;

import java.util.List;
import java.util.stream.Stream;

public record RecordValidatorClassWriter(
        String packageName, // "com.example"
        String className, // "RecordNameValidator or EnclosingClass$RecordNameValidator"
        String enclosingClassPrefix, // "" or "EnclosingClass."
        String recordName, // "RecordName"
        String recordFullName, // "EnclosingClass.RecordName"
        String recordImportName, // "com.example.RecordName" or "com.example.EnclosingClass"
        List<FieldWriter> fieldWriters
) implements ValidatorClassWriter {
    @Override
    public Stream<String> imports() {
        return Stream.concat(
                Stream.of(
                        "io.github.raniagus.javalidation.Validation",
                        "io.github.raniagus.javalidation.validator.Validator"
                ),
                fieldWriters.stream().flatMap(ValidationWriter::imports)
        );
    }

    @Override
    public void writeBody(ValidationOutput out) {
        out.write(
                """
                public class %s implements Validator<%s%s> {\
                """.formatted(className, enclosingClassPrefix, recordName));
        out.incrementIndentationLevel();
        for (FieldWriter writer : fieldWriters) {
            writer.writePropertiesTo(out);
        }
        out.write("");
        out.registerVariable("root");
        out.write("@Override");
        out.write("""
                public void validate(Validation validation, %s%s %s) {\
                """.formatted(enclosingClassPrefix, recordName, out.getVariable()));
        out.incrementIndentationLevel();
        for (ValidationWriter writer : fieldWriters) {
            writer.writeBodyTo(out);
        }
        out.decrementIndentationLevel();
        out.write("}");
        out.decrementIndentationLevel();
        out.write("}");
    }
}
