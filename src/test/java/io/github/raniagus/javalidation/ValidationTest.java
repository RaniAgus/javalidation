package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ValidationTest {

    @Test
    void addRootError_singleError() {
        var validation = Validation.create()
                .addRootError("error message");

        var errors = validation.finish();
        assertThat(errors.rootErrors()).containsExactly("error message");
    }

    @Test
    void addRootError_multipleErrors() {
        var validation = Validation.create()
                .addRootError("error 1")
                .addRootError("error 2")
                .addRootError("error 3");

        var errors = validation.finish();
        assertThat(errors.rootErrors()).containsExactly("error 1", "error 2", "error 3");
    }

    @Test
    void addRootError_nullMessage_throwsNullPointerException() {
        var validation = Validation.create();

        assertThatThrownBy(() -> validation.addRootError(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void addRootErrors_emptyList_doesNothing() {
        var validation = Validation.create()
                .addRootErrors(java.util.List.of());

        var errors = validation.finish();
        assertThat(errors.rootErrors()).isEmpty();
    }

    @Test
    void addRootErrors_multipleMessages() {
        var validation = Validation.create()
                .addRootErrors(java.util.List.of("error 1", "error 2", "error 3"));

        var errors = validation.finish();
        assertThat(errors.rootErrors()).containsExactly("error 1", "error 2", "error 3");
    }

    @Test
    void addRootErrors_nullList_throwsNullPointerException() {
        var validation = Validation.create();

        assertThatThrownBy(() -> validation.addRootErrors(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void addFieldError_singleError() {
        var validation = Validation.create()
                .addFieldError("field", "error message");

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsKey("field");
        assertThat(errors.fieldErrors().get("field")).containsExactly("error message");
    }

    @Test
    void addFieldError_multipleErrorsSameField() {
        var validation = Validation.create()
                .addFieldError("field", "error 1")
                .addFieldError("field", "error 2");

        var errors = validation.finish();
        assertThat(errors.fieldErrors().get("field")).containsExactly("error 1", "error 2");
    }

    @Test
    void addFieldError_multipleDifferentFields() {
        var validation = Validation.create()
                .addFieldError("field1", "error 1")
                .addFieldError("field2", "error 2");

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsOnlyKeys("field1", "field2");
    }

    @Test
    void addFieldError_nullField_throwsNullPointerException() {
        var validation = Validation.create();

        assertThatThrownBy(() -> validation.addFieldError(null, "error"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void addFieldError_nullMessage_throwsNullPointerException() {
        var validation = Validation.create();

        assertThatThrownBy(() -> validation.addFieldError("field", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void addFieldErrors_withMessages() {
        var validation = Validation.create()
                .addFieldErrors("field", java.util.List.of("error 1", "error 2"));

        var errors = validation.finish();
        assertThat(errors.fieldErrors().get("field")).containsExactly("error 1", "error 2");
    }

    @Test
    void addFieldErrors_emptyList_doesNotCreateEntry() {
        var validation = Validation.create()
                .addFieldErrors("field", java.util.List.of());

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).doesNotContainKey("field");
    }

    @Test
    void addFieldErrors_nullField_throwsNullPointerException() {
        var validation = Validation.create();

        assertThatThrownBy(() -> validation.addFieldErrors(null, java.util.List.of("error")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void addFieldErrors_nullMessages_throwsNullPointerException() {
        var validation = Validation.create();

        assertThatThrownBy(() -> validation.addFieldErrors("field", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void addFieldErrors_withMap() {
        var fieldErrors = java.util.Map.of(
                "field1", java.util.List.of("error 1"),
                "field2", java.util.List.of("error 2", "error 3")
        );
        var validation = Validation.create()
                .addFieldErrors(fieldErrors);

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsOnlyKeys("field1", "field2");
        assertThat(errors.fieldErrors().get("field1")).containsExactly("error 1");
        assertThat(errors.fieldErrors().get("field2")).containsExactly("error 2", "error 3");
    }

    @Test
    void addFieldErrors_nullMap_throwsNullPointerException() {
        var validation = Validation.create();

        assertThatThrownBy(() -> validation.addFieldErrors((java.util.Map<String, java.util.List<String>>) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void addAll_withValidationErrors() {
        var validationErrors = ValidationErrors.of("field", "error");
        var validation = Validation.create()
                .addAll(validationErrors);

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsKey("field");
    }

    @Test
    void addAll_nullValidationErrors_throwsNullPointerException() {
        var validation = Validation.create();

        assertThatThrownBy(() -> validation.addAll((ValidationErrors) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void addAll_withPrefix_addsPrefix() {
        var validationErrors = ValidationErrors.of("field", "error");
        var validation = Validation.create()
                .addAll("root", validationErrors);

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsKey("root.field");
    }

    @Test
    void addAll_withPrefix_multipleFieldErrors() {
        var fieldErrors = java.util.Map.of(
                "field1", java.util.List.of("error 1"),
                "field2", java.util.List.of("error 2")
        );
        var validationErrors = new ValidationErrors(java.util.List.of(), fieldErrors);
        var validation = Validation.create()
                .addAll("prefix", validationErrors);

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsOnlyKeys("prefix.field1", "prefix.field2");
    }

    @Test
    void addAll_withPrefix_rootErrors() {
        var validationErrors = ValidationErrors.of(java.util.List.of("root error 1", "root error 2"));
        var validation = Validation.create()
                .addAll("prefix", validationErrors);

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsKey("prefix");
    }

    @Test
    void addAll_withPrefix_nullPrefix_throwsNullPointerException() {
        var validation = Validation.create();
        var validationErrors = ValidationErrors.of("error");

        assertThatThrownBy(() -> validation.addAll(null, validationErrors))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void addAll_withPrefix_nullValidationErrors_throwsNullPointerException() {
        var validation = Validation.create();

        assertThatThrownBy(() -> validation.addAll("prefix", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void finish_returnsValidationErrors() {
        var validation = Validation.create()
                .addFieldError("field", "error");

        var errors = validation.finish();
        assertThat(errors).isNotNull();
        assertThat(errors.fieldErrors()).containsKey("field");
    }

    @Test
    void finish_emptyValidation_returnsEmpty() {
        var validation = Validation.create();

        var errors = validation.finish();
        assertThat(errors.isEmpty()).isTrue();
    }

    @Test
    void asResult_withErrors_returnsErr() {
        var validation = Validation.create()
                .addFieldError("field", "error");

        var result = validation.asResult(() -> "value");
        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void asResult_withoutErrors_returnsOk() {
        var validation = Validation.create();

        var result = validation.asResult(() -> "value");
        assertThat(result.getOrThrow()).isEqualTo("value");
    }

    @Test
    void asResult_supplierThrowsValidationException_returnsErr() {
        var validation = Validation.create();

        var result = validation.asResult(() -> {
            throw new ValidationException(ValidationErrors.of("error"));
        });
        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void check_withErrors_throwsValidationException() {
        var validation = Validation.create()
                .addFieldError("field", "error");

        assertThatThrownBy(validation::check)
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void check_withoutErrors_doesNotThrow() {
        var validation = Validation.create();

        assertThatCode(validation::check).doesNotThrowAnyException();
    }

    @Test
    void checkAndGet_withoutErrors_returnsValue() {
        var validation = Validation.create();

        var result = validation.checkAndGet(() -> "value");
        assertThat(result).isEqualTo("value");
    }

    @Test
    void checkAndGet_withErrors_throwsValidationException() {
        var validation = Validation.create()
                .addFieldError("field", "error");

        assertThatThrownBy(() -> validation.checkAndGet(() -> "value"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void fluent_chainMultipleCalls() {
        var validation = Validation.create()
                .addRootError("root error")
                .addFieldError("field1", "error 1")
                .addFieldError("field2", "error 2")
                .addRootErrors(java.util.List.of("root error 2"));

        var errors = validation.finish();
        assertThat(errors.rootErrors()).hasSize(2);
        assertThat(errors.fieldErrors()).containsOnlyKeys("field1", "field2");
    }

    @Test
    void create_returnsIndependentInstances() {
        var validation1 = Validation.create().addRootError("error 1");
        var validation2 = Validation.create().addRootError("error 2");

        var errors1 = validation1.finish();
        var errors2 = validation2.finish();

        assertThat(errors1.rootErrors()).containsExactly("error 1");
        assertThat(errors2.rootErrors()).containsExactly("error 2");
    }
}
