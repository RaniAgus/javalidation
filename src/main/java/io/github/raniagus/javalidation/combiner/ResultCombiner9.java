package io.github.raniagus.javalidation.combiner;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.NonaFunction;
import org.jspecify.annotations.Nullable;

public record ResultCombiner9<T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, T5 extends @Nullable Object, T6 extends @Nullable Object, T7 extends @Nullable Object, T8 extends @Nullable Object, T9 extends @Nullable Object>(
        Result<T1> result1,
        Result<T2> result2,
        Result<T3> result3,
        Result<T4> result4,
        Result<T5> result5,
        Result<T6> result6,
        Result<T7> result7,
        Result<T8> result8,
        Result<T9> result9
) {
    public <T10 extends @Nullable Object> ResultCombiner10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> and(Result<T10> result10) {
        return new ResultCombiner10<>(
                result1,
                result2,
                result3,
                result4,
                result5,
                result6,
                result7,
                result8,
                result9,
                result10
        );
    }

    public <R extends @Nullable Object> Result<R> combine(NonaFunction<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> onSuccess) {
        return Result.combine(
                () -> onSuccess.apply(
                        result1.getOrThrow(),
                        result2.getOrThrow(),
                        result3.getOrThrow(),
                        result4.getOrThrow(),
                        result5.getOrThrow(),
                        result6.getOrThrow(),
                        result7.getOrThrow(),
                        result8.getOrThrow(),
                        result9.getOrThrow()
                ),
                result1, result2, result3, result4, result5,
                result6, result7, result8, result9
        );
    }
}
