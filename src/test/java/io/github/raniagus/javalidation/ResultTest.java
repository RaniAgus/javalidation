package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ResultTest {

    @Test
    void getOrThrow_withOk_returnsValue() {
        var result = Result.ok(42);

        assertThat(result.getOrThrow()).isEqualTo(42);
    }

    @Test
    void getOrThrow_withErr_throwsValidationException() {
        var result = Result.<Integer>err("Invalid value");

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getErrors_withOk_returnsEmpty() {
        var result = Result.ok("value");

        assertThat(result.getErrors().isEmpty()).isTrue();
    }

    @Test
    void getErrors_withErr_returnsErrors() {
        var result = Result.<String>err("field", "error message");

        var errors = result.getErrors();
        assertThat(errors.isEmpty()).isFalse();
        assertThat(errors.fieldErrors()).containsKey("field");
    }

    @Test
    void withPrefix_onOk_returnsOkWithSameValue() {
        var result = Result.ok("value").withPrefix("prefix");

        assertThat(result.getOrThrow()).isEqualTo("value");
    }

    @Test
    void withPrefix_onErr_prefixesErrorField() {
        var result = Result.<String>err("field", "error").withPrefix("root.");

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey("root.field");
    }

    @Test
    void withPrefix_objectVarargs_buildsPrefix() {
        var result = Result.<String>err("field", "error").withPrefix("root", ".", "sub");

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey("root.subfield");
    }

    @Test
    void map_withOk_transformsValue() {
        var result = Result.ok(5).map(x -> x * 2);

        assertThat(result.getOrThrow()).isEqualTo(10);
    }

    @Test
    void map_withErr_preservesError() {
        var result = Result.<Integer>err("invalid").map(x -> x * 2);

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void map_changesType() {
        var result = Result.ok(42).map(String::valueOf);

        assertThat(result.getOrThrow()).isEqualTo("42");
    }

    @Test
    void flatMap_withOk_chainsResult() {
        var result = Result.ok(5).flatMap(x -> Result.ok(x * 2));

        assertThat(result.getOrThrow()).isEqualTo(10);
    }

    @Test
    void flatMap_withOkReturningErr_propagatesError() {
        var result = Result.ok(5).flatMap(x -> Result.err("failed"));

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void flatMap_withErr_skipsFunctionAndPreservesError() {
        var result = Result.<Integer>err("initial error").flatMap(x -> Result.ok(x * 2));

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void mapErr_withOk_returnsUnchanged() {
        var result = Result.ok("value").mapErr(errors -> errors);

        assertThat(result.getOrThrow()).isEqualTo("value");
    }

    @Test
    void mapErr_withErr_transformsErrors() {
        var originalError = Result.<String>err("field", "message");
        var result = originalError.mapErr(errors -> errors.withPrefix("prefix."));

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey("prefix.field");
    }

    @Test
    void fold_withOk_appliesSuccessFunction() {
        var result = Result.ok(10).fold(
                x -> "success: " + x,
                errors -> "failure"
        );

        assertThat(result).isEqualTo("success: 10");
    }

    @Test
    void fold_withErr_appliesFailureFunction() {
        var result = Result.<Integer>err("invalid").fold(
                x -> "success: " + x,
                errors -> "failure: " + errors.rootErrors().size()
        );

        assertThat(result).isEqualTo("failure: 1");
    }

    @Test
    void fold_extractsDifferentType() {
        var result = Result.ok(42).fold(
                x -> x > 40,
                errors -> false
        );

        assertThat(result).isTrue();
    }

    @Test
    void getOrElse_withOk_returnsValue() {
        var result = Result.ok(42);

        assertThat(result.getOrElse(100)).isEqualTo(42);
    }

    @Test
    void getOrElse_withErr_returnsDefaultValue() {
        var result = Result.<Integer>err("invalid");

        assertThat(result.getOrElse(100)).isEqualTo(100);
    }

    @Test
    void getOrElse_withSupplier_returnsValue() {
        var result = Result.ok(42);

        assertThat(result.getOrElse(() -> 100)).isEqualTo(42);
    }

    @Test
    void getOrElse_withSupplier_callsSupplierOnErr() {
        var result = Result.<Integer>err("invalid");

        assertThat(result.getOrElse(() -> 100)).isEqualTo(100);
    }

    @Test
    void and_chainsMultipleResults() {
        var result = Result.ok(5)
                .and(Result.ok("hello"))
                .combine((num, str) -> num + str.length());

        assertThat(result.getOrThrow()).isEqualTo(10);
    }

    @Test
    void and_propagatesFirstError() {
        var result = Result.<Integer>err("first error")
                .and(Result.ok("hello"))
                .combine((num, str) -> num + str.length());

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void and_propagatesSecondError() {
        var result = Result.ok(5)
                .and(Result.<String>err("second error"))
                .combine((num, str) -> num + str.length());

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void of_withSupplierReturningValue_returnsOk() {
        var result = Result.of(() -> 42);

        assertThat(result.getOrThrow()).isEqualTo(42);
    }

    @Test
    void of_withSupplierThrowingValidationException_returnsErr() {
        var result = Result.of(() -> {
            throw new ValidationException(ValidationErrors.of("error"));
        });

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void combine_withAllOk_returnsOk() {
        var result = Result.combine(
                () -> 42,
                Result.ok(1),
                Result.ok(2),
                Result.ok(3)
        );

        assertThat(result.getOrThrow()).isEqualTo(42);
    }

    @Test
    void combine_withOneErr_returnsErr() {
        var result = Result.combine(
                () -> 42,
                Result.ok(1),
                Result.err("invalid"),
                Result.ok(3)
        );

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void combine_withMultipleErr_accumulates() {
        var result = Result.combine(
                () -> 42,
                Result.err("error1"),
                Result.err("field", "error2")
        );

        var errors = result.getErrors();
        assertThat(errors.rootErrors()).hasSize(1);
        assertThat(errors.fieldErrors()).hasSize(1);
    }
}
