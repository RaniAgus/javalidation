package io.github.raniagus.javalidation.joiner;

import io.github.raniagus.javalidation.Result;
import java.util.function.BiFunction;

public class Joiner2<A, B> {
    private final Result<A> result1;
    private final Result<B> result2;

    public Joiner2(Result<A> result1, Result<B> result2) {
        this.result1 = result1;
        this.result2 = result2;
    }

    public <C> Joiner3<A, B, C> with(Result<C> result3) {
        return new Joiner3<>(result1, result2, result3);
    }

    public <R> Result<R> join(BiFunction<A, B, R> onSuccess) {
        return Result.merge(
                () -> onSuccess.apply(result1.getOrThrow(), result2.getOrThrow()),
                result1, result2);
    }
}
