package io.github.raniagus.javalidation.processor;

import java.util.List;
import java.util.stream.Stream;

public record ValidatorClassWriter(
        String packageName, // "com.example"
        String className, // "RecordNameValidator or EnclosingClass$RecordNameValidator"
        String enclosingClassPrefix, // "" or "EnclosingClass."
        String recordName, // "RecordName"
        String recordFullName, // "EnclosingClass.RecordName"
        String recordImportName, // "com.example.RecordName" or "com.example.EnclosingClass"
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
                public ValidationErrors validate(%s@Nullable %s %s) {\
                """.formatted(enclosingClassPrefix, recordName, out.getVariable()));
        out.incrementIndentationLevel();
        out.write("Validation %sValidation = Validation.create();".formatted(out.getVariable()));
        out.write("");
        for (FieldWriter writer : fieldWriters) {
            writer.writeBodyTo(out);
        }
        out.write("return %sValidation.finish();".formatted(out.getVariable()));
        out.decrementIndentationLevel();
        out.write("}");
        out.decrementIndentationLevel();
        out.write("}");
    }
}
