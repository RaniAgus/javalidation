package io.github.raniagus.javalidation.assertj;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.ValidationErrors;
import org.assertj.core.api.AbstractAssert;

/**
 * AssertJ assertions for {@link Result}.
 *
 * <p>Obtain an instance via {@link JavalidationAssertions#assertThat(Result)}.
 *
 * <p>Example:
 * <pre>{@code
 * // Assert on Ok value
 * assertThat(result).isOk().isEqualTo(42);
 *
 * // Assert on Err contents
 * assertThat(result).isErr()
 *     .hasFieldError("age", "must be at least {0}", 18)
 *     .hasNoRootErrors();
 * }</pre>
 *
 * @param <T> the type of the success value
 */
public class ResultAssert<T> extends AbstractAssert<ResultAssert<T>, Result<T>> {

    ResultAssert(Result<T> actual) {
        super(actual, ResultAssert.class);
    }

    /**
     * Asserts that the result is a success ({@link Result.Ok}) and returns an
     * {@link OkResultAssert} scoped to the unwrapped value for further assertions.
     *
     * @throws AssertionError if the result is an {@link Result.Err}
     */
    public OkResultAssert<T> isOk() {
        isNotNull();
        return switch (actual) {
            case Result.Ok<T>(T value) -> new OkResultAssert<>(value);
            case Result.Err<T>(ValidationErrors errors) -> throw failure(
                    "Expected Result to be Ok but it was Err with errors:%n%s",
                    errors);
        };
    }

    /**
     * Asserts that the result is a failure ({@link Result.Err}) and returns a
     * {@link ValidationErrorsAssert} scoped to the errors for further assertions.
     *
     * @throws AssertionError if the result is a {@link Result.Ok}
     */
    public ValidationErrorsAssert isErr() {
        isNotNull();
        return switch (actual) {
            case Result.Ok<T>(T value) -> throw failure(
                    "Expected Result to be Err but it was Ok with value:%n%s",
                    value);
            case Result.Err<T>(ValidationErrors errors) -> new ValidationErrorsAssert(errors);
        };
    }
}
