package io.github.raniagus.javalidation;

import org.jspecify.annotations.Nullable;

public record PartitionedResult<T extends @Nullable Object>(T value, ValidationErrors errors) {
    public boolean hasErrors() {
        return errors.isNotEmpty();
    }

    public Result<T> toResult() {
        return errors.isEmpty() ? Result.ok(value) : Result.error(errors);
    }
}
