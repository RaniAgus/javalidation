package io.github.raniagus.javalidation.combiner;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.PentaFunction;
import org.jspecify.annotations.Nullable;

/**
 * Combines 5 {@link Result}s using the applicative functor pattern.
 * <p>
 * This is part of a chain of combiners (ResultCombiner2 through ResultCombiner10) that enable
 * combining multiple validation results while accumulating all errors. This combiner specifically
 * handles 5 results.
 * <p>
 * <b>All errors from all results are accumulated.</b> The success function is only called if
 * all results are {@link Result.Ok}.
 * <p>
 * <b>Example:</b>
 * <pre>{@code
 * Result<Person> person = validateName(name)
 *     .and(validateAge(age))
 *     .and(validateEmail(email))
 *     .and(validateAddress(address))
 *     .and(validatePhone(phone))
 *     .combine((v1, v2, v3, v4, v5) -> new Person(v1, v2, v3, v4, v5));
 * }</pre>
 * <p>
 * If any validation fails, all errors are accumulated in the final {@code Err} result.
 *
 * @param <T1> the type of the first result's success value
 * @param <T2> the type of the second result's success value
 * @param <T3> the type of the third result's success value
 * @param <T4> the type of the fourth result's success value
 * @param <T5> the type of the fifth result's success value
 * @param result1 the first result to combine
 * @param result2 the second result to combine
 * @param result3 the third result to combine
 * @param result4 the fourth result to combine
 * @param result5 the fifth result to combine
 * @see Result#and(Result)
 */
public record ResultCombiner5<T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, T5 extends @Nullable Object>(
        Result<T1> result1,
        Result<T2> result2,
        Result<T3> result3,
        Result<T4> result4,
        Result<T5> result5
) {
    /**
     * Chains another result, producing a {@link ResultCombiner6}.
     * <p>
     * Example:
     * <pre>{@code
     * result1.and(result2)
     *     .and(result3)
     *     // ... more .and() calls
     *     .and(result6)
     *     .combine((v1, v2, v3, v4, v5, v6) -> new Combined(v1, v2, v3, v4, v5, v6));
     * }</pre>
     *
     * @param result6 the next result to combine
     * @param <T6>    the type of the next result's success value
     * @return a combiner for 6 results
     */
    public <T6 extends @Nullable Object> ResultCombiner6<T1, T2, T3, T4, T5, T6> and(Result<T6> result6) {
        return new ResultCombiner6<>(result1, result2, result3, result4, result5, result6);
    }


    /**
     * Combines the 5 results by applying the success function if all are {@link Result.Ok}.
     * <p>
     * If any result is {@link Result.Err}, all errors are accumulated and no success function is called.
     * <p>
     * Example:
     * <pre>{@code
     * Result<Combined> result = combiner.combine((v1, v2, v3, v4, v5) ->
     *     new Combined(v1, v2, v3, v4, v5)
     * );
     * }</pre>
     *
     * @param onSuccess function to apply to all success values
     * @param <R> the type of the combined result
     * @return {@link Result.Ok} with the combined value if all results succeed, otherwise {@link Result.Err}
     */
    public <R extends @Nullable Object> Result<R> combine(PentaFunction<T1, T2, T3, T4, T5, R> onSuccess) {
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
