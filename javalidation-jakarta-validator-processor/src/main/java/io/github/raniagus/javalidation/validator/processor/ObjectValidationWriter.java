package io.github.raniagus.javalidation.validator.processor;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record ObjectValidationWriter(
        String field,
        @Nullable NullSafeWriter nullSafeWriter,
        List<NullUnsafeWriter> nullUnsafeWriters
) implements FieldWriter, WithWriters {
    @Override
    public void writeBodyTo(ValidationOutput out) {
        if (nullSafeWriter == null && nullUnsafeWriters.isEmpty()) {
            return;
        }

        out.write("var %s = %s.%s();".formatted(field, out.getVariable(), field));
        out.registerVariable(field);
        out.write("var %sValidation = Validation.create();".formatted(out.getVariable()));

        writeNestedFieldsTo(nullSafeWriter, nullUnsafeWriters, out);

        out.removeVariable();
        out.write("%1sValidation.addAll(%2$sValidation.finish(), new Object[]{\"%2$s\"});".formatted(out.getVariable(), field));
        out.write("");
    }
}
