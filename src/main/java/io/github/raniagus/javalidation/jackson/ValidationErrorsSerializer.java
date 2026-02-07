package io.github.raniagus.javalidation.jackson;

import io.github.raniagus.javalidation.ValidationErrors;
import org.jspecify.annotations.NonNull;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class ValidationErrorsSerializer extends ValueSerializer<ValidationErrors> {
    @Override
    public void serialize(@NonNull ValidationErrors value, @NonNull JsonGenerator gen, SerializationContext context) {
        gen.writeStartObject();

        if (value.rootErrors() != null && !value.rootErrors().isEmpty()) {
            context.defaultSerializeProperty("", value.rootErrors(), gen);
        }

        if (value.fieldErrors() != null) {
            for (var entry : value.fieldErrors().entrySet()) {
                context.defaultSerializeProperty(entry.getKey(), entry.getValue(), gen);
            }
        }

        gen.writeEndObject();
    }
}
