package io.github.raniagus.javalidation.joiner;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.TetraFunction;

public class Joiner4<A, B, C, D> {
    private final Result<A> result1;
    private final Result<B> result2;
    private final Result<C> result3;
    private final Result<D> result4;

    public Joiner4(Result<A> result1, Result<B> result2, Result<C> result3, Result<D> result4) {
        this.result1 = result1;
        this.result2 = result2;
        this.result3 = result3;
        this.result4 = result4;
    }

    public <E> Joiner5<A, B, C, D, E> with(Result<E> result5) {
        return new Joiner5<>(result1, result2, result3, result4, result5);
    }

    public <R> Result<R> join(TetraFunction<A, B, C, D, R> onSuccess) {
        return Result.merge(
                () -> onSuccess.apply(result1.getOrThrow(), result2.getOrThrow(), result3.getOrThrow(), result4.getOrThrow()),
                result1, result2, result3, result4);
    }
}
