package io.github.raniagus.javalidation.function;

import java.util.Objects;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

/**
 * Functional interface for a function that accepts 10 arguments and produces a result.
 * <p>
 * This is an extension of Java's built-in functional interfaces ({@link Function}, {@link java.util.function.BiFunction})
 * to support higher arities needed for applicative validation combiners.
 * <p>
 * Example:
 * <pre>{@code
 * DecaFunction<A, B, C, D, E, F, G, H, I, J, String> formatter =
 *     (a, b, c, d, e, f, g, h, i, j) -> String.format("Combined result", a, b, c, d, e, f, g, h, i, j);
 * }</pre>
 *
 * @param <A> <B> <C> <D> <E> <F> <G> <H> <I> <J>> the types of the input arguments
 * @param <R> the type of the result
 * @see Function
 * @see java.util.function.BiFunction
 */
@FunctionalInterface
public interface DecaFunction<A extends @Nullable Object, B extends @Nullable Object, C extends @Nullable Object, D extends @Nullable Object, E extends @Nullable Object, F extends @Nullable Object, G extends @Nullable Object, H extends @Nullable Object, I extends @Nullable Object, J extends @Nullable Object, R extends @Nullable Object> {
    /**
     * Applies this function to the given arguments.
     *
     * @param a, b, c, d, e, f, g, h, i, j the function arguments
     * @return the function result
     */
    R apply(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j);

    /**
     * Returns a composed function that first applies this function to its input,
     * and then applies the {@code after} function to the result.
     *
     * @param after the function to apply after this function is applied
     * @param <V>   the type of output of the {@code after} function
     * @return a composed function
     * @throws NullPointerException if {@code after} is null
     */
    default <V extends @Nullable Object> DecaFunction<A, B, C, D, E, F, G, H, I, J, V> andThen(
            Function<? super @Nullable R, ? extends @Nullable V> after) {
        Objects.requireNonNull(after);
        return (a, b, c, d, e, f, g, h, i, j) -> after.apply(apply(a, b, c, d, e, f, g, h, i, j));
    }
}
