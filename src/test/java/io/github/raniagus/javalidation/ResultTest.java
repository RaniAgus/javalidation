package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
                .isInstanceOf(JavalidationException.class);
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
                .isInstanceOf(JavalidationException.class);
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
                .isInstanceOf(JavalidationException.class);
    }

    @Test
    void flatMap_withErr_skipsFunctionAndPreservesError() {
        var result = Result.<Integer>err("initial error").flatMap(x -> Result.ok(x * 2));

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
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
                .isInstanceOf(JavalidationException.class);
    }

    @Test
    void and_propagatesSecondError() {
        var result = Result.ok(5)
                .and(Result.<String>err("second error"))
                .combine((num, str) -> num + str.length());

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    @Test
    void of_withSupplierReturningValue_returnsOk() {
        var result = Result.of(() -> 42);

        assertThat(result.getOrThrow()).isEqualTo(42);
    }

    @Test
    void of_withSupplierThrowingValidationException_returnsErr() {
        var result = Result.of(() -> {
            throw new JavalidationException(ValidationErrors.of("error"));
        });

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    @Test
    void of_withRunnableSucceeding_returnsOk() {
        var result = Result.of(() -> {});

        assertThat(result.getOrThrow()).isEqualTo(null);
    }

    @Test
    void of_withRunnableThrowingException_returnsErr() {
        var result = Result.of(this::raiseJavalidationException);

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
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
                .isInstanceOf(JavalidationException.class);
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

    @Test
    void check_withOkAndValidPredicate_returnsOk() {
        var result = Result.ok(42).check((value, validation) -> {
            if (value < 0) {
                validation.addRootError("Value must be positive");
            }
        });

        assertThat(result.getOrThrow()).isEqualTo(42);
    }

    @Test
    void check_withOkAndFailingPredicate_returnsErr() {
        var result = Result.ok(-5).check((value, validation) -> {
            if (value < 0) {
                validation.addRootError("Value must be positive");
            }
        });

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
        assertThat(result.getErrors().rootErrors()).hasSize(1);
    }

    @Test
    void check_withOkAndFieldError_addsFieldError() {
        var result = Result.ok("test").check((value, validation) -> {
            if (value.length() < 5) {
                validation.addFieldError("username", "Username must be at least 5 characters");
            }
        });

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey("username");
    }

    @Test
    void check_withErr_preservesError() {
        var result = Result.<Integer>err("field", "initial error")
                .check((value, validation) -> {
                    validation.addRootError("This should not be executed");
                });

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey("field");
        assertThat(errors.rootErrors()).isEmpty();
    }

    @Test
    void check_withMultipleValidations_accumulates() {
        var result = Result.ok(15).check((value, validation) -> {
            if (value < 10) {
                validation.addRootError("Value must be at least 10");
            }
            if (value > 100) {
                validation.addRootError("Value must not exceed 100");
            }
        });

        assertThat(result.getOrThrow()).isEqualTo(15);
    }

    @Test
    void check_withMultipleFailingValidations_accumulatesErrors() {
        var result = Result.ok(150).check((value, validation) -> {
            if (value > 10) {
                validation.addRootError("Value must not exceed 10");
            }
            if (value > 100) {
                validation.addRootError("Value must not exceed 100");
            }
        });

        assertThat(result.getErrors().rootErrors()).hasSize(2);
    }

    @Test
    void filter_withOkAndPassingPredicate_returnsOk() {
        var result = Result.ok(42).filter(x -> x > 0, "Value must be positive");

        assertThat(result.getOrThrow()).isEqualTo(42);
    }

    @Test
    void filter_withOkAndFailingPredicate_returnsErr() {
        var result = Result.ok(-5).filter(x -> x > 0, "Value must be positive");

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
        assertThat(result.getErrors().rootErrors()).hasSize(1);
    }

    @Test
    void filter_withOkAndFailingPredicateWithArgs_formatsMessage() {
        var result = Result.ok(-5).filter(
                x -> x > 0,
                "Value must be greater than {}",
                0
        );

        var errors = result.getErrors();
        assertThat(errors.rootErrors()).hasSize(1);
    }

    @Test
    void filter_withFieldAndPassingPredicate_returnsOk() {
        var result = Result.ok("hello").filter(
                s -> s.length() >= 5,
                "length",
                "Must be at least 5 characters"
        );

        assertThat(result.getOrThrow()).isEqualTo("hello");
    }

    @Test
    void filter_withFieldAndFailingPredicate_returnsFieldErr() {
        var result = Result.ok("hi").filter(
                s -> s.length() >= 5,
                "username",
                "Must be at least 5 characters"
        );

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey("username");
    }

    @Test
    void filter_withFieldAndFailingPredicateWithArgs_formatsMessage() {
        var result = Result.ok("hi").filter(
                s -> s.length() >= 5,
                "username",
                "Must be at least {} characters",
                5
        );

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey("username");
    }

    @Test
    void filter_withErr_preservesError() {
        var result = Result.<Integer>err("initial", "error")
                .filter(x -> x > 0, "Value must be positive");

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey("initial");
    }

    @Test
    void filter_canBeChained() {
        var result = Result.ok(42)
                .filter(x -> x > 0, "Value must be positive")
                .filter(x -> x < 100, "Value must be less than 100");

        assertThat(result.getOrThrow()).isEqualTo(42);
    }

    @Test
    void filter_chainedWithFailingFirstFilter_returnsErr() {
        var result = Result.ok(42)
                .filter(x -> x > 100, "Value must be greater than 100")
                .filter(x -> x < 50, "Value must be less than 50");

        assertThat(result.getErrors().rootErrors()).hasSize(1);
    }

    @Test
    void filter_chainedWithFailingSecondFilter_returnsErr() {
        var result = Result.ok(42)
                .filter(x -> x > 0, "Value must be positive")
                .filter(x -> x < 30, "Value must be less than 30");

        assertThat(result.getErrors().rootErrors()).hasSize(1);
    }

    @Test
    void filter_withFieldCanBeChained() {
        var result = Result.ok("hello")
                .filter(s -> s.length() >= 3, "length", "Too short")
                .filter(s -> s.length() <= 10, "length", "Too long");

        assertThat(result.getOrThrow()).isEqualTo("hello");
    }

    private void raiseJavalidationException() {
        throw new JavalidationException("error");
    }
}
