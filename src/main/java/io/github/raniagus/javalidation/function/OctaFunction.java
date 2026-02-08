package io.github.raniagus.javalidation.function;

import java.util.Objects;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface OctaFunction<A extends @Nullable Object, B extends @Nullable Object, C extends @Nullable Object, D extends @Nullable Object, E extends @Nullable Object, F extends @Nullable Object, G extends @Nullable Object, H extends @Nullable Object, R extends @Nullable Object> {
    R apply(A a, B b, C c, D d, E e, F f, G g, H h);

    default <V extends @Nullable Object> OctaFunction<A, B, C, D, E, F, G, H, V> andThen(Function<? super @Nullable R, ? extends @Nullable V> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e, F f, G g, H h) -> after.apply(apply(a, b, c, d, e, f, g, h));
    }
}
