package io.github.raniagus.javalidation.assertj;

import io.github.raniagus.javalidation.PartialResult;
import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
import java.util.Objects;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

/**
 * Entry point for all Javalidation AssertJ assertions.
 *
 * <p>Typical usage (static import):
 * <pre>{@code
 * import static io.github.raniagus.javalidation.assertj.JavalidationAssertions.assertThat;
 *
 * // Result assertions
 * assertThat(result).isOk().isEqualTo("expected");
 * assertThat(result).isErr()
 *     .hasRootError("must not be null")
 *     .hasFieldError("email", "invalid format");
 *
 * // ValidationErrors assertions
 * assertThat(errors).isEmpty();
 * assertThat(errors).hasFieldError("name", "must not be blank");
 *
 * // Validation (mutable builder) assertions — calls finish() internally
 * assertThat(validation).hasRootError("must not be null");
 *
 * // PartialResult assertions
 * assertThat(partial).hasErrors()
 *     .hasFieldError("email", "invalid format");
 * assertThat(partial).hasNoErrors()
 *     .success().isEqualTo(expected);
 *
 * // Thrown validation exceptions
 * assertThatThrownBy(validation::check)
 *     .isJavalidationException()
 *     .hasFieldError("email", "invalid format");
 * }</pre>
 */
public final class JavalidationAssertions {

    private JavalidationAssertions() {}

    /**
     * Creates a {@link ResultAssert} for the given {@link Result}.
     *
     * @param actual the result to assert on
     * @param <T>    the success value type
     * @return a new {@link ResultAssert}
     */
    public static <T> ResultAssert<T> assertThat(Result<T> actual) {
        return new ResultAssert<>(actual);
    }

    /**
     * Creates a {@link ValidationErrorsAssert} for the given {@link ValidationErrors}.
     *
     * @param actual the validation errors to assert on
     * @return a new {@link ValidationErrorsAssert}
     */
    public static ValidationErrorsAssert assertThat(ValidationErrors actual) {
        return new ValidationErrorsAssert(actual);
    }

    /**
     * Creates a {@link ValidationErrorsAssert} from the given {@link Validation} by calling
     * {@link Validation#finish()} to obtain the current snapshot of accumulated errors.
     *
     * <p>The {@code Validation} instance is not modified; this is a read-only snapshot.
     *
     * <pre>{@code
     * Validation validation = Validation.create()
     *         .addError("must not be null")
     *         .addErrorAt("email", "invalid format");
     *
     * assertThat(validation)
     *         .hasRootError("must not be null")
     *         .hasFieldError("email", "invalid format");
     * }</pre>
     *
     * @param actual the validation builder to assert on
     * @return a new {@link ValidationErrorsAssert} over {@code actual.finish()}
     */
    public static ValidationErrorsAssert assertThat(Validation actual) {
        return new ValidationErrorsAssert(actual == null ? null : actual.finish());
    }

    /**
     * Creates a {@link PartialResultAssert} for the given {@link PartialResult}.
     *
     * @param actual the partial result to assert on
     * @param <T>    the partial success value type
     * @return a new {@link PartialResultAssert}
     */
    public static <T> PartialResultAssert<T> assertThat(PartialResult<T> actual) {
        return new PartialResultAssert<>(actual);
    }

    /**
     * Executes {@code callable}, asserting that it throws an exception.
     *
     * <p>Call {@link JavalidationThrowableAssert#isJavalidationException()} to verify that the
     * exception is a {@code JavalidationException} and continue with validation-error assertions.
     *
     * @param callable code expected to throw
     * @return an assertion over the thrown exception
     * @throws AssertionError if {@code callable} completes normally
     */
    public static JavalidationThrowableAssert assertThatThrownBy(ThrowingCallable callable) {
        Objects.requireNonNull(callable, "callable must not be null");
        try {
            callable.call();
        } catch (Throwable throwable) {
            return new JavalidationThrowableAssert(throwable);
        }
        throw new AssertionError("Expecting code to raise a throwable.");
    }
}
