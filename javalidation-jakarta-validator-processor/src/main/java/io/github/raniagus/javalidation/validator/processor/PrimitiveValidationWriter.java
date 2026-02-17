package io.github.raniagus.javalidation.validator.processor;

import java.util.List;

public record PrimitiveValidationWriter(
        String field,
        List<NullUnsafeWriter> nullUnsafeWriters
) implements FieldWriter, WithWriters {
    @Override
    public void writeBodyTo(ValidationOutput out) {
        if (nullUnsafeWriters.isEmpty()) {
            return;
        }

        out.write("var %s = %s.%s();".formatted(field, out.getVariable(), field));
        out.registerVariable(field);
        out.write("var %sValidation = Validation.create();".formatted(out.getVariable()));

        nullUnsafeWriters.forEach(writer -> writer.writeBodyTo(out));

        out.removeVariable();
        out.write("%1sValidation.addAll(%2$sValidation.finish(), new Object[]{\"%2$s\"});".formatted(out.getVariable(), field));
        out.write("");
    }
}
