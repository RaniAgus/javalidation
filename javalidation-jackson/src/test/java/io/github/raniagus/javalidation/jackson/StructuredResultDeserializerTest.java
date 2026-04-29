package io.github.raniagus.javalidation.jackson;

import static io.github.raniagus.javalidation.assertj.JavalidationAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.ValidationErrors;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

class StructuredResultDeserializerTest {
    private JsonMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder()
                .addModule(JavalidationModule.getDefault())
                .build();
    }

    // -- deserialize Ok --

    @Test
    void givenOkJson_whenDeserialize_thenReconstructsOkResult() {
        String json = """
                {"ok":true,"value":"hello"}\
                """;

        Result<String> result = mapper.readValue(json, new TypeReference<>() {});

        assertThat(result).isOk().hasValue("hello");
    }

    @Test
    void givenOkWithInteger_whenDeserialize_thenReconstructsWithCorrectType() {
        String json = """
                {"ok":true,"value":42}\
                """;

        Result<Integer> result = mapper.readValue(json, new TypeReference<Result<Integer>>() {});

        assertThat(result).isOk().hasValue(42);
    }

    @Test
    void givenOkWithNull_whenDeserialize_thenReconstructsWithNullValue() {
        String json = """
                {"ok":true,"value":null}\
                """;

        Result<String> result = mapper.readValue(json, new TypeReference<>() {});

        assertThat(result).isOk().hasValue(null);
    }

    @Test
    void givenOkWithComplexObject_whenDeserialize_thenReconstructsNestedObject() {
        record Person(String name, int age) {}
        String json = """
                {"ok":true,"value":{"name":"Alice","age":30}}\
                """;

        Result<Person> result = mapper.readValue(json, new TypeReference<Result<Person>>() {});

        assertThat(result).isOk().hasValue(new Person("Alice", 30));
    }

    @Test
    void givenOkWithList_whenDeserialize_thenReconstructsListWithGenericType() {
        String json = """
                {"ok":true,"value":["a","b","c"]}\
                """;

        Result<List<String>> result = mapper.readValue(json, new TypeReference<>() {});

        assertThat(result).isOk().hasValue(List.of("a", "b", "c"));
    }

    // -- deserialize Err with structured format --

    @Test
    void givenErrWithRootError_whenDeserialize_thenReconstructsValidationErrors() {
        String json = """
                {"ok":false,"errors":{"rootErrors":[{"code":"Invalid input","args":[]}],"fieldErrors":[]}}\
                """;

        Result<String> result = mapper.readValue(json, new TypeReference<>() {});

        assertThat(result).isErr()
                .hasErrorCount(1)
                .hasRootError("Invalid input")
                .hasNoFieldErrors();
    }

    @Test
    void givenErrWithFieldError_whenDeserialize_thenReconstructsFieldErrors() {
        String json = """
                {"ok":false,"errors":{"rootErrors":[],"fieldErrors":[{"key":["email"],"errors":[{"code":"Invalid format","args":[]}]}]}}\
                """;

        Result<String> result = mapper.readValue(json, new TypeReference<>() {});

        assertThat(result).isErr()
                .hasErrorCount(1)
                .hasFieldError("email", "Invalid format")
                .hasNoRootErrors();
    }

    @Test
    void givenErrWithTemplateArgs_whenDeserialize_thenReconstructsTemplateAndArgs() {
        String json = """
                {"ok":false,"errors":{"rootErrors":[],"fieldErrors":[{"key":["age"],"errors":[{"code":"Must be at least {0}","args":[18]}]}]}}\
                """;

        Result<String> result = mapper.readValue(json, new TypeReference<>() {});

        assertThat(result).isErr()
                .hasErrorCount(1)
                .hasFieldError("age", "Must be at least {0}", 18);
    }

    @Test
    void givenErrWithMultipleErrors_whenDeserialize_thenReconstructsAllErrors() {
        String json = """
                {"ok":false,"errors":{"rootErrors":[{"code":"Global error","args":[]}],"fieldErrors":[{"key":["age"],"errors":[{"code":"Must be positive","args":[]}]},{"key":["name"],"errors":[{"code":"Required","args":[]}]}]}}\
                """;

        Result<String> result = mapper.readValue(json, new TypeReference<>() {});

        assertThat(result).isErr()
                .hasErrorCount(3)
                .hasRootError("Global error")
                .hasFieldKey("name")
                .hasFieldKey("age");
    }

    @Test
    void givenErrWithNestedFieldKeys_whenDeserialize_thenReconstructsFieldKeyParts() {
        String json = """
                {"ok":false,"errors":{"rootErrors":[],"fieldErrors":[{"key":["address",0,"street","city"],"errors":[{"code":"Invalid","args":[]}]}]}}\
                """;

        Result<String> result = mapper.readValue(json, new TypeReference<>() {});

        assertThat(result).isErr()
                .hasErrorCount(1)
                .hasFieldErrorAt(FieldKey.of("address", 0, "street", "city"), "Invalid");
    }

    @Test
    void givenErrWithMultipleErrorsOnSameField_whenDeserialize_thenReconstructsAllFieldErrors() {
        String json = """
                {"ok":false,"errors":{"rootErrors":[],"fieldErrors":[{"key":["password"],"errors":[{"code":"Required","args":[]},{"code":"Must be at least {0} characters","args":[8]},{"code":"Must contain a number","args":[]}]}]}}\
                """;

        Result<String> result = mapper.readValue(json, new TypeReference<>() {});

        assertThat(result).isErr()
                .hasErrorCount(3)
                .hasFieldError("password", "Required")
                .hasFieldError("password", "Must be at least {0} characters", 8)
                .hasFieldError("password", "Must contain a number");
    }

    // -- round-trip (serialize then deserialize) --

    @Test
    void givenSerializedOkResult_whenDeserialize_thenMatchesOriginal() {
        Result<String> original = Result.ok("test valid");

        String json = mapper.writeValueAsString(original);
        Result<String> deserialized = mapper.readValue(json, new TypeReference<>() {});

        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void givenSerializedErrResult_whenDeserialize_thenMatchesOriginal() {
        Result<String> original = Result.errorAt("field", "Error with {0} args", 2);

        String json = mapper.writeValueAsString(original);
        Result<String> deserialized = mapper.readValue(json, new TypeReference<>() {});

        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void givenComplexErrorStructure_whenRoundTrip_thenPreservesAllData() {
        ValidationErrors errors = ValidationErrors.empty()
                .mergeWith(ValidationErrors.of("Root error {0}", 1))
                .mergeWith(ValidationErrors.at("user", "Invalid").withPrefix("data", 0))
                .mergeWith(ValidationErrors.at("email", "Required"));
        Result<String> original = Result.error(errors);

        String json = mapper.writeValueAsString(original);
        Result<String> deserialized = mapper.readValue(json, new TypeReference<>() {});

        assertThat(deserialized).isEqualTo(original);
    }

    // -- nested in container --

    @Test
    void givenResultInContainer_whenDeserialize_thenReconstructsNestedResult() {
        record Response(String id, Result<String> result) {}
        String json = """
                {"id":"123","result":{"ok":true,"value":"success"}}\
                """;

        Response response = mapper.readValue(json, Response.class);

        assertThat(response.id()).isEqualTo("123");
        assertThat(response.result()).isOk().hasValue("success");
    }

    @Test
    void givenResultInContainerWithError_whenDeserialize_thenReconstructsNestedError() {
        record Response(String id, Result<String> result) {}
        String json = """
                {"id":"456","result":{"ok":false,"errors":{"rootErrors":[],"fieldErrors":[{"key":["field"],"errors":[{"code":"Error message","args":[]}]}]}}}\
                """;

        Response response = mapper.readValue(json, Response.class);

        assertThat(response.id()).isEqualTo("456");
        assertThat(response.result()).isErr()
                .hasErrorCount(1)
                .hasFieldKey("field");
    }
}
