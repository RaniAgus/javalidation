package io.github.raniagus.javalidation.forcomprehension;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.TetraFunction;

public class TetraFor<A, B, C, D> {
  private final Result<A> result1;
  private final Result<B> result2;
  private final Result<C> result3;
  private final Result<D> result4;

  public TetraFor(Result<A> result1, Result<B> result2, Result<C> result3, Result<D> result4) {
    this.result1 = result1;
    this.result2 = result2;
    this.result3 = result3;
    this.result4 = result4;
  }

  public <R> Result<R> with(TetraFunction<A, B, C, D, R> onSuccess) {
    return Result.merge(
        () -> onSuccess.apply(result1.getValue(), result2.getValue(), result3.getValue(), result4.getValue()),
        result1, result2, result3, result4);
  }
}
