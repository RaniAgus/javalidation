package io.github.raniagus.javalidation.function;

import java.util.Objects;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface TriFunction<A extends @Nullable Object, B extends @Nullable Object, C extends @Nullable Object, R extends @Nullable Object> {
    R apply(A a, B b, C c);

    default <V extends @Nullable Object> TriFunction<A, B, C, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c) -> after.apply(apply(a, b, c));
    }
}
