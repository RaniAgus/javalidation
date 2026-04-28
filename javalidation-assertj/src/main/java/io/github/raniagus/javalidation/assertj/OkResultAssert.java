package io.github.raniagus.javalidation.assertj;

import io.github.raniagus.javalidation.Result;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.assertj.core.api.ObjectAssert;

/**
 * AssertJ assertions scoped to the success value of an {@link Result.Ok}.
 *
 * <p>Obtained via {@link ResultAssert#isOk()}. Delegates all standard object assertions to the
 * unwrapped value, enabling fluent value-level assertions without manually calling
 * {@link Result#getOrThrow()}.
 *
 * <p>Example:
 * <pre>{@code
 * assertThat(result).isOk().isEqualTo(42);
 * assertThat(result).isOk().satisfies(v -> assertThat(v).isGreaterThan(0));
 *
 * // Unwrap to a plain ObjectAssert for further chaining
 * assertThat(result).isOk().get().asString().startsWith("hello");
 *
 * // Narrow to a specific type using InstanceOfAssertFactory (mirrors Optional assertions)
 * assertThat(result).isOk().get(as(String.class)).startsWith("hello");
 * assertThat(result).isOk().get(as(InstanceOfAssertFactories.LIST)).hasSize(3);
 * }</pre>
 *
 * @param <T> the type of the success value
 */
public class OkResultAssert<T> extends AbstractObjectAssert<OkResultAssert<T>, T> {

    OkResultAssert(T actual) {
        super(actual, OkResultAssert.class);
    }

    /**
     * Asserts that the Ok value equals {@code expected}.
     * Convenience alias to keep existing {@code assertThat(result.getOrThrow()).isEqualTo(...)}
     * patterns as a single fluent chain.
     */
    public OkResultAssert<T> hasValue(T expected) {
        get().isEqualTo(expected);
        return this;
    }

    /**
     * Returns a plain {@link ObjectAssert} on the unwrapped Ok value for further assertions.
     *
     * <p>Mirrors {@code AbstractOptionalAssert.get()}: useful when you need standard AssertJ
     * methods that are not exposed on {@link OkResultAssert} itself.
     *
     * <pre>{@code
     * assertThat(result).isOk().get().asString().startsWith("hello");
     * }</pre>
     */
    public ObjectAssert<T> get() {
        return Assertions.assertThat(actual);
    }

    /**
     * Returns a narrowed assert on the unwrapped Ok value using the given
     * {@link InstanceOfAssertFactory}.
     *
     * <p>Mirrors {@code AbstractOptionalAssert.get(InstanceOfAssertFactory)}: useful for
     * type-narrowing without an explicit cast.
     *
     * <pre>{@code
     * import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
     * import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
     *
     * assertThat(result).isOk().get(as(STRING)).startsWith("hello");
     * assertThat(result).isOk().get(as(LIST)).hasSize(3);
     * }</pre>
     *
     * @param <ASSERT> the type of the returned assert
     * @param assertFactory the factory that narrows the value to a specific assert type
     */
    public <ASSERT extends AbstractAssert<?, ?>> ASSERT get(
            InstanceOfAssertFactory<? super T, ASSERT> assertFactory) {
        return get().asInstanceOf(assertFactory);
    }
}
