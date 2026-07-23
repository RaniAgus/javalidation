package io.github.raniagus.javalidation.combiner;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.PentaFunction;
import io.github.raniagus.javalidation.function.QuadFunction;
import io.github.raniagus.javalidation.function.TriFunction;
import java.util.function.BiFunction;
import java.util.function.Function;
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
 * @see Result#and(Result)
 */
public final class ResultCombiner5<T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, T5 extends @Nullable Object> {
    private final ResultSlot<T1> result1;
    private final ResultSlot<T2> result2;
    private final ResultSlot<T3> result3;
    private final ResultSlot<T4> result4;
    private final ResultSlot<T5> result5;

    ResultCombiner5(ResultSlot<T1> result1, ResultSlot<T2> result2, ResultSlot<T3> result3, ResultSlot<T4> result4, ResultSlot<T5> result5) {
        this.result1 = result1;
        this.result2 = result2;
        this.result3 = result3;
        this.result4 = result4;
        this.result5 = result5;
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
        return new ResultCombiner6<>(result1, result2, result3, result4, result5, ResultSlot.of(result6));
    }

    /**
     * Chains another result computed from the previous success values.
     * <p>
     * The function is only called if all previous results are {@link Result.Ok}. If any previous
     * result is {@link Result.Err}, the function is skipped and existing errors are preserved by
     * the final {@code combine()}.
     *
     * @param result6 supplies the next result using the previous success values
     * @param <T6>    the type of the next result's success value
     * @return a combiner for 6 results
     */
    public <T6 extends @Nullable Object> ResultCombiner6<T1, T2, T3, T4, T5, T6> and(PentaFunction<T1, T2, T3, T4, T5, Result<T6>> result6) {
        if (ResultSlot.allOk(result1, result2, result3, result4, result5)) {
            return new ResultCombiner6<>(result1, result2, result3, result4, result5, ResultSlot.from(() -> result6.apply(
                    result1.value(),
                    result2.value(),
                    result3.value(),
                    result4.value(),
                    result5.value()
            )));
        }
        return new ResultCombiner6<>(result1, result2, result3, result4, result5, ResultSlot.skipped());
    }

    /**
     * Chains another result computed from a selected prior result.
     * <p>
     * The projector receives this combiner and returns the specific prior {@link Result} to depend on.
     * The function is only called if that result is {@link Result.Ok}. Any prior results not selected
     * still contribute their errors independently through their own slots.
     *
     * @param projector selects which prior result to depend on
     * @param fn        supplies the next result using the selected success value
     * @param <X>       the type of the selected result's success value
     * @param <T6>      the type of the next result's success value
     * @return a combiner for 6 results
     */
    public <X extends @Nullable Object, T6 extends @Nullable Object> ResultCombiner6<T1, T2, T3, T4, T5, T6> andUsing(
            Function<ResultCombiner5<T1, T2, T3, T4, T5>, Result<X>> projector,
            Function<X, Result<T6>> fn) {
        Result<X> projected = projector.apply(this);
        if (projected instanceof Result.Ok<X>(X x)) {
            return new ResultCombiner6<>(result1, result2, result3, result4, result5, ResultSlot.from(() -> fn.apply(x)));
        }
        return new ResultCombiner6<>(result1, result2, result3, result4, result5, ResultSlot.skipped());
    }

    /** Like {@link #andUsing(Function, Function)}, but selecting 2 prior results. */
    public <X extends @Nullable Object, Y extends @Nullable Object, T6 extends @Nullable Object> ResultCombiner6<T1, T2, T3, T4, T5, T6> andUsing(
            Function<ResultCombiner5<T1, T2, T3, T4, T5>, ResultCombiner2<X, Y>> projector,
            BiFunction<X, Y, Result<T6>> fn) {
        ResultCombiner2<X, Y> sub = projector.apply(this);
        if (sub.first() instanceof Result.Ok<X>(X x) && sub.second() instanceof Result.Ok<Y>(Y y)) {
            return new ResultCombiner6<>(result1, result2, result3, result4, result5, ResultSlot.from(() -> fn.apply(x, y)));
        }
        return new ResultCombiner6<>(result1, result2, result3, result4, result5, ResultSlot.skipped());
    }

    /** Like {@link #andUsing(Function, Function)}, but selecting 3 prior results. */
    public <X extends @Nullable Object, Y extends @Nullable Object, Z extends @Nullable Object, T6 extends @Nullable Object> ResultCombiner6<T1, T2, T3, T4, T5, T6> andUsing(
            Function<ResultCombiner5<T1, T2, T3, T4, T5>, ResultCombiner3<X, Y, Z>> projector,
            TriFunction<X, Y, Z, Result<T6>> fn) {
        ResultCombiner3<X, Y, Z> sub = projector.apply(this);
        if (sub.first() instanceof Result.Ok<X>(X x) && sub.second() instanceof Result.Ok<Y>(Y y) && sub.third() instanceof Result.Ok<Z>(Z z)) {
            return new ResultCombiner6<>(result1, result2, result3, result4, result5, ResultSlot.from(() -> fn.apply(x, y, z)));
        }
        return new ResultCombiner6<>(result1, result2, result3, result4, result5, ResultSlot.skipped());
    }

    /** Like {@link #andUsing(Function, Function)}, but selecting 4 prior results. */
    public <X extends @Nullable Object, Y extends @Nullable Object, Z extends @Nullable Object, W extends @Nullable Object, T6 extends @Nullable Object> ResultCombiner6<T1, T2, T3, T4, T5, T6> andUsing(
            Function<ResultCombiner5<T1, T2, T3, T4, T5>, ResultCombiner4<X, Y, Z, W>> projector,
            QuadFunction<X, Y, Z, W, Result<T6>> fn) {
        ResultCombiner4<X, Y, Z, W> sub = projector.apply(this);
        if (sub.first() instanceof Result.Ok<X>(X x) && sub.second() instanceof Result.Ok<Y>(Y y) && sub.third() instanceof Result.Ok<Z>(Z z) && sub.fourth() instanceof Result.Ok<W>(W w)) {
            return new ResultCombiner6<>(result1, result2, result3, result4, result5, ResultSlot.from(() -> fn.apply(x, y, z, w)));
        }
        return new ResultCombiner6<>(result1, result2, result3, result4, result5, ResultSlot.skipped());
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
        return ResultSlot.combine(
                () -> onSuccess.apply(
                        result1.value(),
                        result2.value(),
                        result3.value(),
                        result4.value(),
                        result5.value()
                ),
                result1, result2, result3, result4, result5
        );
    }

    /**
     * Returns the last success value if all results are {@link Result.Ok}, otherwise accumulates all errors.
     *
     * @return {@link Result.Ok} with the fifth value if all results succeed, otherwise {@link Result.Err}
     */
    public Result<T5> getLast() {
        return ResultSlot.combine(() -> result5.value(), result1, result2, result3, result4, result5);
    }
}
