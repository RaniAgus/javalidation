package io.github.raniagus.javalidation.jackson;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import io.github.raniagus.javalidation.format.TemplateString;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import org.junit.jupiter.api.*;
import tools.jackson.databind.json.JsonMapper;

class TemplateStringSerializerTest {
    private JsonMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder()
                .addModule(new JavalidationModule())
                .build();
    }

    @Test
    void shouldSerializeSimpleString() {
        TemplateString ts = new TemplateString("test");
        String json = mapper.writeValueAsString(ts);
        assertEquals("\"test\"", json);
    }

    @Test
    void shouldSerializeWithMessageFormat() {
        TemplateString ts = new TemplateString("Hello {0}, you have {1} messages", "Alice", 5);
        String json = mapper.writeValueAsString(ts);
        assertEquals("\"Hello Alice, you have 5 messages\"", json);
    }

    @Test
    void shouldSerializeNull() {
        String json = mapper.writeValueAsString(null);
        assertEquals("null", json);
    }

    @Test
    void shouldUseCustomFormatter() {
        TemplateStringFormatter custom = ts -> "CUSTOM: " + (ts != null ? ts.message() : "null");
        mapper = JsonMapper.builder()
                .addModule(new JavalidationModule(custom))
                .build();

        TemplateString ts = new TemplateString("test", "arg");
        String json = mapper.writeValueAsString(ts);
        assertEquals("\"CUSTOM: test\"", json);
    }

    @Test
    void shouldSerializeInComplexObject() {
        record Container(String name, TemplateString message) {}

        Container container = new Container(
                "test",
                new TemplateString("Count: {0}", 42)
        );

        String json = mapper.writeValueAsString(container);
        assertThat(json).isEqualTo("""
                {"name":"test","message":"Count: 42"}\
                """);
    }
}