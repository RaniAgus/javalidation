package io.github.raniagus.javalidation.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

class StructuredResultSerializerTest {
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
                {"ok":true,"value":"hello"}\
                """);
    }

    @Test
    void givenOkWithInteger_whenSerialize_thenWritesTypeAndValue() {
        Result<Integer> result = Result.ok(42);

        String json = mapper.writeValueAsString(result);

        assertThat(json).isEqualTo("""
                {"ok":true,"value":42}\
                """);
    }

    @Test
    void givenOkWithNull_whenSerialize_thenWritesTypeAndNullValue() {
        Result<String> result = Result.ok(null);

        String json = mapper.writeValueAsString(result);

        assertThat(json).isEqualTo("""
                {"ok":true,"value":null}\
                """);
    }

    @Test
    void givenOkWithComplexObject_whenSerialize_thenWritesTypeAndNestedObject() {
        record Person(String name, int age) {}
        Result<Person> result = Result.ok(new Person("Alice", 30));

        String json = mapper.writeValueAsString(result);

        assertThat(json).isEqualTo("""
                {"ok":true,"value":{"name":"Alice","age":30}}\
                """);
    }

    @Test
    void givenOkWithList_whenSerialize_thenWritesTypeAndArray() {
        Result<List<String>> result = Result.ok(List.of("a", "b", "c"));

        String json = mapper.writeValueAsString(result);

        assertThat(json).isEqualTo("""
                {"ok":true,"value":["a","b","c"]}\
                """);
    }

    // -- serialize Err with structured format --

    @Test
    void givenErrWithRootError_whenSerialize_thenWritesStructuredErrors() {
        Result<String> result = Result.error("Invalid input");

        String json = mapper.writeValueAsString(result);

        assertThat(json).isEqualTo("""
                {"ok":false,"errors":{"rootErrors":[{"message":"Invalid input","code":"Invalid input","args":[]}],"fieldErrors":[]}}\
                """);
    }

    @Test
    void givenErrWithFieldError_whenSerialize_thenWritesStructuredFieldErrors() {
        Result<String> result = Result.errorAt("email", "Invalid format");

        String json = mapper.writeValueAsString(result);

        assertThat(json).isEqualTo("""
                {"ok":false,"errors":{"rootErrors":[],"fieldErrors":[{"key":["email"],"errors":[{"message":"Invalid format","code":"Invalid format","args":[]}]}]}}\
                """);
    }

    @Test
    void givenErrWithTemplateArgs_whenSerialize_thenWritesFormattedMessageAndTemplateWithArgs() {
        Result<String> result = Result.errorAt("age", "Must be at least {0}", 18);

        String json = mapper.writeValueAsString(result);

        assertThat(json).isEqualTo("""
                {"ok":false,"errors":{"rootErrors":[],"fieldErrors":[{"key":["age"],"errors":[{"message":"Must be at least 18","code":"Must be at least {0}","args":[18]}]}]}}\
                """);
    }

    @Test
    void givenErrWithMultipleErrors_whenSerialize_thenWritesAllStructuredErrors() {
        ValidationErrors errors = Validation.create()
                .addError("Global error")
                .addErrorAt("name", "Required")
                .addErrorAt("age", "Must be positive")
                .finish();
        Result<String> result = Result.error(errors);

        String json = mapper.writeValueAsString(result);

        // Field order may vary, so check structure instead of exact string
        assertThat(json).contains("\"ok\":false");
        assertThat(json).contains("\"rootErrors\":[{\"message\":\"Global error\",\"code\":\"Global error\",\"args\":[]}]");
        assertThat(json).contains("{\"key\":[\"age\"],\"errors\":[{\"message\":\"Must be positive\",\"code\":\"Must be positive\",\"args\":[]}]}");
        assertThat(json).contains("{\"key\":[\"name\"],\"errors\":[{\"message\":\"Required\",\"code\":\"Required\",\"args\":[]}]}");
    }

    @Test
    void givenErrWithNestedFieldKeys_whenSerialize_thenWritesStructuredKeyArray() {
        ValidationErrors errors = ValidationErrors.at("user", "Invalid")
                .withPrefix("address", 0, "street");
        Result<String> result = Result.error(errors);

        String json = mapper.writeValueAsString(result);

        assertThat(json).isEqualTo("""
                {"ok":false,"errors":{"rootErrors":[],"fieldErrors":[{"key":["address",0,"street","user"],"errors":[{"message":"Invalid","code":"Invalid","args":[]}]}]}}\
                """);
    }

    @Test
    void givenErrWithMultipleErrorsOnSameField_whenSerialize_thenGroupsInArray() {
        ValidationErrors errors = Validation.create()
                .addErrorAt("password", "Required")
                .addErrorAt("password", "Must be at least {0} characters", 8)
                .addErrorAt("password", "Must contain a number")
                .finish();
        Result<String> result = Result.error(errors);

        String json = mapper.writeValueAsString(result);

        assertThat(json).isEqualTo("""
                {"ok":false,"errors":{"rootErrors":[],"fieldErrors":[{"key":["password"],"errors":[{"message":"Required","code":"Required","args":[]},{"message":"Must be at least 8 characters","code":"Must be at least {0} characters","args":[8]},{"message":"Must contain a number","code":"Must contain a number","args":[]}]}]}}\
                """);
    }

    // -- nested in container --

    @Test
    void givenResultInContainer_whenSerialize_thenSerializesNested() {
        record Response(String id, Result<String> result) {}
        Response response = new Response("123", Result.ok("success"));

        String json = mapper.writeValueAsString(response);

        assertThat(json).isEqualTo("""
                {"id":"123","result":{"ok":true,"value":"success"}}\
                """);
    }

    @Test
    void givenResultInContainerWithError_whenSerialize_thenSerializesNestedStructuredError() {
        record Response(String id, Result<String> result) {}
        Response response = new Response("456", Result.errorAt("field", "Error message"));

        String json = mapper.writeValueAsString(response);

        assertThat(json).isEqualTo("""
                {"id":"456","result":{"ok":false,"errors":{"rootErrors":[],"fieldErrors":[{"key":["field"],"errors":[{"message":"Error message","code":"Error message","args":[]}]}]}}}\
                """);
    }

    // -- empty args handling --

    @Test
    void givenErrorWithEmptyArgs_whenSerialize_thenIncludesEmptyArgsArray() {
        Result<String> result = Result.error("Simple error");

        String json = mapper.writeValueAsString(result);

        // Verify that args is present even when empty
        assertThat(json).contains("\"args\":[]");
    }
}
