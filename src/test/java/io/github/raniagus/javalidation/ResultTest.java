package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ResultTest {

    // -- static factory: of(Supplier) --

    @Test
    void givenSupplierReturningValue_whenOf_thenReturnsOk() {
        var result = Result.of(() -> 42);

        assertThat(result.getOrThrow()).isEqualTo(42);
    }

    @Test
    void givenSupplierThrowingValidationException_whenOf_thenReturnsErr() {
        var result = Result.of(() -> {
            throw new JavalidationException(ValidationErrors.of("error"));
        });

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    // -- static factory: of(Runnable) --

    @Test
    void givenRunnableSucceeding_whenOf_thenReturnsOk() {
        var result = Result.of(() -> {});

        assertThat(result.getOrThrow()).isEqualTo(null);
    }

    @Test
    void givenRunnableThrowingException_whenOf_thenReturnsErr() {
        var result = Result.of(this::raiseJavalidationException);

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    // -- static factory: combine --

    @Test
    void givenAllOkResults_whenCombine_thenReturnsOk() {
        var result = Result.combine(
                () -> 42,
                Result.ok(1),
                Result.ok(2),
                Result.ok(3)
        );

        assertThat(result.getOrThrow()).isEqualTo(42);
    }

    @Test
    void givenOneErrResult_whenCombine_thenReturnsErr() {
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
    void givenMultipleErrResults_whenCombine_thenAccumulatesErrors() {
        var result = Result.combine(
                () -> 42,
                Result.err("error1"),
                Result.err("field", "error2")
        );

        var errors = result.getErrors();
        assertThat(errors.rootErrors()).hasSize(1);
        assertThat(errors.fieldErrors()).hasSize(1);
    }

    // -- getOrThrow --

    @Test
    void givenOk_whenGetOrThrow_thenReturnsValue() {
        var result = Result.ok(42);

        assertThat(result.getOrThrow()).isEqualTo(42);
    }

    @Test
    void givenErr_whenGetOrThrow_thenThrowsValidationException() {
        var result = Result.<Integer>err("Invalid value");

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    // -- getErrors --

    @Test
    void givenOk_whenGetErrors_thenReturnsEmpty() {
        var result = Result.ok("value");

        assertThat(result.getErrors().isEmpty()).isTrue();
    }

    @Test
    void givenErr_whenGetErrors_thenReturnsErrors() {
        var result = Result.<String>err("field", "error message");

        var errors = result.getErrors();
        assertThat(errors.isEmpty()).isFalse();
        assertThat(errors.fieldErrors()).containsKey("field");
    }

    // -- getOrElse --

    @Test
    void givenOk_whenGetOrElse_thenReturnsValue() {
        var result = Result.ok(42);

        assertThat(result.getOrElse(100)).isEqualTo(42);
    }

    @Test
    void givenErr_whenGetOrElse_thenReturnsDefaultValue() {
        var result = Result.<Integer>err("invalid");

        assertThat(result.getOrElse(100)).isEqualTo(100);
    }

    @Test
    void givenErr_whenGetOrElseWithSupplier_thenCallsSupplier() {
        var result = Result.<Integer>err("invalid");

        assertThat(result.getOrElse(() -> 100)).isEqualTo(100);
    }

    // -- withPrefix --

    @Test
    void givenOk_whenWithPrefix_thenReturnsOkWithSameValue() {
        var result = Result.ok("value").withPrefix("prefix");

        assertThat(result.getOrThrow()).isEqualTo("value");
    }

    @Test
    void givenErr_whenWithPrefix_thenPrefixesErrorField() {
        var result = Result.<String>err("field", "error").withPrefix("root");

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey("root.field");
    }

    @Test
    void givenErr_whenWithPrefixVarargs_thenBuildsPrefix() {
        var result = Result.<String>err("field", "error").withPrefix("root", ".", "sub");

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey("root.sub.field");
    }

    // -- and --

    @Test
    void givenTwoOkResults_whenAnd_thenCombinesValues() {
        var result = Result.ok(5)
                .and(Result.ok("hello"))
                .combine((num, str) -> num + str.length());

        assertThat(result.getOrThrow()).isEqualTo(10);
    }

    @Test
    void givenFirstErr_whenAnd_thenPropagatesError() {
        var result = Result.<Integer>err("first error")
                .and(Result.ok("hello"))
                .combine((num, str) -> num + str.length());

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    @Test
    void givenSecondErr_whenAnd_thenPropagatesError() {
        var result = Result.ok(5)
                .and(Result.<String>err("second error"))
                .combine((num, str) -> num + str.length());

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    // -- map --

    @Test
    void givenOk_whenMap_thenTransformsValue() {
        var result = Result.ok(5).map(x -> x * 2);

        assertThat(result.getOrThrow()).isEqualTo(10);
    }

    @Test
    void givenErr_whenMap_thenPreservesError() {
        var result = Result.<Integer>err("invalid").map(x -> x * 2);

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    // -- mapErr --

    @Test
    void givenOk_whenMapErr_thenReturnsUnchanged() {
        var result = Result.ok("value").mapErr(errors -> errors);

        assertThat(result.getOrThrow()).isEqualTo("value");
    }

    @Test
    void givenErr_whenMapErr_thenTransformsErrors() {
        var originalError = Result.<String>err("field", "message");
        var result = originalError.mapErr(errors -> errors.withPrefix("prefix"));

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey("prefix.field");
    }

    // -- flatMap --

    @Test
    void givenOk_whenFlatMap_thenChainsResult() {
        var result = Result.ok(5).flatMap(x -> Result.ok(x * 2));

        assertThat(result.getOrThrow()).isEqualTo(10);
    }

    @Test
    void givenOk_whenFlatMapReturnsErr_thenPropagatesError() {
        var result = Result.ok(5).flatMap(x -> Result.err("failed"));

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    @Test
    void givenErr_whenFlatMap_thenSkipsFunctionAndPreservesError() {
        var result = Result.<Integer>err("initial error").flatMap(x -> Result.ok(x * 2));

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    // -- fold --

    @Test
    void givenOk_whenFold_thenAppliesSuccessFunction() {
        var result = Result.ok(10).fold(
                x -> "success: " + x,
                errors -> "failure"
        );

        assertThat(result).isEqualTo("success: 10");
    }

    @Test
    void givenErr_whenFold_thenAppliesFailureFunction() {
        var result = Result.<Integer>err("invalid").fold(
                x -> "success: " + x,
                errors -> "failure: " + errors.rootErrors().size()
        );

        assertThat(result).isEqualTo("failure: 1");
    }

    // -- check --

    @Test
    void givenOkAndValidPredicate_whenCheck_thenReturnsOk() {
        var result = Result.ok(42).check((value, validation) -> {
            if (value < 0) {
                validation.addRootError("Value must be positive");
            }
        });

        assertThat(result.getOrThrow()).isEqualTo(42);
    }

    @Test
    void givenOkAndFailingPredicate_whenCheck_thenReturnsErr() {
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
    void givenOkAndFieldError_whenCheck_thenAddsFieldError() {
        var result = Result.ok("test").check((value, validation) -> {
            if (value.length() < 5) {
                validation.addFieldError("username", "Username must be at least 5 characters");
            }
        });

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey("username");
    }

    @Test
    void givenErr_whenCheck_thenPreservesError() {
        var result = Result.<Integer>err("field", "initial error")
                .check((value, validation) -> {
                    validation.addRootError("This should not be executed");
                });

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey("field");
        assertThat(errors.rootErrors()).isEmpty();
    }

    @Test
    void givenOkAndMultipleFailingValidations_whenCheck_thenAccumulatesErrors() {
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

    // -- filter (root error) --

    @Test
    void givenOkAndPassingPredicate_whenFilter_thenReturnsOk() {
        var result = Result.ok(42).filter(x -> x > 0, "Value must be positive");

        assertThat(result.getOrThrow()).isEqualTo(42);
    }

    @Test
    void givenOkAndFailingPredicate_whenFilter_thenReturnsErr() {
        var result = Result.ok(-5).filter(x -> x > 0, "Value must be positive");

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
        assertThat(result.getErrors().rootErrors()).hasSize(1);
    }

    @Test
    void givenErr_whenFilter_thenPreservesError() {
        var result = Result.<Integer>err("initial", "error")
                .filter(x -> x > 0, "Value must be positive");

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey("initial");
    }

    // -- filter (field error) --

    @Test
    void givenOkAndPassingPredicate_whenFilterWithField_thenReturnsOk() {
        var result = Result.ok("hello").filter(
                s -> s.length() >= 5,
                "length",
                "Must be at least 5 characters"
        );

        assertThat(result.getOrThrow()).isEqualTo("hello");
    }

    @Test
    void givenOkAndFailingPredicate_whenFilterWithField_thenReturnsFieldErr() {
        var result = Result.ok("hi").filter(
                s -> s.length() >= 5,
                "username",
                "Must be at least 5 characters"
        );

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey("username");
    }

    private void raiseJavalidationException() {
        throw new JavalidationException("error");
    }
}
