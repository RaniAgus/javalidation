package io.github.raniagus.javalidation.joiner;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.OctaFunction;

public class Joiner8<A, B, C, D, E, F, G, H> {
    private final Result<A> result1;
    private final Result<B> result2;
    private final Result<C> result3;
    private final Result<D> result4;
    private final Result<E> result5;
    private final Result<F> result6;
    private final Result<G> result7;
    private final Result<H> result8;

    public Joiner8(Result<A> result1, Result<B> result2, Result<C> result3, Result<D> result4, Result<E> result5, Result<F> result6, Result<G> result7, Result<H> result8) {
        this.result1 = result1;
        this.result2 = result2;
        this.result3 = result3;
        this.result4 = result4;
        this.result5 = result5;
        this.result6 = result6;
        this.result7 = result7;
        this.result8 = result8;
    }

    public <I> Joiner9<A, B, C, D, E, F, G, H, I> with(Result<I> result9) {
        return new Joiner9<>(result1, result2, result3, result4, result5, result6, result7, result8, result9);
    }

    public <R> Result<R> join(OctaFunction<A, B, C, D, E, F, G, H, R> onSuccess) {
        return Result.merge(
                () -> onSuccess.apply(result1.getOrThrow(), result2.getOrThrow(), result3.getOrThrow(), result4.getOrThrow(), result5.getOrThrow(), result6.getOrThrow(), result7.getOrThrow(), result8.getOrThrow()),
                result1, result2, result3, result4, result5, result6, result7, result8);
    }
}
