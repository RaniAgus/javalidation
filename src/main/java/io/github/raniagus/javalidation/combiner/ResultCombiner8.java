package io.github.raniagus.javalidation.combiner;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.OctaFunction;

public record ResultCombiner8<T1, T2, T3, T4, T5, T6, T7, T8>(
        Result<T1> result1,
        Result<T2> result2,
        Result<T3> result3,
        Result<T4> result4,
        Result<T5> result5,
        Result<T6> result6,
        Result<T7> result7,
        Result<T8> result8
) {
    public <T9> ResultCombiner9<T1, T2, T3, T4, T5, T6, T7, T8, T9> and(Result<T9> result9) {
        return new ResultCombiner9<>(
                result1,
                result2,
                result3,
                result4,
                result5,
                result6,
                result7,
                result8,
                result9
        );
    }

    public <R> Result<R> combine(OctaFunction<T1, T2, T3, T4, T5, T6, T7, T8, R> onSuccess) {
        return Result.combine(
                () -> onSuccess.apply(
                        result1.getOrThrow(),
                        result2.getOrThrow(),
                        result3.getOrThrow(),
                        result4.getOrThrow(),
                        result5.getOrThrow(),
                        result6.getOrThrow(),
                        result7.getOrThrow(),
                        result8.getOrThrow()
                ),
                result1, result2, result3, result4, result5,
                result6, result7, result8
        );
    }
}
