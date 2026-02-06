package io.github.raniagus.javalidation.joiner;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.DecaFunction;

public class Joiner10<A, B, C, D, E, F, G, H, I, J> {
    private final Result<A> result1;
    private final Result<B> result2;
    private final Result<C> result3;
    private final Result<D> result4;
    private final Result<E> result5;
    private final Result<F> result6;
    private final Result<G> result7;
    private final Result<H> result8;
    private final Result<I> result9;
    private final Result<J> result10;

    public Joiner10(Result<A> result1, Result<B> result2, Result<C> result3, Result<D> result4, Result<E> result5, Result<F> result6, Result<G> result7, Result<H> result8, Result<I> result9, Result<J> result10) {
        this.result1 = result1;
        this.result2 = result2;
        this.result3 = result3;
        this.result4 = result4;
        this.result5 = result5;
        this.result6 = result6;
        this.result7 = result7;
        this.result8 = result8;
        this.result9 = result9;
        this.result10 = result10;
    }

    public <R> Result<R> join(DecaFunction<A, B, C, D, E, F, G, H, I, J, R> onSuccess) {
        return Result.merge(
                () -> onSuccess.apply(result1.getOrThrow(), result2.getOrThrow(), result3.getOrThrow(), result4.getOrThrow(), result5.getOrThrow(), result6.getOrThrow(), result7.getOrThrow(), result8.getOrThrow(), result9.getOrThrow(), result10.getOrThrow()),
                result1, result2, result3, result4, result5, result6, result7, result8, result9, result10);
    }
}
