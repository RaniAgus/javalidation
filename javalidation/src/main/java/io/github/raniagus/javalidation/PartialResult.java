package io.github.raniagus.javalidation;

import org.jspecify.annotations.Nullable;

public record PartialResult<T extends @Nullable Object>(T success, ValidationErrors errors) {
    public boolean hasErrors() {
        return errors.isNotEmpty();
    }

    public Result<T> toResult() {
        return errors.isEmpty() ? Result.ok(success) : Result.error(errors);
    }
}
