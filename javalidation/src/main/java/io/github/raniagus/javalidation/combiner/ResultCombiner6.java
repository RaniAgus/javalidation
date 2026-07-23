package io.github.raniagus.javalidation.combiner;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.HexFunction;
import org.jspecify.annotations.Nullable;

/**
 * Combines 6 {@link Result}s using the applicative functor pattern.
 * <p>
 * This is part of a chain of combiners (ResultCombiner2 through ResultCombiner10) that enable
 * combining multiple validation results while accumulating all errors. This combiner specifically
 * handles 6 results.
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
 *     .and(validatePassword(password))
 *     .combine((v1, v2, v3, v4, v5, v6) -> new Person(v1, v2, v3, v4, v5, v6));
 * }</pre>
 * <p>
 * If any validation fails, all errors are accumulated in the final {@code Err} result.
 *
 * @param <T1> the type of the first result's success value
 * @param <T2> the type of the second result's success value
 * @param <T3> the type of the third result's success value
 * @param <T4> the type of the fourth result's success value
 * @param <T5> the type of the fifth result's success value
 * @param <T6> the type of the sixth result's success value
 * @see Result#and(Result)
 */
public final class ResultCombiner6<T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, T5 extends @Nullable Object, T6 extends @Nullable Object> {
    private final ResultSlot<T1> result1;
    private final ResultSlot<T2> result2;
    private final ResultSlot<T3> result3;
    private final ResultSlot<T4> result4;
    private final ResultSlot<T5> result5;
    private final ResultSlot<T6> result6;

    ResultCombiner6(ResultSlot<T1> result1, ResultSlot<T2> result2, ResultSlot<T3> result3, ResultSlot<T4> result4, ResultSlot<T5> result5, ResultSlot<T6> result6) {
        this.result1 = result1;
        this.result2 = result2;
        this.result3 = result3;
        this.result4 = result4;
        this.result5 = result5;
        this.result6 = result6;
    }

    /**
     * Chains another result, producing a {@link ResultCombiner7}.
     * <p>
     * Example:
     * <pre>{@code
     * result1.and(result2)
     *     .and(result3)
     *     // ... more .and() calls
     *     .and(result7)
     *     .combine((v1, v2, v3, v4, v5, v6, v7) -> new Combined(v1, v2, v3, v4, v5, v6, v7));
     * }</pre>
     *
     * @param result7 the next result to combine
     * @param <T7>    the type of the next result's success value
     * @return a combiner for 7 results
     */
    public <T7 extends @Nullable Object> ResultCombiner7<T1, T2, T3, T4, T5, T6, T7> and(Result<T7> result7) {
        return new ResultCombiner7<>(result1, result2, result3, result4, result5, result6, ResultSlot.of(result7));
    }

    /**
     * Chains another result computed from the previous success values.
     * <p>
     * The function is only called if all previous results are {@link Result.Ok}. If any previous
     * result is {@link Result.Err}, the function is skipped and existing errors are preserved by
     * the final {@code combine()}.
     *
     * @param result7 supplies the next result using the previous success values
     * @param <T7>    the type of the next result's success value
     * @return a combiner for 7 results
     */
    public <T7 extends @Nullable Object> ResultCombiner7<T1, T2, T3, T4, T5, T6, T7> and(HexFunction<T1, T2, T3, T4, T5, T6, Result<T7>> result7) {
        if (ResultSlot.allOk(result1, result2, result3, result4, result5, result6)) {
            return new ResultCombiner7<>(result1, result2, result3, result4, result5, result6, ResultSlot.from(() -> result7.apply(
                    ResultSlot.value(result1),
                    ResultSlot.value(result2),
                    ResultSlot.value(result3),
                    ResultSlot.value(result4),
                    ResultSlot.value(result5),
                    ResultSlot.value(result6)
            )));
        }
        return new ResultCombiner7<>(result1, result2, result3, result4, result5, result6, ResultSlot.skipped());
    }

    /**
     * Combines the 6 results by applying the success function if all are {@link Result.Ok}.
     * <p>
     * If any result is {@link Result.Err}, all errors are accumulated and no success function is called.
     * <p>
     * Example:
     * <pre>{@code
     * Result<Combined> result = combiner.combine((v1, v2, v3, v4, v5, v6) ->
     *     new Combined(v1, v2, v3, v4, v5, v6)
     * );
     * }</pre>
     *
     * @param onSuccess function to apply to all success values
     * @param <R> the type of the combined result
     * @return {@link Result.Ok} with the combined value if all results succeed, otherwise {@link Result.Err}
     */
    public <R extends @Nullable Object> Result<R> combine(HexFunction<T1, T2, T3, T4, T5, T6, R> onSuccess) {
        return ResultSlot.combine(
                () -> onSuccess.apply(
                        ResultSlot.value(result1),
                        ResultSlot.value(result2),
                        ResultSlot.value(result3),
                        ResultSlot.value(result4),
                        ResultSlot.value(result5),
                        ResultSlot.value(result6)
                ),
                result1, result2, result3, result4, result5, result6
        );
    }

    /**
     * Returns the last success value if all results are {@link Result.Ok}, otherwise accumulates all errors.
     *
     * @return {@link Result.Ok} with the sixth value if all results succeed, otherwise {@link Result.Err}
     */
    public Result<T6> getLast() {
        return ResultSlot.combine(() -> ResultSlot.value(result6), result1, result2, result3, result4, result5, result6);
    }
}
