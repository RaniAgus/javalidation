package io.github.raniagus.javalidation.combiner;

import io.github.raniagus.javalidation.Result;
import java.util.function.BiFunction;
import org.jspecify.annotations.Nullable;

/**
 * Combines two {@link Result}s using the applicative functor pattern.
 * <p>
 * This is part of a chain of combiners (ResultCombiner2 through ResultCombiner10) that enable
 * combining multiple validation results while accumulating all errors. This combiner specifically
 * handles two results.
 * <p>
 * You typically create this via {@link Result#and(Result)} and either:
 * <ul>
 *   <li>Chain more results with {@link #and(Result)} (returns ResultCombiner3)</li>
 *   <li>Terminate with {@link #combine(BiFunction)} to produce the final result</li>
 * </ul>
 * <p>
 * <b>All errors from all results are accumulated.</b> The success function is only called if
 * all results are {@link Result.Ok}.
 * <p>
 * <b>Example:</b>
 * <pre>{@code
 * Result<Person> person = validateName(name)
 *     .and(validateAge(age))
 *     .combine((validName, validAge) -> new Person(validName, validAge));
 * }</pre>
 * <p>
 * If {@code validateName} returns {@code Err("name", "Invalid")} and {@code validateAge} returns
 * {@code Err("age", "Too young")}, both errors are accumulated in the final {@code Err} result.
 *
 * @param result1 the first result
 * @param result2 the second result
 * @param <T1> the type of the first result's success value
 * @param <T2> the type of the second result's success value
 * @see Result#and(Result)
 * @see ResultCombiner3
 */
public record ResultCombiner2<T1 extends @Nullable Object, T2 extends @Nullable Object>(
        Result<T1> result1,
        Result<T2> result2
) {
    /**
     * Chains a third result, producing a {@link ResultCombiner3}.
     * <p>
     * Example:
     * <pre>{@code
     * result1.and(result2)
     *     .and(result3)
     *     .combine((v1, v2, v3) -> new Triple(v1, v2, v3));
     * }</pre>
     *
     * @param result3 the third result to combine
     * @param <T3> the type of the third result's success value
     * @return a combiner for three results
     */
    public <T3 extends @Nullable Object> ResultCombiner3<T1, T2, T3> and(Result<T3> result3) {
        return new ResultCombiner3<>(result1, result2, result3);
    }

    /**
     * Combines the two results by applying the success function if both are {@link Result.Ok}.
     * <p>
     * If either result is {@link Result.Err}, all errors are accumulated and no success function is called.
     * <p>
     * Example:
     * <pre>{@code
     * Result<Person> person = validateName("Alice")
     *     .and(validateAge(30))
     *     .combine((name, age) -> new Person(name, age));
     * }</pre>
     *
     * @param onSuccess function to apply to both success values
     * @param <R> the type of the combined result
     * @return {@link Result.Ok} with the combined value if both results succeed, otherwise {@link Result.Err}
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
