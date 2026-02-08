package io.github.raniagus.javalidation.jackson;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.raniagus.javalidation.format.TemplateString;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class TemplateStringSerializerTest {
    private JsonMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder()
                .addModule(JavalidationModule.getDefault())
                .build();
    }

    // -- serialize --

    @Test
    void givenSimpleString_whenSerialize_thenReturnsJsonString() {
        TemplateString ts = TemplateString.of("test");

        String json = mapper.writeValueAsString(ts);

        assertEquals("\"test\"", json);
    }

    @Test
    void givenTemplateWithArgs_whenSerialize_thenFormatsAndSerializes() {
        TemplateString ts = TemplateString.of("Hello {0}, you have {1} messages", "Alice", 5);

        String json = mapper.writeValueAsString(ts);

        assertEquals("\"Hello Alice, you have 5 messages\"", json);
    }

    @Test
    void givenNull_whenSerialize_thenReturnsJsonNull() {
        String json = mapper.writeValueAsString(null);

        assertEquals("null", json);
    }

    @Test
    void givenCustomFormatter_whenSerialize_thenUsesCustomFormatter() {
        TemplateStringFormatter custom = ts -> "CUSTOM: " + (ts != null ? ts.message() : "null");
        mapper = JsonMapper.builder()
                .addModule(JavalidationModule.builder()
                        .withTemplateStringFormatter(custom)
                        .build())
                .build();

        TemplateString ts = TemplateString.of("test", "arg");
        String json = mapper.writeValueAsString(ts);

        assertEquals("\"CUSTOM: test\"", json);
    }

    @Test
    void givenComplexObject_whenSerialize_thenSerializesNestedTemplateString() {
        record Container(String name, TemplateString message) {}

        Container container = new Container(
                "test",
                TemplateString.of("Count: {0}", 42)
        );

        String json = mapper.writeValueAsString(container);

        assertThat(json).isEqualTo("""
                {"name":"test","message":"Count: 42"}\
                """);
    }
}
