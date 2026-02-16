package io.github.raniagus.javalidation.processor;

import java.util.List;

public record ValidatorClassWriter(
        String packageName,
        String validatorName, // "RecordNameValidator or EnclosingClass$RecordNameValidator"
        String enclosingClassPrefix, // "EnclosingClass." or ""
        String recordName,
        String recordFullName,
        List<FieldWriter> fieldWriters
) implements ClassWriter {
    @Override
    public String fullName() {
        return packageName + "." + validatorName;
    }

    @Override
    public void write(ValidationOutput out) {
        out.write(
                """
                package %s;
                
                import io.github.raniagus.javalidation.*;
                import org.jspecify.annotations.Nullable;
                
                public class %s implements Validator<%s%s> {
                """.formatted(packageName, validatorName, enclosingClassPrefix, recordName));
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
