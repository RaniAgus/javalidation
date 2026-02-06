package io.github.raniagus.javalidation.combiner;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.HexFunction;

public record ResultCombiner6<T1, T2, T3, T4, T5, T6>(
        Result<T1> result1,
        Result<T2> result2,
        Result<T3> result3,
        Result<T4> result4,
        Result<T5> result5,
        Result<T6> result6
) {
    public <T7> ResultCombiner7<T1, T2, T3, T4, T5, T6, T7> and(Result<T7> result7) {
        return new ResultCombiner7<>(result1, result2, result3, result4, result5, result6, result7);
    }

    public <R> Result<R> combine(HexFunction<T1, T2, T3, T4, T5, T6, R> onSuccess) {
        return Result.combine(
                () -> onSuccess.apply(
                        result1.getOrThrow(),
                        result2.getOrThrow(),
                        result3.getOrThrow(),
                        result4.getOrThrow(),
                        result5.getOrThrow(),
                        result6.getOrThrow()
                ),
                result1, result2, result3, result4, result5, result6
        );
    }
}
