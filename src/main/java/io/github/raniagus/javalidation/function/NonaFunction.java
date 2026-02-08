package io.github.raniagus.javalidation.function;

import java.util.Objects;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

/**
 * Functional interface for a function that accepts 9 arguments and produces a result.
 * <p>
 * This is an extension of Java's built-in functional interfaces ({@link Function}, {@link java.util.function.BiFunction})
 * to support higher arities needed for applicative validation combiners.
 * <p>
 * Example:
 * <pre>{@code
 * NonaFunction<A, B, C, D, E, F, G, H, I, String> formatter =
 *     (a, b, c, d, e, f, g, h, i) -> String.format("Combined result", a, b, c, d, e, f, g, h, i);
 * }</pre>
 *
 * @param <A> the type of the first argument
 * @param <B> the type of the second argument
 * @param <C> the type of the third argument
 * @param <D> the type of the fourth argument
 * @param <E> the type of the fifth argument
 * @param <F> the type of the sixth argument
 * @param <G> the type of the seventh argument
 * @param <H> the type of the eighth argument
 * @param <I> the type of the ninth argument
 * @param <R> the type of the result
 * @see Function
 * @see java.util.function.BiFunction
 */
@FunctionalInterface
public interface NonaFunction<A extends @Nullable Object, B extends @Nullable Object, C extends @Nullable Object, D extends @Nullable Object, E extends @Nullable Object, F extends @Nullable Object, G extends @Nullable Object, H extends @Nullable Object, I extends @Nullable Object, R extends @Nullable Object> {
    /**
     * Applies this function to the given arguments.
     *
     * @param a the first function argument
     * @param b the second function argument
     * @param c the third function argument
     * @param d the fourth function argument
     * @param e the fifth function argument
     * @param f the sixth function argument
     * @param g the seventh function argument
     * @param h the eighth function argument
     * @param i the ninth function argument
     * @return the function result
     */
    R apply(A a, B b, C c, D d, E e, F f, G g, H h, I i);

    /**
     * Returns a composed function that first applies this function to its input,
     * and then applies the {@code after} function to the result.
     *
     * @param after the function to apply after this function is applied
     * @param <V>   the type of output of the {@code after} function
     * @return a composed function
     * @throws NullPointerException if {@code after} is null
     */
    default <V extends @Nullable Object> NonaFunction<A, B, C, D, E, F, G, H, I, V> andThen(
            Function<? super @Nullable R, ? extends @Nullable V> after) {
        Objects.requireNonNull(after);
        return (a, b, c, d, e, f, g, h, i) -> after.apply(apply(a, b, c, d, e, f, g, h, i));
    }
}
