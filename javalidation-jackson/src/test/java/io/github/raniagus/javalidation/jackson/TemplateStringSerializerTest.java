package io.github.raniagus.javalidation.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.raniagus.javalidation.TemplateString;
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

        assertThat(json).isEqualTo("\"test\"");
    }

    @Test
    void givenTemplateWithArgs_whenSerialize_thenFormatsAndSerializes() {
        TemplateString ts = TemplateString.of("Hello {0}, you have {1} messages", "Alice", 5);

        String json = mapper.writeValueAsString(ts);

        assertThat(json).isEqualTo("\"Hello Alice, you have 5 messages\"");
    }

    @Test
    void givenNull_whenSerialize_thenReturnsJsonNull() {
        String json = mapper.writeValueAsString(null);

        assertThat(json).isEqualTo("null");
    }

    @Test
    void givenCustomFormatter_whenSerialize_thenUsesCustomFormatter() {
        TemplateStringFormatter custom = ts -> "CUSTOM: " + ts.message();
        mapper = JsonMapper.builder()
                .addModule(JavalidationModule.builder()
                        .withTemplateStringFormatter(custom)
                        .build())
                .build();

        TemplateString ts = TemplateString.of("test", "arg");
        String json = mapper.writeValueAsString(ts);

        assertThat(json).isEqualTo("\"CUSTOM: test\"");
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
