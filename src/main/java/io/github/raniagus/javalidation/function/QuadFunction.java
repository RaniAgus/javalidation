package io.github.raniagus.javalidation.function;

import java.util.Objects;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

/**
 * Functional interface for a function that accepts 4 arguments and produces a result.
 * <p>
 * This is an extension of Java's built-in functional interfaces ({@link Function}, {@link java.util.function.BiFunction})
 * to support higher arities needed for applicative validation combiners.
 * <p>
 * Example:
 * <pre>{@code
 * QuadFunction<A, B, C, D, String> formatter =
 *     (a, b, c, d) -> String.format("Combined result", a, b, c, d);
 * }</pre>
 *
 * @param <A> the type of the first argument
 * @param <B> the type of the second argument
 * @param <C> the type of the third argument
 * @param <D> the type of the fourth argument
 * @param <R> the type of the result
 * @see Function
 * @see java.util.function.BiFunction
 */
@FunctionalInterface
public interface QuadFunction<A extends @Nullable Object, B extends @Nullable Object, C extends @Nullable Object, D extends @Nullable Object, R extends @Nullable Object> {
    /**
     * Applies this function to the given arguments.
     *
     * @param a the first function argument
     * @param b the second function argument
     * @param c the third function argument
     * @param d the fourth function argument
     * @return the function result
     */
    R apply(A a, B b, C c, D d);

    /**
     * Returns a composed function that first applies this function to its input,
     * and then applies the {@code after} function to the result.
     *
     * @param after the function to apply after this function is applied
     * @param <V>   the type of output of the {@code after} function
     * @return a composed function
     * @throws NullPointerException if {@code after} is null
     */
    default <V extends @Nullable Object> QuadFunction<A, B, C, D, V> andThen(
            Function<? super @Nullable R, ? extends @Nullable V> after) {
        Objects.requireNonNull(after);
        return (a, b, c, d) -> after.apply(apply(a, b, c, d));
    }
}
