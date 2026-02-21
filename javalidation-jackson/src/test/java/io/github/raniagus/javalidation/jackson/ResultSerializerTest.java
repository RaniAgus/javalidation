package io.github.raniagus.javalidation.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

class ResultSerializerTest {
    private JsonMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder()
                .addModule(JavalidationModule.getDefault())
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .build();
    }

    // -- serialize Ok --

    @Test
    void givenOkWithString_whenSerialize_thenWritesTypeAndValue() {
        Result<String> result = Result.ok("hello");

        String json = mapper.writeValueAsString(result);

        assertThat(json).isEqualTo("""
                {"ok":"true","value":"hello"}\
                """);
    }

    @Test
    void givenOkWithInteger_whenSerialize_thenWritesTypeAndValue() {
        Result<Integer> result = Result.ok(42);

        String json = mapper.writeValueAsString(result);

        assertThat(json).isEqualTo("""
                {"ok":"true","value":42}\
                """);
    }

    @Test
    void givenOkWithNull_whenSerialize_thenWritesTypeAndNullValue() {
        Result<String> result = Result.ok(null);

        String json = mapper.writeValueAsString(result);

        assertThat(json).isEqualTo("""
                {"ok":"true","value":null}\
                """);
    }

    @Test
    void givenOkWithComplexObject_whenSerialize_thenWritesTypeAndNestedObject() {
        record Person(String name, int age) {}
        Result<Person> result = Result.ok(new Person("Alice", 30));

        String json = mapper.writeValueAsString(result);

        assertThat(json).isEqualTo("""
                {"ok":"true","value":{"name":"Alice","age":30}}\
                """);
    }

    // -- serialize Err --

    @Test
    void givenErrWithRootError_whenSerialize_thenWritesTypeAndErrors() {
        Result<String> result = Result.error("Invalid input");

        String json = mapper.writeValueAsString(result);

        assertThat(json).isEqualTo("""
                {"ok":"false","errors":{"rootErrors":["Invalid input"]}}\
                """);
    }

    @Test
    void givenErrWithFieldError_whenSerialize_thenWritesTypeAndErrors() {
        Result<String> result = Result.errorAt("email", "Invalid format");

        String json = mapper.writeValueAsString(result);

        assertThat(json).isEqualTo("""
                {"ok":"false","errors":{"fieldErrors":{"email":["Invalid format"]}}}\
                """);
    }

    @Test
    void givenErrWithMultipleErrors_whenSerialize_thenWritesAllErrors() {
        ValidationErrors errors = Validation.create()
                .addError("Global error")
                .addErrorAt("name", "Required")
                .addErrorAt("age", "Must be positive")
                .finish();
        Result<String> result = Result.error(errors);

        String json = mapper.writeValueAsString(result);

        assertThat(json).isEqualTo("""
                {"ok":"false","errors":{"rootErrors":["Global error"],"fieldErrors":{"age":["Must be positive"],"name":["Required"]}}}\
                """);
    }

    // -- nested in container --

    @Test
    void givenResultInContainer_whenSerialize_thenSerializesNested() {
        record Response(String id, Result<String> result) {}
        Response response = new Response("123", Result.ok("success"));

        String json = mapper.writeValueAsString(response);

        assertThat(json).isEqualTo("""
                {"id":"123","result":{"ok":"true","value":"success"}}\
                """);
    }

}
