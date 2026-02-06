package io.github.raniagus.javalidation.combiner;

import io.github.raniagus.javalidation.Result;
import java.util.function.BiFunction;

public record ResultCombiner2<T1, T2>(
        Result<T1> result1,
        Result<T2> result2
) {
    public <T3> ResultCombiner3<T1, T2, T3> and(Result<T3> result3) {
        return new ResultCombiner3<>(result1, result2, result3);
    }

    public <R> Result<R> combine(BiFunction<T1, T2, R> onSuccess) {
        return Result.combine(
                () -> onSuccess.apply(
                        result1.getOrThrow(),
                        result2.getOrThrow()
                ),
                result1, result2
        );
    }
}
