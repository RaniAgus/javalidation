package io.github.raniagus.javalidation.combiner;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.QuadFunction;
import org.jspecify.annotations.Nullable;

/**
 * Combines 4 {@link Result}s using the applicative functor pattern.
 * <p>
 * This is part of a chain of combiners (ResultCombiner2 through ResultCombiner10) that enable
 * combining multiple validation results while accumulating all errors. This combiner specifically
 * handles 4 results.
 * <p>
 * You typically create this via {@link Result#and(Result)} and either:
 * <ul>
 *   <li>Chain more results with {@link #and(Result)} (returns ResultCombiner5)</li>
 *   <li>Terminate with {@link #combine(QuadFunction <T1, T2, T3, T4, R>)} to produce the final result</li>
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
 *     .combine((v1, v2, v3, v4) -> new Person(v1, v2, v3, v4));
 * }</pre>
 * <p>
 * If any validation fails, all errors are accumulated in the final {@code Err} result.
 *
 * @param <T1> <T2> <T3> <T4>> the types of the results' success values
 * @see Result#and(Result)
 */
public record ResultCombiner4<T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object>(
        Result<T1> result1,
        Result<T2> result2,
        Result<T3> result3,
        Result<T4> result4
) {
    /**
     * Chains another result, producing a {@link ResultCombiner5}.
     * <p>
     * Example:
     * <pre>{@code
     * result1.and(result2)
     *     .and(result3)
     *     // ... more .and() calls
     *     .and(result5)
     *     .combine((v1, v2, v3, v4, v5) -> new Combined(v1, v2, v3, v4, v5));
     * }</pre>
     *
     * @param result5 the next result to combine
     * @param <T5>    the type of the next result's success value
     * @return a combiner for 5 results
     */
    public <T5 extends @Nullable Object> ResultCombiner5<T1, T2, T3, T4, T5> and(Result<T5> result5) {
        return new ResultCombiner5<>(result1, result2, result3, result4, result5);
    }


    /**
     * Combines the 4 results by applying the success function if all are {@link Result.Ok}.
     * <p>
     * If any result is {@link Result.Err}, all errors are accumulated and no success function is called.
     * <p>
     * Example:
     * <pre>{@code
     * Result<Combined> result = combiner.combine((v1, v2, v3, v4) ->
     *     new Combined(v1, v2, v3, v4)
     * );
     * }</pre>
     *
     * @param onSuccess function to apply to all success values
     * @param <R>       the type of the combined result
     * @return {@link Result.Ok} with the combined value if all results succeed, otherwise {@link Result.Err}
     */
    public <R extends @Nullable Object> Result<R> combine(QuadFunction<T1, T2, T3, T4, R> onSuccess) {
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
