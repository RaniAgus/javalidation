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
            throw JavalidationException.of("error");
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
                Result.error("invalid"),
                Result.ok(3)
        );

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    @Test
    void givenMultipleErrResults_whenCombine_thenAccumulatesErrors() {
        var result = Result.combine(
                () -> 42,
                Result.error("error1"),
                Result.errorAt("field", "error2")
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
        var result = Result.<Integer>error("Invalid value");

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
        var result = Result.<String>errorAt("field", "error message");

        var errors = result.getErrors();
        assertThat(errors.isEmpty()).isFalse();
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("field"));
    }

    // -- getOrElse --

    @Test
    void givenOk_whenGetOrElse_thenReturnsValue() {
        var result = Result.ok(42);

        assertThat(result.getOrElse(100)).isEqualTo(42);
    }

    @Test
    void givenOk_whenGetOrElseWithSupplier_thenReturnsValueWithoutCallingSupplier() {
        var callCount = new int[1];
        var result = Result.ok(42).getOrElse(() -> {
            callCount[0]++;
            return 100;
        });

        assertThat(result).isEqualTo(42);
        assertThat(callCount[0]).isEqualTo(0);
    }

    @Test
    void givenErr_whenGetOrElse_thenReturnsDefaultValue() {
        var result = Result.<Integer>error("invalid");

        assertThat(result.getOrElse(100)).isEqualTo(100);
    }

    @Test
    void givenErr_whenGetOrElseWithSupplier_thenCallsSupplier() {
        var result = Result.<Integer>error("invalid");

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
        var result = Result.<String>errorAt("field", "error").withPrefix("root");

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("root", "field"));
    }

    @Test
    void givenOk_whenWithPrefixVarargs_thenReturnsOkWithSameValue() {
        var result = Result.ok("value").withPrefix("prefix", ".", "sub");

        assertThat(result.getOrThrow()).isEqualTo("value");
    }

    @Test
    void givenErr_whenWithPrefixVarargs_thenBuildsPrefix() {
        var result = Result.<String>errorAt("field", "error").withPrefix("root", "sub");

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("root", "sub", "field"));
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
        var result = Result.<Integer>error("first error")
                .and(Result.ok("hello"))
                .combine((num, str) -> num + str.length());

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    @Test
    void givenSecondErr_whenAnd_thenPropagatesError() {
        var result = Result.ok(5)
                .and(Result.<String>error("second error"))
                .combine((num, str) -> num + str.length());

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    // -- or(Supplier) --

    @Test
    void givenOkResult_whenOrWithSupplier_thenReturnsOkWithoutCallingSupplier() {
        var callCount = new int[1];
        var result = Result.ok(42).or(() -> {
            callCount[0]++;
            return Result.ok(100);
        });

        assertThat(result.getOrThrow()).isEqualTo(42);
        assertThat(callCount[0]).isEqualTo(0);
    }

    @Test
    void givenErrResult_whenOrWithSupplier_thenCallsSupplierAndReturnsOk() {
        var result = Result.<Integer>error("initial error")
                .or(() -> Result.ok(100));

        assertThat(result.getOrThrow()).isEqualTo(100);
    }

    @Test
    void givenErrResult_whenOrWithSupplierReturningErr_thenMergesErrors() {
        var result = Result.<Integer>errorAt("first", "error1")
                .or(() -> Result.errorAt("second", "error2"));

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).hasSize(2);
        assertThat(errors.fieldErrors()).containsKeys(FieldKey.of("first"), FieldKey.of("second"));
    }

    @Test
    void givenMultipleErrResults_whenOrChained_thenAppliesFallbackChain() {
        var result = Result.<String>error("cache-miss")
                .or(() -> Result.error("db-miss"))
                .or(() -> Result.ok("default-value"));

        assertThat(result.getOrThrow()).isEqualTo("default-value");
    }

    // -- or(Result) --

    @Test
    void givenOkResult_whenOrWithEagerResult_thenReturnsOk() {
        var result = Result.ok(42)
                .or(Result.ok(100));

        assertThat(result.getOrThrow()).isEqualTo(42);
    }

    @Test
    void givenErrResult_whenOrWithEagerResult_thenReturnsEagerOk() {
        var result = Result.<Integer>error("initial error")
                .or(Result.ok(100));

        assertThat(result.getOrThrow()).isEqualTo(100);
    }

    @Test
    void givenErrResult_whenOrWithEagerErr_thenMergesErrors() {
        var result = Result.<Integer>errorAt("field1", "error1")
                .or(Result.errorAt("field2", "error2"));

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).hasSize(2);
        assertThat(errors.fieldErrors()).containsKeys(FieldKey.of("field1"), FieldKey.of("field2"));
    }

    // -- map --

    @Test
    void givenOk_whenMap_thenTransformsValue() {
        var result = Result.ok(5).map(x -> x * 2);

        assertThat(result.getOrThrow()).isEqualTo(10);
    }

    @Test
    void givenErr_whenMap_thenPreservesError() {
        var result = Result.<Integer>error("invalid").map(x -> x * 2);

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    @Test
    void givenOk_whenMapThrowsJavalidationException_thenCatchesAndReturnsErr() {
        var result = Result.ok(5).map(x -> {
            throw JavalidationException.of("error in mapper");
        });

        assertThat(result).isInstanceOf(Result.Err.class);
        var errors = result.getErrors();
        assertThat(errors.rootErrors()).hasSize(1);
        assertThat(errors.rootErrors().get(0).message()).isEqualTo("error in mapper");
    }

    @Test
    void givenOk_whenMapThrowsOtherException_thenPropagatesException() {
        assertThatThrownBy(() -> Result.ok(5).map(x -> {
            throw new IllegalStateException("unexpected error");
        }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unexpected error");
    }

    // -- mapErr --

    @Test
    void givenOk_whenMapErr_thenReturnsUnchanged() {
        var result = Result.ok("value").mapErr(errors -> errors);

        assertThat(result.getOrThrow()).isEqualTo("value");
    }

    @Test
    void givenErr_whenMapErr_thenTransformsErrors() {
        var originalError = Result.<String>errorAt("field", "message");
        var result = originalError.mapErr(errors -> errors.withPrefix("prefix"));

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("prefix", "field"));
    }

    // -- bimap --

    @Test
    void givenOk_whenBimap_thenAppliesSuccessFunction() {
        var result = Result.ok(10).bimap(
                x -> x * 2,
                errors -> errors.withPrefix("should-not-be-called")
        );

        assertThat(result.getOrThrow()).isEqualTo(20);
    }

    @Test
    void givenErr_whenBimap_thenAppliesErrorFunction() {
        var result = Result.<Integer>errorAt("age", "invalid")
                .bimap(
                        x -> x * 2,
                        errors -> errors.withPrefix("user")
                );

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("user", "age"));
        assertThat(errors.fieldErrors()).doesNotContainKey(FieldKey.of("age"));
    }

    // -- peek --

    @Test
    void givenOk_whenPeek_thenExecutesActionAndReturnsSameInstance() {
        var box = new int[1];
        var ok = Result.ok(42);

        var result = ok.peek(value -> box[0] = value);

        assertThat(result).isSameAs(ok);
        assertThat(box[0]).isEqualTo(42);
    }

    @Test
    void givenErr_whenPeek_thenSkipsAction() {
        var box = new int[1];
        var result = Result.<Integer>error("error")
                .peek(value -> box[0] = value);

        assertThat(box[0]).isEqualTo(0);
        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    // -- peekErr --

    @Test
    void givenErr_whenPeekErr_thenExecutesActionAndReturnsSameInstance() {
        var box = new int[1];
        var error = Result.<Integer>error("field", "error");

        var result = error.peekErr(errors -> box[0] = errors.count());

        assertThat(result).isSameAs(error);
        assertThat(box[0]).isEqualTo(1);
    }

    @Test
    void givenOk_whenPeekErr_thenSkipsAction() {
        var box = new int[1];
        var result = Result.ok(42)
                .peekErr(errors -> box[0] = errors.count());

        assertThat(box[0]).isEqualTo(0);
        assertThat(result.getOrThrow()).isEqualTo(42);
    }

    // -- flatMap --

    @Test
    void givenOk_whenFlatMap_thenChainsResult() {
        var result = Result.ok(5).flatMap(x -> Result.ok(x * 2));

        assertThat(result.getOrThrow()).isEqualTo(10);
    }

    @Test
    void givenOk_whenFlatMapReturnsErr_thenPropagatesError() {
        var result = Result.ok(5).flatMap(x -> Result.error("failed"));

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    @Test
    void givenErr_whenFlatMap_thenSkipsFunctionAndPreservesError() {
        var result = Result.<Integer>error("initial error").flatMap(x -> Result.ok(x * 2));

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    @Test
    void givenOk_whenFlatMapThrowsJavalidationException_thenCatchesAndReturnsErr() {
        var result = Result.ok(5).flatMap(x -> {
            throw JavalidationException.of("error in flatMap");
        });

        assertThat(result).isInstanceOf(Result.Err.class);
        var errors = result.getErrors();
        assertThat(errors.rootErrors()).hasSize(1);
        assertThat(errors.rootErrors().get(0).message()).isEqualTo("error in flatMap");
    }

    @Test
    void givenOk_whenFlatMapThrowsOtherException_thenPropagatesException() {
        assertThatThrownBy(() -> Result.ok(5).flatMap(x -> {
            throw new NullPointerException("unexpected error");
        }))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("unexpected error");
    }

    // -- flatMapErr --

    @Test
    void givenOk_whenFlatMapErr_thenSkipsFunctionAndReturnsOk() {
        var ok = Result.ok(42);

        var result = ok.flatMapErr(errors -> Result.ok(100));

        assertThat(result).isSameAs(ok);
        assertThat(result.getOrThrow()).isEqualTo(42);
    }

    @Test
    void givenErr_whenFlatMapErrRecoversWithOk_thenReturnsOk() {
        var result = Result.<Integer>error("cache-miss")
                .flatMapErr(errors -> Result.ok(100));

        assertThat(result.getOrThrow()).isEqualTo(100);
    }

    @Test
    void givenErr_whenFlatMapErrReturnsAnotherErr_thenReturnsNewErr() {
        var result = Result.<Integer>errorAt("first", "error1")
                .flatMapErr(errors -> Result.errorAt("second", "error2"));

        var resultErrors = result.getErrors();
        assertThat(resultErrors.fieldErrors()).containsKey(FieldKey.of("second"));
        assertThat(resultErrors.fieldErrors()).doesNotContainKey(FieldKey.of("first"));
    }

    @Test
    void givenErr_whenFlatMapErrChained_thenAppliesFallbackChain() {
        var result = Result.<String>error("Cache miss")
                .flatMapErr(e1 -> Result.error("Database miss"))
                .flatMapErr(e2 -> Result.ok("Default value"));

        assertThat(result.getOrThrow()).isEqualTo("Default value");
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
        var result = Result.<Integer>error("invalid").fold(
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
                validation.addError("Value must be positive");
            }
        });

        assertThat(result.getOrThrow()).isEqualTo(42);
    }

    @Test
    void givenOkAndFailingPredicate_whenCheck_thenReturnsErr() {
        var result = Result.ok(-5).check((value, validation) -> {
            if (value < 0) {
                validation.addError("Value must be positive");
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
                validation.addErrorAt("username", "Username must be at least 5 characters");
            }
        });

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("username"));
    }

    @Test
    void givenErr_whenCheck_thenPreservesError() {
        var result = Result.<Integer>errorAt("field", "initial error")
                .check((value, validation) -> {
                    validation.addError("This should not be executed");
                });

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("field"));
        assertThat(errors.rootErrors()).isEmpty();
    }

    @Test
    void givenOkAndMultipleFailingValidations_whenCheck_thenAccumulatesErrors() {
        var result = Result.ok(150).check((value, validation) -> {
            if (value > 10) {
                validation.addError("Value must not exceed 10");
            }
            if (value > 100) {
                validation.addError("Value must not exceed 100");
            }
        });

        assertThat(result.getErrors().rootErrors()).hasSize(2);
    }

    // -- ensure --

    @Test
    void givenOkAndPassingPredicate_whenFilter_thenReturnsOk() {
        var result = Result.ok(42).ensure(x -> x > 0, "Value must be positive");

        assertThat(result.getOrThrow()).isEqualTo(42);
    }

    @Test
    void givenOkAndFailingPredicate_whenFilter_thenReturnsErr() {
        var result = Result.ok(-5).ensure(x -> x > 0, "Value must be positive");

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
        assertThat(result.getErrors().rootErrors()).hasSize(1);
    }

    @Test
    void givenErr_whenFilter_thenPreservesError() {
        var result = Result.<Integer>errorAt("initial", "error")
                .ensure(x -> x > 0, "Value must be positive");

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("initial"));
    }

    // -- ensureAt --

    @Test
    void givenOkAndPassingPredicate_whenFilterWithField_thenReturnsOk() {
        var result = Result.ok("hello").ensureAt(
                s -> s.length() >= 5,
                "length",
                "Must be at least 5 characters"
        );

        assertThat(result.getOrThrow()).isEqualTo("hello");
    }

    @Test
    void givenOkAndFailingPredicate_whenFilterWithField_thenReturnsFieldErr() {
        var result = Result.ok("hi").ensureAt(
                s -> s.length() >= 5,
                "username",
                "Must be at least 5 characters"
        );

        var errors = result.getErrors();
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("username"));
    }

    private void raiseJavalidationException() {
        throw JavalidationException.of("error");
    }
}
