package io.github.raniagus.javalidation.combiner;

import io.github.raniagus.javalidation.Result;
import java.util.function.BiFunction;
import java.util.function.Function;
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
 * @see Result#and(Result)
 */
public final class ResultCombiner2<T1 extends @Nullable Object, T2 extends @Nullable Object> {
    private final ResultSlot<T1> result1;
    private final ResultSlot<T2> result2;

    public ResultCombiner2(Result<T1> result1, Result<T2> result2) {
        this(ResultSlot.of(result1), ResultSlot.of(result2));
    }

    public ResultCombiner2(Result<T1> result1, Function<T1, Result<T2>> result2) {
        ResultSlot<T1> slot1 = ResultSlot.of(result1);
        this.result1 = slot1;
        this.result2 = ResultSlot.allOk(slot1)
                ? ResultSlot.from(() -> result2.apply(ResultSlot.value(slot1)))
                : ResultSlot.skipped();
    }

    ResultCombiner2(ResultSlot<T1> result1, ResultSlot<T2> result2) {
        this.result1 = result1;
        this.result2 = result2;
    }

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
        return new ResultCombiner3<>(result1, result2, ResultSlot.of(result3));
    }

    /**
     * Chains another result computed from the previous success values.
     * <p>
     * The function is only called if all previous results are {@link Result.Ok}. If any previous
     * result is {@link Result.Err}, the function is skipped and existing errors are preserved by
     * the final {@code combine()}.
     *
     * @param result3 supplies the next result using the previous success values
     * @param <T3>    the type of the next result's success value
     * @return a combiner for 3 results
     */
    public <T3 extends @Nullable Object> ResultCombiner3<T1, T2, T3> and(BiFunction<T1, T2, Result<T3>> result3) {
        if (ResultSlot.allOk(result1, result2)) {
            return new ResultCombiner3<>(result1, result2, ResultSlot.from(() -> result3.apply(
                    ResultSlot.value(result1),
                    ResultSlot.value(result2)
            )));
        }
        return new ResultCombiner3<>(result1, result2, ResultSlot.skipped());
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
        return ResultSlot.combine(
                () -> onSuccess.apply(
                        ResultSlot.value(result1),
                        ResultSlot.value(result2)
                ),
                result1, result2
        );
    }

    /**
     * Returns the last success value if both results are {@link Result.Ok}, otherwise accumulates all errors.
     *
     * @return {@link Result.Ok} with the second value if all results succeed, otherwise {@link Result.Err}
     */
    public Result<T2> getLast() {
        return ResultSlot.combine(() -> ResultSlot.value(result2), result1, result2);
    }
}
