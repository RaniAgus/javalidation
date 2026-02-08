package io.github.raniagus.javalidation.function;

import java.util.Objects;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

/**
 * Functional interface for a function that accepts 8 arguments and produces a result.
 * <p>
 * This is an extension of Java's built-in functional interfaces ({@link Function}, {@link java.util.function.BiFunction})
 * to support higher arities needed for applicative validation combiners.
 * <p>
 * Example:
 * <pre>{@code
 * OctaFunction<A, B, C, D, E, F, G, H, String> formatter =
 *     (a, b, c, d, e, f, g, h) -> String.format("Combined result", a, b, c, d, e, f, g, h);
 * }</pre>
 *
 * @param <A> <B> <C> <D> <E> <F> <G> <H>> the types of the input arguments
 * @param <R> the type of the result
 * @see Function
 * @see java.util.function.BiFunction
 */
@FunctionalInterface
public interface OctaFunction<A extends @Nullable Object, B extends @Nullable Object, C extends @Nullable Object, D extends @Nullable Object, E extends @Nullable Object, F extends @Nullable Object, G extends @Nullable Object, H extends @Nullable Object, R extends @Nullable Object> {
    /**
     * Applies this function to the given arguments.
     *
     * @param a, b, c, d, e, f, g, h the function arguments
     * @return the function result
     */
    R apply(A a, B b, C c, D d, E e, F f, G g, H h);

    /**
     * Returns a composed function that first applies this function to its input,
     * and then applies the {@code after} function to the result.
     *
     * @param after the function to apply after this function is applied
     * @param <V>   the type of output of the {@code after} function
     * @return a composed function
     * @throws NullPointerException if {@code after} is null
     */
    default <V extends @Nullable Object> OctaFunction<A, B, C, D, E, F, G, H, V> andThen(
            Function<? super @Nullable R, ? extends @Nullable V> after) {
        Objects.requireNonNull(after);
        return (a, b, c, d, e, f, g, h) -> after.apply(apply(a, b, c, d, e, f, g, h));
    }
}
