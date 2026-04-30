package io.github.raniagus.javalidation.jackson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.format.FieldKeyNotation;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

class FieldKeyDeserializerTest {
    private JsonMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder()
                .addModule(JavalidationModule.getDefault())
                .build();
    }

    // -- property path notation (default) --

    @Test
    void givenSimpleKey_whenDeserialize_thenReturnsSingleSegmentKey() {
        Map<FieldKey, String> map = mapper.readValue(
                """
                {"message":"Hello, world!"}
                """,
                new TypeReference<>() {});

        assertThat(map).containsEntry(FieldKey.of("message"), "Hello, world!");
    }

    @Test
    void givenPropertyPathKey_whenDeserialize_thenReturnsCorrectKey() {
        Map<FieldKey, String> map = mapper.readValue(
                """
                {"messages[5].text":"Hello, world!"}
                """,
                new TypeReference<>() {});

        assertThat(map).containsEntry(FieldKey.of("messages", 5, "text"), "Hello, world!");
    }

    // -- round-trip tests --

    @Nested
    class RoundTripTests {

        @Test
        void givenDefaultNotation_whenSerializeThenDeserialize_thenReturnOriginalKey() throws Exception {
            FieldKey original = FieldKey.of("items", 2, "name");
            Map<FieldKey, String> original_map = Map.of(original, "value");

            String json = mapper.writeValueAsString(original_map);
            Map<FieldKey, String> result = mapper.readValue(json, new TypeReference<>() {});

            assertThat(result).containsEntry(original, "value");
        }

        @Test
        void givenDotNotation_whenSerializeThenDeserialize_thenReturnOriginalKey() throws Exception {
            JsonMapper dotMapper = JsonMapper.builder()
                    .addModule(JavalidationModule.builder()
                            .withDotNotation()
                            .build())
                    .build();
            FieldKey original = FieldKey.of("items", 2, "name");
            Map<FieldKey, String> original_map = Map.of(original, "value");

            String json = dotMapper.writeValueAsString(original_map);
            Map<FieldKey, String> result = dotMapper.readValue(json, new TypeReference<>() {});

            assertThat(result).containsEntry(original, "value");
        }

        @Test
        void givenBracketNotation_whenSerializeThenDeserialize_thenReturnOriginalKey() throws Exception {
            JsonMapper bracketMapper = JsonMapper.builder()
                    .addModule(JavalidationModule.builder()
                            .withBracketNotation()
                            .build())
                    .build();

            FieldKey original = FieldKey.of("items", 2, "name");
            Map<FieldKey, String> original_map = Map.of(original, "value");

            String json = bracketMapper.writeValueAsString(original_map);
            Map<FieldKey, String> result = bracketMapper.readValue(json, new TypeReference<>() {});

            assertThat(result).containsEntry(original, "value");
        }
    }

    // -- explicit parser via withFieldKeyParser --

    @Test
    void givenCustomParser_whenDeserialize_thenUsesCustomParser() throws Exception {
        JsonMapper customMapper = JsonMapper.builder()
                .addModule(JavalidationModule.builder()
                        .withFieldKeyParser(FieldKeyNotation.DOTS)
                        .build())
                .build();

        Map<FieldKey, String> map = customMapper.readValue(
                """
                {"items.2.name":"value"}
                """,
                new TypeReference<>() {});

        assertThat(map).containsEntry(FieldKey.of("items", 2, "name"), "value");
    }

    // -- withFieldKeyConverter sets both serializer and deserializer --

    @Test
    void givenConverterSetViaWithFieldKeyNotation_whenSerializeThenDeserialize_thenReturnOriginalKey() throws Exception {
        JsonMapper converterMapper = JsonMapper.builder()
                .addModule(JavalidationModule.builder()
                        .withFieldKeyNotation(FieldKeyNotation.BRACKETS)
                        .build())
                .build();

        FieldKey original = FieldKey.of("user", 0, "address");
        String json = converterMapper.writeValueAsString(Map.of(original, "test"));

        assertEquals("{\"user[0][address]\":\"test\"}", json);

        Map<FieldKey, String> result = converterMapper.readValue(json, new TypeReference<>() {});
        assertThat(result).containsEntry(original, "test");
    }
}
