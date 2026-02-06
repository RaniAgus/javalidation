package io.github.raniagus.javalidation.joiner;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.TriFunction;

public class Joiner3<A, B, C> {
    private final Result<A> result1;
    private final Result<B> result2;
    private final Result<C> result3;

    public Joiner3(Result<A> result1, Result<B> result2, Result<C> result3) {
        this.result1 = result1;
        this.result2 = result2;
        this.result3 = result3;
    }

    public <D> Joiner4<A, B, C, D> with(Result<D> result4) {
        return new Joiner4<>(result1, result2, result3, result4);
    }

    public <R> Result<R> join(TriFunction<A, B, C, R> onSuccess) {
        return Result.merge(
                () -> onSuccess.apply(result1.getOrThrow(), result2.getOrThrow(), result3.getOrThrow()),
                result1, result2, result3);
    }
}
