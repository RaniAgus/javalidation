package io.github.raniagus.javalidation.jackson;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.format.TemplateString;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

class FlattenedErrorsSerializerTest {
    private JsonMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder()
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .addModule(JavalidationModule.builder().withFlattenedErrors().build())
                .build();
    }

    // -- serialize --

    @Test
    void givenOnlyRootErrors_whenSerialize_thenUsesEmptyKeyForRoot() {
        ValidationErrors errors = new ValidationErrors(
                List.of(TemplateString.of("root error")),
                Map.of()
        );

        String json = mapper.writeValueAsString(errors);

        assertThat(json).isEqualTo("""
                {"":["root error"]}\
                """);
    }

    @Test
    void givenOnlyFieldErrors_whenSerialize_thenSerializesFieldsAsKeys() {
        ValidationErrors errors = new ValidationErrors(
                List.of(),
                Map.of(
                        "name", List.of(TemplateString.of("required")),
                        "age", List.of(TemplateString.of("invalid"))
                )
        );

        String json = mapper.writeValueAsString(errors);

        assertThat(json).isEqualTo("""
                {"age":["invalid"],"name":["required"]}\
                """);
    }

    @Test
    void givenRootAndFieldErrors_whenSerialize_thenFlattensBoth() {
        ValidationErrors errors = new ValidationErrors(
                List.of(TemplateString.of("global error")),
                Map.of(
                        "email", List.of(TemplateString.of("invalid format"))
                )
        );

        String json = mapper.writeValueAsString(errors);

        assertThat(json).isEqualTo("""
                {"":["global error"],"email":["invalid format"]}\
                """);
    }

    @Test
    void givenEmpty_whenSerialize_thenReturnsEmptyObject() {
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
    void givenNull_whenSerialize_thenReturnsJsonNull() {
        String json = mapper.writeValueAsString(null);

        assertEquals("null", json);
    }

    @Test
    void givenComplexObject_whenSerialize_thenSerializesNestedErrors() {
        record Container(String id, ValidationErrors errors) {}

        Container container = new Container(
                "123",
                new ValidationErrors(
                        List.of(TemplateString.of("root")),
                        Map.of(
                                "field", List.of(TemplateString.of("bad value"))
                        )
                )
        );

        String json = mapper.writeValueAsString(container);

        assertThat(json).isEqualTo("""
                {"id":"123","errors":{"":["root"],"field":["bad value"]}}\
                """);
    }
}
