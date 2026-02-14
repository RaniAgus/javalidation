package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.raniagus.javalidation.format.TemplateString;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidationTest {

    // -- addRootError --

    @Test
    void givenMessage_whenAddRootError_thenAddsError() {
        var validation = Validation.create()
                .addRootError("error message");

        var errors = validation.finish();
        assertThat(errors.rootErrors()).containsExactly(TemplateString.of("error message"));
    }

    @Test
    void givenMultipleCalls_whenAddRootError_thenAccumulatesErrors() {
        var validation = Validation.create()
                .addRootError("error 1")
                .addRootError("error 2");

        var errors = validation.finish();
        assertThat(errors.rootErrors()).containsExactly(
                TemplateString.of("error 1"),
                TemplateString.of("error 2")
        );
    }

    @Test
    void givenNullMessage_whenAddRootError_thenThrowsNullPointerException() {
        var validation = Validation.create();

        assertThatThrownBy(() -> validation.addRootError(null))
                .isInstanceOf(NullPointerException.class);
    }

    // -- addFieldError --

    @Test
    void givenFieldAndMessage_whenAddFieldError_thenAddsError() {
        var validation = Validation.create()
                .addFieldError("field", "error message");

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsKey("field");
        assertThat(errors.fieldErrors().get("field")).containsExactly(TemplateString.of("error message"));
    }

    @Test
    void givenSameFieldMultipleTimes_whenAddFieldError_thenAccumulatesErrors() {
        var validation = Validation.create()
                .addFieldError("field", "error 1")
                .addFieldError("field", "error 2");

        var errors = validation.finish();
        assertThat(errors.fieldErrors().get("field")).containsExactly(
                TemplateString.of("error 1"),
                TemplateString.of("error 2")
        );
    }

    @Test
    void givenDifferentFields_whenAddFieldError_thenAddsToEachField() {
        var validation = Validation.create()
                .addFieldError("field1", "error 1")
                .addFieldError("field2", "error 2");

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsOnlyKeys("field1", "field2");
    }

    @Test
    void givenNullField_whenAddFieldError_thenThrowsNullPointerException() {
        var validation = Validation.create();

        assertThatThrownBy(() -> validation.addFieldError(null, "error"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void givenNullMessage_whenAddFieldError_thenThrowsNullPointerException() {
        var validation = Validation.create();

        assertThatThrownBy(() -> validation.addFieldError("field", null))
                .isInstanceOf(NullPointerException.class);
    }

    // -- addAll(Validation) --

    @Test
    void givenValidation_whenAddAll_thenAddsAllErrors() {
        var validation = Validation.create()
                .addFieldError("field", "error");
        var validation2 = Validation.create()
                .addRootError("root error");

        var validation3 = Validation.create()
                .addAll(validation)
                .addAll(validation2);

        var errors = validation3.finish();
        assertThat(errors.fieldErrors()).containsEntry("field", List.of(TemplateString.of("error")));
        assertThat(errors.rootErrors()).containsExactly(TemplateString.of("root error"));
    }

    // -- addAll(ValidationErrors) --

    @Test
    void givenValidationErrors_whenAddAll_thenAddsAllErrors() {
        var validationErrors = ValidationErrors.ofField("field", "error");
        var validation = Validation.create()
                .addAll(validationErrors);

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsKey("field");
    }

    @Test
    void givenNull_whenAddAll_thenThrowsNullPointerException() {
        var validation = Validation.create();

        assertThatThrownBy(() -> validation.addAll((ValidationErrors) null))
                .isInstanceOf(NullPointerException.class);
    }

    // -- addAll(String, ValidationErrors) --

    @Test
    void givenFieldErrors_whenAddAllWithPrefix_thenPrefixesFieldNames() {
        var validationErrors = ValidationErrors.ofField("field", "error");
        var validation = Validation.create()
                .addAll(validationErrors, new StringBuilder("root"));

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsKey("root.field");
    }

    @Test
    void givenRootErrors_whenAddAllWithPrefix_thenConvertsToFieldErrors() {
        var validationErrors = ValidationErrors.ofRoot("root error");
        var validation = Validation.create()
                .addAll(validationErrors, new StringBuilder("prefix"));

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsKey("prefix");
    }

    // -- finish --

    @Test
    void givenErrors_whenFinish_thenReturnsValidationErrors() {
        var validation = Validation.create()
                .addFieldError("field", "error");

        var errors = validation.finish();
        assertThat(errors).isNotNull();
        assertThat(errors.fieldErrors()).containsKey("field");
    }

    @Test
    void givenNoErrors_whenFinish_thenReturnsEmpty() {
        var validation = Validation.create();

        var errors = validation.finish();
        assertThat(errors.isEmpty()).isTrue();
    }

    // -- asResult --

    @Test
    void givenNoErrors_whenAsResult_thenReturnsOk() {
        var validation = Validation.create();

        var result = validation.asResult(() -> "value");
        assertThat(result.getOrThrow()).isEqualTo("value");
    }

    @Test
    void givenErrors_whenAsResult_thenReturnsErr() {
        var validation = Validation.create()
                .addFieldError("field", "error");

        var result = validation.asResult(() -> "value");
        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    @Test
    void givenSupplierThrows_whenAsResult_thenReturnsErr() {
        var validation = Validation.create();

        var result = validation.asResult(() -> {
            throw JavalidationException.ofRoot("error");
        });
        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    // -- check --

    @Test
    void givenNoErrors_whenCheck_thenDoesNotThrow() {
        var validation = Validation.create();

        assertThatCode(validation::check).doesNotThrowAnyException();
    }

    @Test
    void givenErrors_whenCheck_thenThrowsValidationException() {
        var validation = Validation.create()
                .addFieldError("field", "error");

        assertThatThrownBy(validation::check)
                .isInstanceOf(JavalidationException.class);
    }

    // -- checkAndGet --

    @Test
    void givenNoErrors_whenCheckAndGet_thenReturnsValue() {
        var validation = Validation.create();

        var result = validation.checkAndGet(() -> "value");
        assertThat(result).isEqualTo("value");
    }

    @Test
    void givenErrors_whenCheckAndGet_thenThrowsValidationException() {
        var validation = Validation.create()
                .addFieldError("field", "error");

        assertThatThrownBy(() -> validation.checkAndGet(() -> "value"))
                .isInstanceOf(JavalidationException.class);
    }
}
