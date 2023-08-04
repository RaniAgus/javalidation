package io.github.raniagus.javalidation.forcomprehension;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.TriFunction;

public class TriFor<A, B, C> {
  private final Result<A> result1;
  private final Result<B> result2;
  private final Result<C> result3;

  public TriFor(Result<A> result1, Result<B> result2, Result<C> result3) {
    this.result1 = result1;
    this.result2 = result2;
    this.result3 = result3;
  }

  public <R> Result<R> with(TriFunction<A, B, C, R> onSuccess) {
    return Result.merge(
        () -> onSuccess.apply(result1.getValue(), result2.getValue(), result3.getValue()),
        result1, result2, result3);
  }
}
