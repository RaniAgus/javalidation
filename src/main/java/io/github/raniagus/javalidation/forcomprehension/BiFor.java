package io.github.raniagus.javalidation.forcomprehension;

import io.github.raniagus.javalidation.Result;
import java.util.function.BiFunction;

public class BiFor<A, B> {
  private final Result<A> result1;
  private final Result<B> result2;

  public BiFor(Result<A> result1, Result<B> result2) {
    this.result1 = result1;
    this.result2 = result2;
  }

  public <R> Result<R> with(BiFunction<A, B, R> onSuccess) {
    return Result.merge(
        () -> onSuccess.apply(result1.getValue(), result2.getValue()),
        result1, result2);
  }
}
