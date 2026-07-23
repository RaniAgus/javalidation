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
 *     .and(validateRole(role))
 *     .and(validateIsAdmin(isAdmin))
 *     .and(validateIsPremium(isPremium))
 *     .and(validateIsBanned(isBanned))
 *     .combine((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10) -> new Person(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10));
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
 * @param <T7> the type of the seventh result's success value
 * @param <T8> the type of the eighth result's success value
 * @param <T9> the type of the ninth result's success value
 * @param <T10> the type of the tenth result's success value
 * @see Result#and(Result)
 */
public final class ResultCombiner10<T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, T5 extends @Nullable Object, T6 extends @Nullable Object, T7 extends @Nullable Object, T8 extends @Nullable Object, T9 extends @Nullable Object, T10 extends @Nullable Object> {
    private final ResultSlot<T1> result1;
    private final ResultSlot<T2> result2;
    private final ResultSlot<T3> result3;
    private final ResultSlot<T4> result4;
    private final ResultSlot<T5> result5;
    private final ResultSlot<T6> result6;
    private final ResultSlot<T7> result7;
    private final ResultSlot<T8> result8;
    private final ResultSlot<T9> result9;
    private final ResultSlot<T10> result10;

    ResultCombiner10(ResultSlot<T1> result1, ResultSlot<T2> result2, ResultSlot<T3> result3, ResultSlot<T4> result4, ResultSlot<T5> result5, ResultSlot<T6> result6, ResultSlot<T7> result7, ResultSlot<T8> result8, ResultSlot<T9> result9, ResultSlot<T10> result10) {
        this.result1 = result1;
        this.result2 = result2;
        this.result3 = result3;
        this.result4 = result4;
        this.result5 = result5;
        this.result6 = result6;
        this.result7 = result7;
        this.result8 = result8;
        this.result9 = result9;
        this.result10 = result10;
    }

    public Result<T1> first() {
        return result1.toResult();
    }

    public Result<T2> second() {
        return result2.toResult();
    }

    public Result<T3> third() {
        return result3.toResult();
    }

    public Result<T4> fourth() {
        return result4.toResult();
    }

    public Result<T5> fifth() {
        return result5.toResult();
    }

    public Result<T6> sixth() {
        return result6.toResult();
    }

    public Result<T7> seventh() {
        return result7.toResult();
    }

    public Result<T8> eighth() {
        return result8.toResult();
    }

    public Result<T9> ninth() {
        return result9.toResult();
    }

    public Result<T10> tenth() {
        return result10.toResult();
    }

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
        return ResultSlot.combine(
                () -> onSuccess.apply(
                        result1.value(),
                        result2.value(),
                        result3.value(),
                        result4.value(),
                        result5.value(),
                        result6.value(),
                        result7.value(),
                        result8.value(),
                        result9.value(),
                        result10.value()
                ),
                result1, result2, result3, result4, result5, result6, result7, result8, result9, result10
        );
    }

    /**
     * Returns the last success value if all results are {@link Result.Ok}, otherwise accumulates all errors.
     *
     * @return {@link Result.Ok} with the tenth value if all results succeed, otherwise {@link Result.Err}
     */
    public Result<T10> getLast() {
        return ResultSlot.combine(() -> result10.value(), result1, result2, result3, result4, result5, result6, result7, result8, result9, result10);
    }
}
