package io.github.raniagus.javalidation.function;

import java.util.Objects;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface PentaFunction<A extends @Nullable Object, B extends @Nullable Object, C extends @Nullable Object, D extends @Nullable Object, E extends @Nullable Object, R extends @Nullable Object> {
    R apply(A a, B b, C c, D d, E e);

    default <V extends @Nullable Object> PentaFunction<A, B, C, D, E, V> andThen(Function<? super @Nullable R, ? extends @Nullable V> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e) -> after.apply(apply(a, b, c, d, e));
    }
}
