package io.github.raniagus.javalidation.combiner;

import io.github.raniagus.javalidation.JavalidationException;
import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

sealed interface ResultSlot<T extends @Nullable Object> permits ResultSlot.Value, ResultSlot.Skipped {

    record Value<T extends @Nullable Object>(Result<T> result) implements ResultSlot<T> {
    }

    record Skipped<T extends @Nullable Object>() implements ResultSlot<T> {
    }

    static <T extends @Nullable Object> ResultSlot<T> of(Result<T> result) {
        return new Value<>(result);
    }

    static <T extends @Nullable Object> ResultSlot<T> skipped() {
        return new Skipped<>();
    }

    static <T extends @Nullable Object> ResultSlot<T> from(Supplier<Result<T>> supplier) {
        try {
            return of(supplier.get());
        } catch (JavalidationException e) {
            return of(Result.error(e.getErrors()));
        }
    }

    static boolean allOk(ResultSlot<?>... slots) {
        for (ResultSlot<?> slot : slots) {
            if (!(slot instanceof Value<?>(Result<?> result)) || !(result instanceof Result.Ok<?>)) {
                return false;
            }
        }
        return true;
    }

    static <T extends @Nullable Object> T value(ResultSlot<T> slot) {
        if (slot instanceof Value<T>(Result<T> result) && result instanceof Result.Ok<T>(T value)) {
            return value;
        }
        throw new IllegalStateException("Result slot does not contain a success value");
    }

    static <T extends @Nullable Object> Result<T> toResult(ResultSlot<T> slot) {
        return slot instanceof Value<T>(var r) ? r : Result.error(ValidationErrors.empty());
    }

    static <R extends @Nullable Object> Result<R> combine(Supplier<R> onSuccess, ResultSlot<?>... slots) {
        Validation validation = Validation.create();
        boolean hasSkipped = false;

        for (ResultSlot<?> slot : slots) {
            if (slot instanceof Value<?>(Result<?> result) && result instanceof Result.Err<?>(ValidationErrors errors)) {
                validation.addAll(errors);
            } else if (slot instanceof Skipped<?>) {
                hasSkipped = true;
            }
        }

        if (hasSkipped) {
            return Result.error(validation.finish());
        }
        return validation.asResult(onSuccess);
    }
}
