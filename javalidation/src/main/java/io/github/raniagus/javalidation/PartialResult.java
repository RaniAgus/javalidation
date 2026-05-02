package io.github.raniagus.javalidation;

import java.util.function.Function;
import org.jspecify.annotations.Nullable;

public record PartialResult<T extends @Nullable Object>(T success, ValidationErrors errors) {
    public boolean hasErrors() {
        return errors.isNotEmpty();
    }

    public Result<T> toResult() {
        return errors.isEmpty() ? Result.ok(success) : Result.error(errors);
    }

    /**
     * Transforms the success value, preserving any accumulated errors.
     *
     * @param mapper function to apply to the success value
     * @param <U> the new success type
     * @return a new {@code PartialResult} with the mapped value and the same errors
     */
    public <U extends @Nullable Object> PartialResult<U> map(Function<T, U> mapper) {
        return new PartialResult<>(mapper.apply(success), errors);
    }

    /**
     * Transforms the accumulated errors, preserving the success value.
     *
     * @param mapper function to apply to the errors
     * @return a new {@code PartialResult} with the same value and the mapped errors
     */
    public PartialResult<T> mapErr(Function<ValidationErrors, ValidationErrors> mapper) {
        return new PartialResult<>(success, mapper.apply(errors));
    }

    /**
     * Transforms both the success value and the accumulated errors independently.
     *
     * @param valueMapper function to apply to the success value
     * @param errorsMapper function to apply to the errors
     * @param <U> the new success type
     * @return a new {@code PartialResult} with both sides transformed
     */
    public <U extends @Nullable Object> PartialResult<U> bimap(
            Function<T, U> valueMapper,
            Function<ValidationErrors, ValidationErrors> errorsMapper) {
        return new PartialResult<>(valueMapper.apply(success), errorsMapper.apply(errors));
    }
}
