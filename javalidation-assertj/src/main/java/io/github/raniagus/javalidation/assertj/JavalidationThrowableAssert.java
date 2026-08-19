package io.github.raniagus.javalidation.assertj;

import io.github.raniagus.javalidation.JavalidationException;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.Assertions;

/**
 * AssertJ assertions for exceptions thrown by code under test.
 *
 * <p>Obtain an instance via {@link JavalidationAssertions#assertThatThrownBy(
 * org.assertj.core.api.ThrowableAssert.ThrowingCallable)}.
 */
public final class JavalidationThrowableAssert
        extends AbstractAssert<JavalidationThrowableAssert, Throwable> {

    JavalidationThrowableAssert(Throwable actual) {
        super(actual, JavalidationThrowableAssert.class);
    }

    /**
     * Asserts that the thrown exception is a {@link JavalidationException} and returns assertions
     * scoped to its validation errors.
     *
     * @return a {@link ValidationErrorsAssert} for the exception's errors
     * @throws AssertionError if the thrown exception is not a {@link JavalidationException}
     */
    public ValidationErrorsAssert isJavalidationException() {
        isNotNull();
        if (!(actual instanceof JavalidationException exception)) {
            throw failure("Expected thrown exception to be a JavalidationException but was %s",
                    actual.getClass().getName());
        }
        return new ValidationErrorsAssert(exception.getErrors());
    }

    /**
     * Asserts that the thrown exception is not a {@link JavalidationException} and returns
     * AssertJ's standard throwable assertion for further checks.
     *
     * @return AssertJ's standard assertion over the thrown exception
     * @throws AssertionError if the thrown exception is a {@link JavalidationException}
     */
    public AbstractThrowableAssert<?, Throwable> isNotJavalidationException() {
        isNotNull();
        if (actual instanceof JavalidationException) {
            throw failure("Expected thrown exception not to be a JavalidationException but it was one");
        }
        return Assertions.assertThat(actual);
    }
}
