package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JavalidationExceptionTest {

    @Test
    void givenSingleFieldError_whenGetMessage_thenReturnsErrorCount() {
        var exception = JavalidationException.ofField("email", "Invalid email format");

        assertThat(exception.getMessage()).isEqualTo("Validation failed with 1 error(s)");
    }

    @Test
    void givenSingleRootError_whenGetMessage_thenReturnsErrorCount() {
        var exception = JavalidationException.ofRoot("Something went wrong");

        assertThat(exception.getMessage()).isEqualTo("Validation failed with 1 error(s)");
    }

    @Test
    void givenMultipleErrors_whenGetMessage_thenReturnsTotalCount() {
        var validation = Validation.create();
        validation.addRootError("Invalid request");
        validation.addFieldError("name", "Name is required");
        validation.addFieldError("age", "Must be at least 18");
        validation.addFieldError("age", "Cannot be negative");

        var exception = JavalidationException.of(validation.finish());

        assertThat(exception.getMessage()).isEqualTo("Validation failed with 4 error(s)");
    }

    @Test
    void givenException_whenGetErrors_thenReturnsValidationErrors() {
        var errors = ValidationErrors.ofField("email", "Invalid format");
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
        var exception = JavalidationException.ofField("age", "Must be at least {0}", 18);

        assertThat(exception.getMessage()).isEqualTo("Validation failed with 1 error(s)");
    }

    @Test
    void givenMultipleFieldsWithMultipleErrors_whenGetMessage_thenReturnsTotalCount() {
        var validation = Validation.create();
        validation.addFieldError("email", "Required");
        validation.addFieldError("email", "Invalid format");
        validation.addFieldError("name", "Required");
        validation.addFieldError("age", "Too young");

        var exception = JavalidationException.of(validation.finish());

        assertThat(exception.getMessage()).isEqualTo("Validation failed with 4 error(s)");
    }
}
