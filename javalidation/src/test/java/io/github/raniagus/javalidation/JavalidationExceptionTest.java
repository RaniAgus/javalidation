package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JavalidationExceptionTest {

    @Test
    void givenSingleFieldError_whenGetMessage_thenReturnsErrorCount() {
        var exception = JavalidationException.at("email", "Invalid email format");

        assertThat(exception.getMessage()).isEqualTo("Validation failed with 1 error(s)");
    }

    @Test
    void givenSingleRootError_whenGetMessage_thenReturnsErrorCount() {
        var exception = JavalidationException.of("Something went wrong");

        assertThat(exception.getMessage()).isEqualTo("Validation failed with 1 error(s)");
    }

    @Test
    void givenMultipleErrors_whenGetMessage_thenReturnsTotalCount() {
        var validation = Validation.create();
        validation.addError("Invalid request");
        validation.addErrorAt("name", "Name is required");
        validation.addErrorAt("age", "Must be at least 18");
        validation.addErrorAt("age", "Cannot be negative");

        var exception = JavalidationException.of(validation.finish());

        assertThat(exception.getMessage()).isEqualTo("Validation failed with 4 error(s)");
    }

    @Test
    void givenException_whenGetErrors_thenReturnsValidationErrors() {
        var errors = ValidationErrors.at("email", "Invalid format");
        var exception = JavalidationException.of(errors);

        assertThat(exception.getErrors()).isEqualTo(errors);
    }

    @Test
    void givenEmptyErrors_whenGetMessage_thenReturnsZeroCount() {
        var exception = JavalidationException.of(ValidationErrors.empty());

        assertThat(exception.getMessage()).isEqualTo("Validation failed with 0 error(s)");
    }

    @Test
    void givenFieldErrorWithArgs_whenGetMessage_thenReturnsErrorCount() {
        var exception = JavalidationException.at("age", "Must be at least {0}", 18);

        assertThat(exception.getMessage()).isEqualTo("Validation failed with 1 error(s)");
    }

    @Test
    void givenMultipleFieldsWithMultipleErrors_whenGetMessage_thenReturnsTotalCount() {
        var validation = Validation.create();
        validation.addErrorAt("email", "Required");
        validation.addErrorAt("email", "Invalid format");
        validation.addErrorAt("name", "Required");
        validation.addErrorAt("age", "Too young");

        var exception = JavalidationException.of(validation.finish());

        assertThat(exception.getMessage()).isEqualTo("Validation failed with 4 error(s)");
    }
}
