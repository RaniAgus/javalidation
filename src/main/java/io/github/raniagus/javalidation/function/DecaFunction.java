package io.github.raniagus.javalidation.function;

import java.util.Objects;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface DecaFunction<A extends @Nullable Object, B extends @Nullable Object, C extends @Nullable Object, D extends @Nullable Object, E extends @Nullable Object, F extends @Nullable Object, G extends @Nullable Object, H extends @Nullable Object, I extends @Nullable Object, J extends @Nullable Object, R extends @Nullable Object> {
    R apply(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j);

    default <V extends @Nullable Object> DecaFunction<A, B, C, D, E, F, G, H, I, J, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e, F f, G g, H h, I i, J j) -> after.apply(apply(a, b, c, d, e, f, g, h, i, j));
    }
}
