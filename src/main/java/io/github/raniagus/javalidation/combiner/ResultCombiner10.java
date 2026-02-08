package io.github.raniagus.javalidation.combiner;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.DecaFunction;
import org.jspecify.annotations.Nullable;

/**
 * Combines 10 {@link Result}s using the applicative functor pattern.
 * <p>
 * This is part of a chain of combiners (ResultCombiner2 through ResultCombiner10) that enable
 * combining multiple validation results while accumulating all errors. This combiner specifically
 * handles 10 results.
 * <p>
 * You typically create this via {@link Result#and(Result)} and:
 * <ul>
 *   <li>Terminate with {@link #combine(DecaFunction<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R>)} to produce the final result</li>
 * </ul>
 * <p>
 * <b>All errors from all results are accumulated.</b> The success function is only called if
 * all results are {@link Result.Ok}.
 * <p>
 * <b>Example:</b>
 * <pre>{@code
 * Result<Person> person = validateName(name)
 *     .and(validateAge(age))
 *     .and(validateEmail(email))
 *     .combine((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10) -> new Person(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10));
 * }</pre>
 * <p>
 * If any validation fails, all errors are accumulated in the final {@code Err} result.
 *
 * @param <T1> <T2> <T3> <T4> <T5> <T6> <T7> <T8> <T9> <T10>> the types of the results' success values
 * @see Result#and(Result)
 */
public record ResultCombiner10<T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, T5 extends @Nullable Object, T6 extends @Nullable Object, T7 extends @Nullable Object, T8 extends @Nullable Object, T9 extends @Nullable Object, T10 extends @Nullable Object>(
        Result<T1> result1,
        Result<T2> result2,
        Result<T3> result3,
        Result<T4> result4,
        Result<T5> result5,
        Result<T6> result6,
        Result<T7> result7,
        Result<T8> result8,
        Result<T9> result9,
        Result<T10> result10
) {

    /**
     * Combines the 10 results by applying the success function if all are {@link Result.Ok}.
     * <p>
     * If any result is {@link Result.Err}, all errors are accumulated and no success function is called.
     * <p>
     * Example:
     * <pre>{@code
     * Result<Combined> result = combiner.combine((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10) ->
     *     new Combined(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10)
     * );
     * }</pre>
     *
     * @param onSuccess function to apply to all success values
     * @param <R>       the type of the combined result
     * @return {@link Result.Ok} with the combined value if all results succeed, otherwise {@link Result.Err}
     */
    public <R extends @Nullable Object> Result<R> combine(DecaFunction<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> onSuccess) {
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
                        result9.getOrThrow(),
                        result10.getOrThrow()
                ),
                result1, result2, result3, result4, result5, result6, result7, result8, result9, result10
        );
    }
}
