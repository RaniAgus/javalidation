package io.github.raniagus.javalidation.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.TemplateString;
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

        Result<String> result = mapper.readValue(json, new TypeReference<Result<String>>() {});

        assertThat(result).isInstanceOf(Result.Ok.class);
        assertThat(((Result.Ok<String>) result).value()).isEqualTo("hello");
    }

    @Test
    void givenOkWithInteger_whenDeserialize_thenReconstructsWithCorrectType() {
        String json = """
                {"ok":true,"value":42}\
                """;

        Result<Integer> result = mapper.readValue(json, new TypeReference<Result<Integer>>() {});

        assertThat(result).isInstanceOf(Result.Ok.class);
        assertThat(((Result.Ok<Integer>) result).value()).isEqualTo(42);
    }

    @Test
    void givenOkWithNull_whenDeserialize_thenReconstructsWithNullValue() {
        String json = """
                {"ok":true,"value":null}\
                """;

        Result<String> result = mapper.readValue(json, new TypeReference<Result<String>>() {});

        assertThat(result).isInstanceOf(Result.Ok.class);
        assertThat(((Result.Ok<String>) result).value()).isNull();
    }

    @Test
    void givenOkWithComplexObject_whenDeserialize_thenReconstructsNestedObject() {
        record Person(String name, int age) {}
        String json = """
                {"ok":true,"value":{"name":"Alice","age":30}}\
                """;

        Result<Person> result = mapper.readValue(json, new TypeReference<Result<Person>>() {});

        assertThat(result).isInstanceOf(Result.Ok.class);
        Person person = ((Result.Ok<Person>) result).value();
        assertThat(person.name()).isEqualTo("Alice");
        assertThat(person.age()).isEqualTo(30);
    }

    @Test
    void givenOkWithList_whenDeserialize_thenReconstructsListWithGenericType() {
        String json = """
                {"ok":true,"value":["a","b","c"]}\
                """;

        Result<List<String>> result = mapper.readValue(json, new TypeReference<Result<List<String>>>() {});

        assertThat(result).isInstanceOf(Result.Ok.class);
        List<String> list = ((Result.Ok<List<String>>) result).value();
        assertThat(list).containsExactly("a", "b", "c");
    }

    // -- deserialize Err with structured format --

    @Test
    void givenErrWithRootError_whenDeserialize_thenReconstructsValidationErrors() {
        String json = """
                {"ok":false,"errors":{"rootErrors":[{"message":"Invalid input","code":"Invalid input","args":[]}],"fieldErrors":[]}}\
                """;

        Result<String> result = mapper.readValue(json, new TypeReference<Result<String>>() {});

        assertThat(result).isInstanceOf(Result.Err.class);
        ValidationErrors errors = ((Result.Err) result).errors();
        assertThat(errors.rootErrors()).hasSize(1);
        assertThat(errors.rootErrors().getFirst().message()).isEqualTo("Invalid input");
        assertThat(errors.rootErrors().getFirst().args()).isEmpty();
        assertThat(errors.fieldErrors()).isEmpty();
    }

    @Test
    void givenErrWithFieldError_whenDeserialize_thenReconstructsFieldErrors() {
        String json = """
                {"ok":false,"errors":{"rootErrors":[],"fieldErrors":[{"key":["email"],"errors":[{"message":"Invalid format","code":"Invalid format","args":[]}]}]}}\
                """;

        Result<String> result = mapper.readValue(json, new TypeReference<Result<String>>() {});

        assertThat(result).isInstanceOf(Result.Err.class);
        ValidationErrors errors = ((Result.Err) result).errors();
        assertThat(errors.rootErrors()).isEmpty();
        assertThat(errors.fieldErrors()).hasSize(1);
        
        FieldKey emailKey = FieldKey.of("email");
        assertThat(errors.fieldErrors()).containsKey(emailKey);
        List<TemplateString> fieldErrors = errors.fieldErrors().get(emailKey);
        assertThat(fieldErrors).hasSize(1);
        assertThat(fieldErrors.getFirst().message()).isEqualTo("Invalid format");
    }

    @Test
    void givenErrWithTemplateArgs_whenDeserialize_thenReconstructsTemplateAndArgs() {
        String json = """
                {"ok":false,"errors":{"rootErrors":[],"fieldErrors":[{"key":["age"],"errors":[{"message":"Must be at least 18","code":"Must be at least {0}","args":[18]}]}]}}\
                """;

        Result<String> result = mapper.readValue(json, new TypeReference<Result<String>>() {});

        assertThat(result).isInstanceOf(Result.Err.class);
        ValidationErrors errors = ((Result.Err) result).errors();
        
        FieldKey ageKey = FieldKey.of("age");
        List<TemplateString> fieldErrors = errors.fieldErrors().get(ageKey);
        assertThat(fieldErrors).hasSize(1);
        
        TemplateString error = fieldErrors.getFirst();
        assertThat(error.message()).isEqualTo("Must be at least {0}");
        assertThat(error.args()).containsExactly(18);
    }

    @Test
    void givenErrWithMultipleErrors_whenDeserialize_thenReconstructsAllErrors() {
        String json = """
                {"ok":false,"errors":{"rootErrors":[{"message":"Global error","code":"Global error","args":[]}],"fieldErrors":[{"key":["age"],"errors":[{"message":"Must be positive","code":"Must be positive","args":[]}]},{"key":["name"],"errors":[{"message":"Required","code":"Required","args":[]}]}]}}\
                """;

        Result<String> result = mapper.readValue(json, new TypeReference<Result<String>>() {});

        assertThat(result).isInstanceOf(Result.Err.class);
        ValidationErrors errors = ((Result.Err) result).errors();
        
        assertThat(errors.rootErrors()).hasSize(1);
        assertThat(errors.rootErrors().getFirst().message()).isEqualTo("Global error");
        
        assertThat(errors.fieldErrors()).hasSize(2);
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("name"));
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("age"));
    }

    @Test
    void givenErrWithNestedFieldKeys_whenDeserialize_thenReconstructsFieldKeyParts() {
        String json = """
                {"ok":false,"errors":{"rootErrors":[],"fieldErrors":[{"key":["address",0,"street","city"],"errors":[{"message":"Invalid","code":"Invalid","args":[]}]}]}}\
                """;

        Result<String> result = mapper.readValue(json, new TypeReference<Result<String>>() {});

        assertThat(result).isInstanceOf(Result.Err.class);
        ValidationErrors errors = ((Result.Err) result).errors();
        
        FieldKey expectedKey = FieldKey.of("address", 0, "street", "city");
        assertThat(errors.fieldErrors()).containsKey(expectedKey);
        assertThat(errors.fieldErrors().get(expectedKey).getFirst().message()).isEqualTo("Invalid");
    }

    @Test
    void givenErrWithMultipleErrorsOnSameField_whenDeserialize_thenReconstructsAllFieldErrors() {
        String json = """
                {"ok":false,"errors":{"rootErrors":[],"fieldErrors":[{"key":["password"],"errors":[{"message":"Required","code":"Required","args":[]},{"message":"Must be at least 8 characters","code":"Must be at least {0} characters","args":[8]},{"message":"Must contain a number","code":"Must contain a number","args":[]}]}]}}\
                """;

        Result<String> result = mapper.readValue(json, new TypeReference<Result<String>>() {});

        assertThat(result).isInstanceOf(Result.Err.class);
        ValidationErrors errors = ((Result.Err) result).errors();
        
        FieldKey passwordKey = FieldKey.of("password");
        List<TemplateString> fieldErrors = errors.fieldErrors().get(passwordKey);
        assertThat(fieldErrors).hasSize(3);
        assertThat(fieldErrors.get(0).message()).isEqualTo("Required");
        assertThat(fieldErrors.get(1).message()).isEqualTo("Must be at least {0} characters");
        assertThat(fieldErrors.get(1).args()).containsExactly(8);
        assertThat(fieldErrors.get(2).message()).isEqualTo("Must contain a number");
    }

    // -- round-trip (serialize then deserialize) --

    @Test
    void givenSerializedOkResult_whenDeserialize_thenMatchesOriginal() {
        Result<String> original = Result.ok("test value");
        
        String json = mapper.writeValueAsString(original);
        Result<String> deserialized = mapper.readValue(json, new TypeReference<Result<String>>() {});

        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void givenSerializedErrResult_whenDeserialize_thenMatchesOriginal() {
        Result<String> original = Result.errorAt("field", "Error with {0} args", 2);
        
        String json = mapper.writeValueAsString(original);
        Result<String> deserialized = mapper.readValue(json, new TypeReference<Result<String>>() {});

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
        Result<String> deserialized = mapper.readValue(json, new TypeReference<Result<String>>() {});

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
        assertThat(response.result()).isInstanceOf(Result.Ok.class);
        assertThat(((Result.Ok<String>) response.result()).value()).isEqualTo("success");
    }

    @Test
    void givenResultInContainerWithError_whenDeserialize_thenReconstructsNestedError() {
        record Response(String id, Result<String> result) {}
        String json = """
                {"id":"456","result":{"ok":false,"errors":{"rootErrors":[],"fieldErrors":[{"key":["field"],"errors":[{"message":"Error message","code":"Error message","args":[]}]}]}}}\
                """;

        Response response = mapper.readValue(json, Response.class);

        assertThat(response.id()).isEqualTo("456");
        assertThat(response.result()).isInstanceOf(Result.Err.class);
        ValidationErrors errors = ((Result.Err) response.result()).errors();
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("field"));
    }
}
