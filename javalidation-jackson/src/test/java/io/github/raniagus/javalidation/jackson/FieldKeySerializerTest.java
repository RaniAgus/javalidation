package io.github.raniagus.javalidation.jackson;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.TemplateString;
import io.github.raniagus.javalidation.format.FieldKeyFormatter;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class FieldKeySerializerTest {
    private JsonMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder()
                .addModule(JavalidationModule.getDefault())
                .build();
    }

    // -- property path notation (default) --

    @Test
    void givenSimpleKey_whenSerialize_thenReturnsJsonString() {
        FieldKey fk = FieldKey.of("message");

        String json = mapper.writeValueAsString(Map.of(fk, "Hello, world!"));

        assertEquals("""
                {"message":"Hello, world!"}\
                """, json);
    }

    @Test
    void givenMultiKey_whenSerialize_thenFormatsAndSerializes() {
        FieldKey fk = FieldKey.of("messages", 5, "text");

        String json = mapper.writeValueAsString(Map.of(fk, "Hello, world!"));

        assertEquals("""
                {"messages[5].text":"Hello, world!"}\
                """, json);
    }

    @Test
    void givenNull_whenSerialize_thenReturnsJsonNull() {
        String json = mapper.writeValueAsString(null);

        assertEquals("null", json);
    }

    // -- dot notation --

    @Test
    void givenDotNotationFormatter_whenSerialize_thenFormatsAndSerializes() {
        mapper = JsonMapper.builder()
                .addModule(JavalidationModule.builder()
                        .withDotNotation()
                        .build())
                .build();

        FieldKey fk = FieldKey.of("messages", 5, "text");

        String json = mapper.writeValueAsString(Map.of(fk, "Hello, world!"));

        assertEquals("""
                {"messages.5.text":"Hello, world!"}\
                """, json);
    }

    // -- bracket notation --

    @Test
    void givenBracketNotationFormatter_whenSerialize_thenFormatsAndSerializes() {
        mapper = JsonMapper.builder()
                .addModule(JavalidationModule.builder()
                        .withBracketNotation()
                        .build())
                .build();

        FieldKey fk = FieldKey.of("messages", 5, "text");

        String json = mapper.writeValueAsString(Map.of(fk, "Hello, world!"));

        assertEquals("""
                {"messages[5][text]":"Hello, world!"}\
                """, json);
    }

    // -- custom formatter --

    @Test
    void givenCustomFormatter_whenSerialize_thenUsesCustomFormatter() {
        FieldKeyFormatter custom = FieldKey::toString;
        mapper = JsonMapper.builder()
                .addModule(JavalidationModule.builder()
                        .withFieldKeyFormatter(custom)
                        .build())
                .build();

        FieldKey fk = FieldKey.of("test", "arg");
        String json = mapper.writeValueAsString(Map.of(fk, "value"));

        assertEquals("""
                {"FieldKey{parts=[test, arg]}":"value"}\
                """, json);
    }

    @Test
    void givenComplexObject_whenSerialize_thenSerializesNestedFieldKey() {
        record Container(String name, Map<FieldKey, String> map) {}

        Container container = new Container(
                "test",
                Map.of(FieldKey.of("field", 42), "Count")
        );

        String json = mapper.writeValueAsString(container);

        assertThat(json).isEqualTo("""
                {"name":"test","map":{"field[42]":"Count"}}\
                """);
    }
}
