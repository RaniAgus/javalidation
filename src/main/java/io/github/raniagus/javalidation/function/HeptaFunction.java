package io.github.raniagus.javalidation.function;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface HeptaFunction<A, B, C, D, E, F, G, R> {
  R apply(A a, B b, C c, D d, E e, F f, G g);

  default <V> HeptaFunction<A, B, C, D, E, F, G, V> andThen(Function<? super R, ? extends V> after) {
    Objects.requireNonNull(after);
    return (A a, B b, C c, D d, E e, F f, G g) -> after.apply(apply(a, b, c, d, e, f, g));
  }
}
