package io.github.raniagus.javalidation.forcomprehension;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.SeptaFunction;

public class SeptaFor<A, B, C, D, E, F, G> {
  private final Result<A> result1;
  private final Result<B> result2;
  private final Result<C> result3;
  private final Result<D> result4;
  private final Result<E> result5;
  private final Result<F> result6;
  private final Result<G> result7;

  public SeptaFor(Result<A> result1, Result<B> result2, Result<C> result3, Result<D> result4, Result<E> result5, Result<F> result6, Result<G> result7) {
    this.result1 = result1;
    this.result2 = result2;
    this.result3 = result3;
    this.result4 = result4;
    this.result5 = result5;
    this.result6 = result6;
    this.result7 = result7;
  }

  public <R> Result<R> with(SeptaFunction<A, B, C, D, E, F, G, R> onSuccess) {
    return Result.merge(
        () -> onSuccess.apply(result1.getValue(), result2.getValue(), result3.getValue(), result4.getValue(), result5.getValue(), result6.getValue(), result7.getValue()),
        result1, result2, result3, result4, result5, result6, result7);
  }
}
