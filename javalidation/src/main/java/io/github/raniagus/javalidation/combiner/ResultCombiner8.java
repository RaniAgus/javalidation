package io.github.raniagus.javalidation.combiner;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.function.HexFunction;
import io.github.raniagus.javalidation.function.OctaFunction;
import io.github.raniagus.javalidation.function.PentaFunction;
import io.github.raniagus.javalidation.function.QuadFunction;
import io.github.raniagus.javalidation.function.SeptaFunction;
import io.github.raniagus.javalidation.function.TriFunction;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

/**
 * Combines 8 {@link Result}s using the applicative functor pattern.
 * <p>
 * This is part of a chain of combiners (ResultCombiner2 through ResultCombiner10) that enable
 * combining multiple validation results while accumulating all errors. This combiner specifically
 * handles 8 results.
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
 *     .combine((v1, v2, v3, v4, v5, v6, v7, v8) -> new Person(v1, v2, v3, v4, v5, v6, v7, v8));
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
 * @see Result#and(Result)
 */
public final class ResultCombiner8<T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, T5 extends @Nullable Object, T6 extends @Nullable Object, T7 extends @Nullable Object, T8 extends @Nullable Object> {
    private final ResultSlot<T1> result1;
    private final ResultSlot<T2> result2;
    private final ResultSlot<T3> result3;
    private final ResultSlot<T4> result4;
    private final ResultSlot<T5> result5;
    private final ResultSlot<T6> result6;
    private final ResultSlot<T7> result7;
    private final ResultSlot<T8> result8;

    ResultCombiner8(ResultSlot<T1> result1, ResultSlot<T2> result2, ResultSlot<T3> result3, ResultSlot<T4> result4, ResultSlot<T5> result5, ResultSlot<T6> result6, ResultSlot<T7> result7, ResultSlot<T8> result8) {
        this.result1 = result1;
        this.result2 = result2;
        this.result3 = result3;
        this.result4 = result4;
        this.result5 = result5;
        this.result6 = result6;
        this.result7 = result7;
        this.result8 = result8;
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

    /**
     * Chains another result, producing a {@link ResultCombiner9}.
     * <p>
     * Example:
     * <pre>{@code
     * result1.and(result2)
     *     .and(result3)
     *     // ... more .and() calls
     *     .and(result9)
     *     .combine((v1, v2, v3, v4, v5, v6, v7, v8, v9) -> new Combined(v1, v2, v3, v4, v5, v6, v7, v8, v9));
     * }</pre>
     *
     * @param result9 the next result to combine
     * @param <T9>    the type of the next result's success value
     * @return a combiner for 9 results
     */
    public <T9 extends @Nullable Object> ResultCombiner9<T1, T2, T3, T4, T5, T6, T7, T8, T9> and(Result<T9> result9) {
        return new ResultCombiner9<>(result1, result2, result3, result4, result5, result6, result7, result8, ResultSlot.of(result9));
    }

    /**
     * Chains another result computed from the previous success values.
     * <p>
     * The function is only called if all previous results are {@link Result.Ok}. If any previous
     * result is {@link Result.Err}, the function is skipped and existing errors are preserved by
     * the final {@code combine()}.
     *
     * @param result9 supplies the next result using the previous success values
     * @param <T9>    the type of the next result's success value
     * @return a combiner for 9 results
     */
    public <T9 extends @Nullable Object> ResultCombiner9<T1, T2, T3, T4, T5, T6, T7, T8, T9> and(OctaFunction<T1, T2, T3, T4, T5, T6, T7, T8, Result<T9>> result9) {
        if (ResultSlot.allOk(result1, result2, result3, result4, result5, result6, result7, result8)) {
            return new ResultCombiner9<>(result1, result2, result3, result4, result5, result6, result7, result8, ResultSlot.from(() -> result9.apply(
                    result1.value(),
                    result2.value(),
                    result3.value(),
                    result4.value(),
                    result5.value(),
                    result6.value(),
                    result7.value(),
                    result8.value()
            )));
        }
        return new ResultCombiner9<>(result1, result2, result3, result4, result5, result6, result7, result8, ResultSlot.skipped());
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
     * @param <T9>      the type of the next result's success value
     * @return a combiner for 9 results
     */
    public <X extends @Nullable Object, T9 extends @Nullable Object> ResultCombiner9<T1, T2, T3, T4, T5, T6, T7, T8, T9> andUsing(
            Function<ResultCombiner8<T1, T2, T3, T4, T5, T6, T7, T8>, Result<X>> projector,
            Function<X, Result<T9>> fn) {
        var projected = projector.apply(this);
        if (projected instanceof Result.Ok<X>(var x)) {
            return new ResultCombiner9<>(result1, result2, result3, result4, result5, result6, result7, result8, ResultSlot.from(() -> fn.apply(x)));
        }
        return new ResultCombiner9<>(result1, result2, result3, result4, result5, result6, result7, result8, ResultSlot.skipped());
    }

    /** Like {@link #andUsing(Function, Function)}, but selecting 2 prior results. */
    public <X extends @Nullable Object, Y extends @Nullable Object, T9 extends @Nullable Object> ResultCombiner9<T1, T2, T3, T4, T5, T6, T7, T8, T9> andUsing(
            Function<ResultCombiner8<T1, T2, T3, T4, T5, T6, T7, T8>, ResultCombiner2<X, Y>> projector,
            BiFunction<X, Y, Result<T9>> fn) {
        var sub = projector.apply(this);
        if (sub.first() instanceof Result.Ok<X>(var x) && sub.second() instanceof Result.Ok<Y>(var y)) {
            return new ResultCombiner9<>(result1, result2, result3, result4, result5, result6, result7, result8, ResultSlot.from(() -> fn.apply(x, y)));
        }
        return new ResultCombiner9<>(result1, result2, result3, result4, result5, result6, result7, result8, ResultSlot.skipped());
    }

    /** Like {@link #andUsing(Function, Function)}, but selecting 3 prior results. */
    public <X extends @Nullable Object, Y extends @Nullable Object, Z extends @Nullable Object, T9 extends @Nullable Object> ResultCombiner9<T1, T2, T3, T4, T5, T6, T7, T8, T9> andUsing(
            Function<ResultCombiner8<T1, T2, T3, T4, T5, T6, T7, T8>, ResultCombiner3<X, Y, Z>> projector,
            TriFunction<X, Y, Z, Result<T9>> fn) {
        var sub = projector.apply(this);
        if (sub.first() instanceof Result.Ok<X>(var x) && sub.second() instanceof Result.Ok<Y>(var y) && sub.third() instanceof Result.Ok<Z>(var z)) {
            return new ResultCombiner9<>(result1, result2, result3, result4, result5, result6, result7, result8, ResultSlot.from(() -> fn.apply(x, y, z)));
        }
        return new ResultCombiner9<>(result1, result2, result3, result4, result5, result6, result7, result8, ResultSlot.skipped());
    }

    /** Like {@link #andUsing(Function, Function)}, but selecting 4 prior results. */
    public <X extends @Nullable Object, Y extends @Nullable Object, Z extends @Nullable Object, W extends @Nullable Object, T9 extends @Nullable Object> ResultCombiner9<T1, T2, T3, T4, T5, T6, T7, T8, T9> andUsing(
            Function<ResultCombiner8<T1, T2, T3, T4, T5, T6, T7, T8>, ResultCombiner4<X, Y, Z, W>> projector,
            QuadFunction<X, Y, Z, W, Result<T9>> fn) {
        var sub = projector.apply(this);
        if (sub.first() instanceof Result.Ok<X>(var x) && sub.second() instanceof Result.Ok<Y>(var y) && sub.third() instanceof Result.Ok<Z>(var z) && sub.fourth() instanceof Result.Ok<W>(var w)) {
            return new ResultCombiner9<>(result1, result2, result3, result4, result5, result6, result7, result8, ResultSlot.from(() -> fn.apply(x, y, z, w)));
        }
        return new ResultCombiner9<>(result1, result2, result3, result4, result5, result6, result7, result8, ResultSlot.skipped());
    }

    /** Like {@link #andUsing(Function, Function)}, but selecting 5 prior results. */
    public <X extends @Nullable Object, Y extends @Nullable Object, Z extends @Nullable Object, W extends @Nullable Object, V extends @Nullable Object, T9 extends @Nullable Object> ResultCombiner9<T1, T2, T3, T4, T5, T6, T7, T8, T9> andUsing(
            Function<ResultCombiner8<T1, T2, T3, T4, T5, T6, T7, T8>, ResultCombiner5<X, Y, Z, W, V>> projector,
            PentaFunction<X, Y, Z, W, V, Result<T9>> fn) {
        var sub = projector.apply(this);
        if (sub.first() instanceof Result.Ok<X>(var x) && sub.second() instanceof Result.Ok<Y>(var y) && sub.third() instanceof Result.Ok<Z>(var z) && sub.fourth() instanceof Result.Ok<W>(var w) && sub.fifth() instanceof Result.Ok<V>(var v)) {
            return new ResultCombiner9<>(result1, result2, result3, result4, result5, result6, result7, result8, ResultSlot.from(() -> fn.apply(x, y, z, w, v)));
        }
        return new ResultCombiner9<>(result1, result2, result3, result4, result5, result6, result7, result8, ResultSlot.skipped());
    }

    /** Like {@link #andUsing(Function, Function)}, but selecting 6 prior results. */
    public <X extends @Nullable Object, Y extends @Nullable Object, Z extends @Nullable Object, W extends @Nullable Object, V extends @Nullable Object, U extends @Nullable Object, T9 extends @Nullable Object> ResultCombiner9<T1, T2, T3, T4, T5, T6, T7, T8, T9> andUsing(
            Function<ResultCombiner8<T1, T2, T3, T4, T5, T6, T7, T8>, ResultCombiner6<X, Y, Z, W, V, U>> projector,
            HexFunction<X, Y, Z, W, V, U, Result<T9>> fn) {
        var sub = projector.apply(this);
        if (sub.first() instanceof Result.Ok<X>(var x) && sub.second() instanceof Result.Ok<Y>(var y) && sub.third() instanceof Result.Ok<Z>(var z) && sub.fourth() instanceof Result.Ok<W>(var w) && sub.fifth() instanceof Result.Ok<V>(var v) && sub.sixth() instanceof Result.Ok<U>(var u)) {
            return new ResultCombiner9<>(result1, result2, result3, result4, result5, result6, result7, result8, ResultSlot.from(() -> fn.apply(x, y, z, w, v, u)));
        }
        return new ResultCombiner9<>(result1, result2, result3, result4, result5, result6, result7, result8, ResultSlot.skipped());
    }

    /** Like {@link #andUsing(Function, Function)}, but selecting 7 prior results. */
    public <X extends @Nullable Object, Y extends @Nullable Object, Z extends @Nullable Object, W extends @Nullable Object, V extends @Nullable Object, U extends @Nullable Object, S extends @Nullable Object, T9 extends @Nullable Object> ResultCombiner9<T1, T2, T3, T4, T5, T6, T7, T8, T9> andUsing(
            Function<ResultCombiner8<T1, T2, T3, T4, T5, T6, T7, T8>, ResultCombiner7<X, Y, Z, W, V, U, S>> projector,
            SeptaFunction<X, Y, Z, W, V, U, S, Result<T9>> fn) {
        var sub = projector.apply(this);
        if (sub.first() instanceof Result.Ok<X>(var x) && sub.second() instanceof Result.Ok<Y>(var y) && sub.third() instanceof Result.Ok<Z>(var z) && sub.fourth() instanceof Result.Ok<W>(var w) && sub.fifth() instanceof Result.Ok<V>(var v) && sub.sixth() instanceof Result.Ok<U>(var u) && sub.seventh() instanceof Result.Ok<S>(var s)) {
            return new ResultCombiner9<>(result1, result2, result3, result4, result5, result6, result7, result8, ResultSlot.from(() -> fn.apply(x, y, z, w, v, u, s)));
        }
        return new ResultCombiner9<>(result1, result2, result3, result4, result5, result6, result7, result8, ResultSlot.skipped());
    }

    /**
     * Combines the 8 results by applying the success function if all are {@link Result.Ok}.
     * <p>
     * If any result is {@link Result.Err}, all errors are accumulated and no success function is called.
     * <p>
     * Example:
     * <pre>{@code
     * Result<Combined> result = combiner.combine((v1, v2, v3, v4, v5, v6, v7, v8) ->
     *     new Combined(v1, v2, v3, v4, v5, v6, v7, v8)
     * );
     * }</pre>
     *
     * @param onSuccess function to apply to all success values
     * @param <R> the type of the combined result
     * @return {@link Result.Ok} with the combined value if all results succeed, otherwise {@link Result.Err}
     */
    public <R extends @Nullable Object> Result<R> combine(OctaFunction<T1, T2, T3, T4, T5, T6, T7, T8, R> onSuccess) {
        return ResultSlot.combine(
                () -> onSuccess.apply(
                        result1.value(),
                        result2.value(),
                        result3.value(),
                        result4.value(),
                        result5.value(),
                        result6.value(),
                        result7.value(),
                        result8.value()
                ),
                result1, result2, result3, result4, result5, result6, result7, result8
        );
    }

    /**
     * Returns the last success value if all results are {@link Result.Ok}, otherwise accumulates all errors.
     *
     * @return {@link Result.Ok} with the eighth value if all results succeed, otherwise {@link Result.Err}
     */
    public Result<T8> getLast() {
        return ResultSlot.combine(() -> result8.value(), result1, result2, result3, result4, result5, result6, result7, result8);
    }
}
