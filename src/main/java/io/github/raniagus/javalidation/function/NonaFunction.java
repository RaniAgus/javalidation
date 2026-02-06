package io.github.raniagus.javalidation.function;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface NonaFunction<A, B, C, D, E, F, G, H, I, R> {
    R apply(A a, B b, C c, D d, E e, F f, G g, H h, I i);

    default <V> NonaFunction<A, B, C, D, E, F, G, H, I, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e, F f, G g, H h, I i) -> after.apply(apply(a, b, c, d, e, f, g, h, i));
    }
}
