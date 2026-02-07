package io.github.raniagus.javalidation.jackson;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.format.TemplateString;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.*;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

class ValidationErrorsSerializerTest {
    private JsonMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder()
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .addModule(new JavalidationModule())
                .build();
    }

    @Test
    void shouldSerializeOnlyRootErrors() {
        ValidationErrors errors = new ValidationErrors(
                List.of(new TemplateString("root error")),
                Map.of()
        );

        String json = mapper.writeValueAsString(errors);

        assertThat(json).isEqualTo("""
                {"":["root error"]}\
                """);
    }

    @Test
    void shouldSerializeOnlyFieldErrors() {
        ValidationErrors errors = new ValidationErrors(
                List.of(),
                Map.of(
                        "age", List.of(new TemplateString("invalid")),
                        "name", List.of(new TemplateString("required"))
                )
        );

        String json = mapper.writeValueAsString(errors);

        assertThat(json).isEqualTo("""
                {"age":["invalid"],"name":["required"]}\
                """);
    }

    @Test
    void shouldFlattenRootAndFieldErrors() {
        ValidationErrors errors = new ValidationErrors(
                List.of(new TemplateString("global error")),
                Map.of(
                        "email", List.of(new TemplateString("invalid format"))
                )
        );

        String json = mapper.writeValueAsString(errors);

        assertThat(json).isEqualTo("""
                {"":["global error"],"email":["invalid format"]}\
                """);
    }

    @Test
    void shouldSkipEmptyCollections() {
        ValidationErrors errors = new ValidationErrors(
                List.of(),
                Map.of()
        );

        String json = mapper.writeValueAsString(errors);

        assertThat(json).isEqualTo("""
                {}\
                """);
    }

    @Test
    void shouldSerializeInComplexObject() {
        record Container(String id, ValidationErrors errors) {
        }

        Container container = new Container(
                "123",
                new ValidationErrors(
                        List.of(new TemplateString("root")),
                        Map.of(
                                "field", List.of(new TemplateString("bad value"))
                        )
                )
        );

        String json = mapper.writeValueAsString(container);

        assertThat(json).isEqualTo("""
                {"id":"123","errors":{"":["root"],"field":["bad value"]}}\
                """);
    }

    @Test
    void shouldSerializeNullAsNull() {
        String json = mapper.writeValueAsString(null);
        assertEquals("null", json);
    }
}