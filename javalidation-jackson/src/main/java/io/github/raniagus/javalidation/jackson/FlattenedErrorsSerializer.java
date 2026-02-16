package io.github.raniagus.javalidation.jackson;

import static tools.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.ValidationErrors;
import java.util.Map;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class FlattenedErrorsSerializer extends ValueSerializer<ValidationErrors> {
    @Override
    public void serialize(ValidationErrors value, JsonGenerator gen, SerializationContext context) {
        gen.writeStartObject();

        if (!value.rootErrors().isEmpty()) {
            context.defaultSerializeProperty("", value.rootErrors(), gen);
        }

        var entries = context.hasSerializationFeatures(ORDER_MAP_ENTRIES_BY_KEYS.getMask()) ?
                  value.fieldErrors().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()
                : value.fieldErrors().entrySet();

        for (var entry : entries) {
            context.findKeySerializer(FieldKey.class, null).serialize(entry.getKey(), gen, context);
            context.findValueSerializer(entry.getValue().getClass()).serialize(entry.getValue(), gen, context);
        }

        gen.writeEndObject();
    }
}
