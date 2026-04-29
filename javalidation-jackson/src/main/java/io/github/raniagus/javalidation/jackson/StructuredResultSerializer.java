package io.github.raniagus.javalidation.jackson;

import io.github.raniagus.javalidation.Result;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class StructuredResultSerializer extends ValueSerializer<Result<?>> {

    @Override
    @SuppressWarnings("unchecked")
    public Class<Result<?>> handledType() {
        return (Class<Result<?>>) (Class<?>) Result.class;
    }

    @Override
    public void serialize(Result<?> result, JsonGenerator gen, SerializationContext context) {
        gen.writeStartObject();

        switch (result) {
            case Result.Ok<?> ok -> {
                gen.writeBooleanProperty("ok", true);
                context.defaultSerializeProperty("value", ok.value(), gen);
            }
            case Result.Err<?> err -> {
                gen.writeBooleanProperty("ok", false);
                var errorsDto = StructuredValidationErrorsDto.from(err.errors());
                context.defaultSerializeProperty("errors", errorsDto, gen);
            }
        }

        gen.writeEndObject();
    }
}
