package io.github.raniagus.javalidation.combiner;

import io.github.raniagus.javalidation.Result;
import java.util.function.BiFunction;
import org.jspecify.annotations.Nullable;

/**
 * Combines 2 {@link Result}s using the applicative functor pattern.
 * <p>
 * This is part of a chain of combiners (ResultCombiner2 through ResultCombiner10) that enable
 * combining multiple validation results while accumulating all errors. This combiner specifically
 * handles two results.
 * <p>
 * <b>All errors from all results are accumulated.</b> The success function is only called if
 * all results are {@link Result.Ok}.
 * <p>
 * <b>Example:</b>
 * <pre>{@code
 * Result<Person> person = validateName(name)
 *     .and(validateAge(age))
 *     .combine((v1, v2) -> new Person(v1, v2));
 * }</pre>
 * <p>
 * If any validation fails, all errors are accumulated in the final {@code Err} result.
 *
 * @param <T1> the type of the first result's success value
 * @param <T2> the type of the second result's success value
 * @param result1 the first result to combine
 * @param result2 the second result to combine
 * @see Result#and(Result)
 */
public record ResultCombiner2<T1 extends @Nullable Object, T2 extends @Nullable Object>(
        Result<T1> result1,
        Result<T2> result2
) {
    /**
     * Chains another result, producing a {@link ResultCombiner3}.
     * <p>
     * Example:
     * <pre>{@code
     * result1.and(result2)
     *     .and(result3)
     *     .combine((v1, v2, v3) -> new Combined(v1, v2, v3));
     * }</pre>
     *
     * @param result3 the next result to combine
     * @param <T3> the type of the next result's success value
     * @return a combiner for 3 results
     */
    public <T3 extends @Nullable Object> ResultCombiner3<T1, T2, T3> and(Result<T3> result3) {
        return new ResultCombiner3<>(result1, result2, result3);
    }


    /**
     * Combines the two results by applying the success function if all are {@link Result.Ok}.
     * <p>
     * If any result is {@link Result.Err}, all errors are accumulated and no success function is called.
     * <p>
     * Example:
     * <pre>{@code
     * Result<Combined> result = combiner.combine((v1, v2) ->
     *     new Combined(v1, v2)
     * );
     * }</pre>
     *
     * @param onSuccess function to apply to all success values
     * @param <R> the type of the combined result
     * @return {@link Result.Ok} with the combined value if all results succeed, otherwise {@link Result.Err}
     */
    public <R extends @Nullable Object> Result<R> combine(BiFunction<T1, T2, R> onSuccess) {
        return Result.combine(
                () -> onSuccess.apply(
                        result1.getOrThrow(),
                        result2.getOrThrow()
                ),
                result1, result2
        );
    }
}
