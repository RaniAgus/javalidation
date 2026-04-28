package io.github.raniagus.javalidation.assertj;

import io.github.raniagus.javalidation.PartialResult;
import io.github.raniagus.javalidation.ValidationErrors;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.assertj.core.api.ObjectAssert;

/**
 * AssertJ assertions for {@link PartialResult}.
 *
 * <p>A {@link PartialResult} always carries both a partial success value and a (possibly empty)
 * set of validation errors — in contrast to {@link io.github.raniagus.javalidation.Result}, which
 * is either fully Ok or fully Err. This assert lets you inspect both parts independently.
 *
 * <p>Obtain an instance via {@link JavalidationAssertions#assertThat(PartialResult)}.
 *
 * <p>Example:
 * <pre>{@code
 * // Assert on errors only
 * assertThat(partial).hasErrors()
 *     .hasFieldError("email", "invalid format");
 *
 * // Assert no errors, then inspect the success value
 * assertThat(partial).hasNoErrors()
 *     .success()
 *     .isEqualTo(expectedValue);
 *
 * // Navigate to the equivalent Result
 * assertThat(partial).toResult()
 *     .isOk().hasValue(expectedValue);
 * }</pre>
 *
 * @param <T> the type of the partial success value
 */
public class PartialResultAssert<T> extends AbstractAssert<PartialResultAssert<T>, PartialResult<T>> {

    PartialResultAssert(PartialResult<T> actual) {
        super(actual, PartialResultAssert.class);
    }

    // -- error state --

    /**
     * Asserts that this {@link PartialResult} has at least one validation error and returns a
     * {@link ValidationErrorsAssert} scoped to those errors for further assertions.
     *
     * @throws AssertionError if there are no errors
     */
    public ValidationErrorsAssert hasErrors() {
        isNotNull();
        if (!actual.hasErrors()) {
            failWithMessage("Expected PartialResult to have errors but it had none");
        }
        return new ValidationErrorsAssert(actual.errors());
    }

    /**
     * Asserts that this {@link PartialResult} has no validation errors.
     *
     * @throws AssertionError if there are any errors
     */
    public PartialResultAssert<T> hasNoErrors() {
        isNotNull();
        if (actual.hasErrors()) {
            failWithMessage(
                    "Expected PartialResult to have no errors but found %d error(s):%n%s",
                    actual.errors().count(), actual.errors());
        }
        return this;
    }

    // -- direct access to errors and success value --

    /**
     * Returns a {@link ValidationErrorsAssert} scoped to the errors of this {@link PartialResult},
     * without asserting whether errors are present or absent.
     *
     * <p>Use this when you want to make assertions about the errors regardless of state, or when
     * you have already called {@link #hasErrors()} and want a fresh assert on the same errors.
     */
    public ValidationErrorsAssert errors() {
        isNotNull();
        return new ValidationErrorsAssert(actual.errors());
    }

    /**
     * Returns a plain {@link ObjectAssert} on the partial success value for further assertions.
     *
     * <pre>{@code
     * assertThat(partial).success().isEqualTo(expectedList);
     * }</pre>
     */
    public ObjectAssert<T> success() {
        isNotNull();
        return Assertions.assertThat(actual.success());
    }

    /**
     * Returns a narrowed assert on the partial success value using the given
     * {@link InstanceOfAssertFactory}.
     *
     * <pre>{@code
     * import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
     *
     * assertThat(partial).success(as(LIST)).hasSize(3);
     * }</pre>
     *
     * @param <ASSERT> the type of the returned assert
     * @param assertFactory the factory that narrows the value to a specific assert type
     */
    public <ASSERT extends AbstractAssert<?, ?>> ASSERT success(
            InstanceOfAssertFactory<? super T, ASSERT> assertFactory) {
        isNotNull();
        return Assertions.assertThat(actual.success()).asInstanceOf(assertFactory);
    }

    // -- conversion to Result --

    /**
     * Converts the {@link PartialResult} to a {@link io.github.raniagus.javalidation.Result} via
     * {@link PartialResult#toResult()} and returns a {@link ResultAssert} for further assertions.
     *
     * <pre>{@code
     * assertThat(partial).toResult().isOk().hasValue(expected);
     * assertThat(partial).toResult().isErr().hasRootError("...");
     * }</pre>
     */
    public ResultAssert<T> toResult() {
        isNotNull();
        return new ResultAssert<>(actual.toResult());
    }
}
