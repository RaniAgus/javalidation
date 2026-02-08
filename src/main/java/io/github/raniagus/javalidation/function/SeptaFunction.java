package io.github.raniagus.javalidation.function;

import java.util.Objects;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface SeptaFunction<A extends @Nullable Object, B extends @Nullable Object, C extends @Nullable Object, D extends @Nullable Object, E extends @Nullable Object, F extends @Nullable Object, G extends @Nullable Object, R extends @Nullable Object> {
    R apply(A a, B b, C c, D d, E e, F f, G g);

    default <V extends @Nullable Object> SeptaFunction<A, B, C, D, E, F, G, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e, F f, G g) -> after.apply(apply(a, b, c, d, e, f, g));
    }
}
