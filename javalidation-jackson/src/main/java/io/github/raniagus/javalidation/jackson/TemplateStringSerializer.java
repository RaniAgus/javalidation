package io.github.raniagus.javalidation.jackson;

import io.github.raniagus.javalidation.TemplateString;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class TemplateStringSerializer extends ValueSerializer<TemplateString> {
    private final TemplateStringFormatter formatter;

    public TemplateStringSerializer(TemplateStringFormatter formatter) {
        this.formatter = formatter;
    }

    public TemplateStringSerializer() {
        this(TemplateStringFormatter.getDefault());
    }

    @Override
    public void serialize(TemplateString value, JsonGenerator gen, SerializationContext context) {
        String formatted = formatter.format(value);
        gen.writeString(formatted);
    }
}
