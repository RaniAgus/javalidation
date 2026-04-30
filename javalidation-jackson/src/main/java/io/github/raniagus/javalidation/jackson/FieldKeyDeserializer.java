package io.github.raniagus.javalidation.jackson;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.format.FieldKeyParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.KeyDeserializer;

/**
 * Jackson {@link KeyDeserializer} for {@link FieldKey} map keys.
 * <p>
 * Delegates to a {@link FieldKeyParser} to convert the raw JSON key string back into a
 * {@link FieldKey} instance. By default, uses {@link FieldKeyParser#getDefault()} (property-path
 * notation), which is the inverse of the default {@link FieldKeySerializer}.
 *
 * @see FieldKeySerializer
 * @see FieldKeyParser
 */
public class FieldKeyDeserializer extends KeyDeserializer {
    private final FieldKeyParser parser;

    /**
     * Creates a deserializer using the given parser.
     *
     * @param parser the parser to use
     */
    public FieldKeyDeserializer(FieldKeyParser parser) {
        this.parser = parser;
    }

    /**
     * Creates a deserializer using the default parser (property-path notation).
     */
    public FieldKeyDeserializer() {
        this(FieldKeyParser.getDefault());
    }

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) {
        return parser.parse(key);
    }
}
