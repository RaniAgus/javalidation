package io.github.raniagus.javalidation.combiner;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.PentaFunction;

public record ResultCombiner5<T1, T2, T3, T4, T5>(
        Result<T1> result1,
        Result<T2> result2,
        Result<T3> result3,
        Result<T4> result4,
        Result<T5> result5
) {
    public <T6> ResultCombiner6<T1, T2, T3, T4, T5, T6> and(Result<T6> result6) {
        return new ResultCombiner6<>(result1, result2, result3, result4, result5, result6);
    }

    public <R> Result<R> combine(PentaFunction<T1, T2, T3, T4, T5, R> onSuccess) {
        return Result.combine(
                () -> onSuccess.apply(
                        result1.getOrThrow(),
                        result2.getOrThrow(),
                        result3.getOrThrow(),
                        result4.getOrThrow(),
                        result5.getOrThrow()
                ),
                result1, result2, result3, result4, result5
        );
    }
}
