package io.github.raniagus.javalidation.combiner;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.TriFunction;

public record ResultCombiner3<T1, T2, T3>(
        Result<T1> result1,
        Result<T2> result2,
        Result<T3> result3
) {
    public <T4> ResultCombiner4<T1, T2, T3, T4> and(Result<T4> result4) {
        return new ResultCombiner4<>(result1, result2, result3, result4);
    }

    public <R> Result<R> combine(TriFunction<T1, T2, T3, R> onSuccess) {
        return Result.combine(
                () -> onSuccess.apply(
                        result1.getOrThrow(),
                        result2.getOrThrow(),
                        result3.getOrThrow()
                ),
                result1, result2, result3
        );
    }
}
