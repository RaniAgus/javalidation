package io.github.raniagus.javalidation.combiner;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.TetraFunction;

public record ResultCombiner4<T1, T2, T3, T4>(
        Result<T1> result1,
        Result<T2> result2,
        Result<T3> result3,
        Result<T4> result4
) {
    public <T5> ResultCombiner5<T1, T2, T3, T4, T5> and(Result<T5> result5) {
        return new ResultCombiner5<>(result1, result2, result3, result4, result5);
    }

    public <R> Result<R> combine(TetraFunction<T1, T2, T3, T4, R> onSuccess) {
        return Result.combine(
                () -> onSuccess.apply(
                        result1.getOrThrow(),
                        result2.getOrThrow(),
                        result3.getOrThrow(),
                        result4.getOrThrow()
                ),
                result1, result2, result3, result4
        );
    }
}
