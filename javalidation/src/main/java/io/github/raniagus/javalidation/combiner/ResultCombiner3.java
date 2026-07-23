package io.github.raniagus.javalidation.combiner;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.TriFunction;
import org.jspecify.annotations.Nullable;

/**
 * Combines 3 {@link Result}s using the applicative functor pattern.
 * <p>
 * This is part of a chain of combiners (ResultCombiner2 through ResultCombiner10) that enable
 * combining multiple validation results while accumulating all errors. This combiner specifically
 * handles 3 results.
 * <p>
 * <b>All errors from all results are accumulated.</b> The success function is only called if
 * all results are {@link Result.Ok}.
 * <p>
 * <b>Example:</b>
 * <pre>{@code
 * Result<Person> person = validateName(name)
 *     .and(validateAge(age))
 *     .and(validateEmail(email))
 *     .combine((v1, v2, v3) -> new Person(v1, v2, v3));
 * }</pre>
 * <p>
 * If any validation fails, all errors are accumulated in the final {@code Err} result.
 *
 * @param <T1> the type of the first result's success value
 * @param <T2> the type of the second result's success value
 * @param <T3> the type of the third result's success value
 * @see Result#and(Result)
 */
public final class ResultCombiner3<T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object> {
    private final ResultSlot<T1> result1;
    private final ResultSlot<T2> result2;
    private final ResultSlot<T3> result3;

    ResultCombiner3(ResultSlot<T1> result1, ResultSlot<T2> result2, ResultSlot<T3> result3) {
        this.result1 = result1;
        this.result2 = result2;
        this.result3 = result3;
    }

    /**
     * Chains another result, producing a {@link ResultCombiner4}.
     * <p>
     * Example:
     * <pre>{@code
     * result1.and(result2)
     *     .and(result3)
     *     // ... more .and() calls
     *     .and(result4)
     *     .combine((v1, v2, v3, v4) -> new Combined(v1, v2, v3, v4));
     * }</pre>
     *
     * @param result4 the next result to combine
     * @param <T4>    the type of the next result's success value
     * @return a combiner for 4 results
     */
    public <T4 extends @Nullable Object> ResultCombiner4<T1, T2, T3, T4> and(Result<T4> result4) {
        return new ResultCombiner4<>(result1, result2, result3, ResultSlot.of(result4));
    }

    /**
     * Chains another result computed from the previous success values.
     * <p>
     * The function is only called if all previous results are {@link Result.Ok}. If any previous
     * result is {@link Result.Err}, the function is skipped and existing errors are preserved by
     * the final {@code combine()}.
     *
     * @param result4 supplies the next result using the previous success values
     * @param <T4>    the type of the next result's success value
     * @return a combiner for 4 results
     */
    public <T4 extends @Nullable Object> ResultCombiner4<T1, T2, T3, T4> and(TriFunction<T1, T2, T3, Result<T4>> result4) {
        if (ResultSlot.allOk(result1, result2, result3)) {
            return new ResultCombiner4<>(result1, result2, result3, ResultSlot.from(() -> result4.apply(
                    ResultSlot.value(result1),
                    ResultSlot.value(result2),
                    ResultSlot.value(result3)
            )));
        }
        return new ResultCombiner4<>(result1, result2, result3, ResultSlot.skipped());
    }

    /**
     * Combines the 3 results by applying the success function if all are {@link Result.Ok}.
     * <p>
     * If any result is {@link Result.Err}, all errors are accumulated and no success function is called.
     * <p>
     * Example:
     * <pre>{@code
     * Result<Combined> result = combiner.combine((v1, v2, v3) ->
     *     new Combined(v1, v2, v3)
     * );
     * }</pre>
     *
     * @param onSuccess function to apply to all success values
     * @param <R> the type of the combined result
     * @return {@link Result.Ok} with the combined value if all results succeed, otherwise {@link Result.Err}
     */
    public <R extends @Nullable Object> Result<R> combine(TriFunction<T1, T2, T3, R> onSuccess) {
        return ResultSlot.combine(
                () -> onSuccess.apply(
                        ResultSlot.value(result1),
                        ResultSlot.value(result2),
                        ResultSlot.value(result3)
                ),
                result1, result2, result3
        );
    }

    /**
     * Returns the last success value if all results are {@link Result.Ok}, otherwise accumulates all errors.
     *
     * @return {@link Result.Ok} with the third value if all results succeed, otherwise {@link Result.Err}
     */
    public Result<T3> getLast() {
        return ResultSlot.combine(() -> ResultSlot.value(result3), result1, result2, result3);
    }
}
