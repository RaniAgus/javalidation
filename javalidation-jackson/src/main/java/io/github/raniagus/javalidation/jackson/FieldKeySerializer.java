package io.github.raniagus.javalidation.jackson;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.format.FieldKeyFormatter;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class FieldKeySerializer extends ValueSerializer<FieldKey> {
    private final FieldKeyFormatter formatter;

    public FieldKeySerializer(FieldKeyFormatter formatter) {
        this.formatter = formatter;
    }

    public FieldKeySerializer() {
        this(FieldKeyFormatter.getDefault());
    }

    @Override
    public void serialize(FieldKey value, JsonGenerator gen, SerializationContext context) {
        String formatted = formatter.format(value);
        gen.writeName(formatted);
    }
}
