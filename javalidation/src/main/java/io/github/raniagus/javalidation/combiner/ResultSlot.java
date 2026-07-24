package io.github.raniagus.javalidation.combiner;

import io.github.raniagus.javalidation.JavalidationException;
import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

sealed interface ResultSlot<T extends @Nullable Object> permits ResultSlot.Value, ResultSlot.Skipped {

    record Value<T extends @Nullable Object>(Result<T> result) implements ResultSlot<T> {
        @Override
        public boolean isOk() {
            return result instanceof Result.Ok<?>;
        }

        @Override
        public T value() {
            if (result instanceof Result.Ok<T>(T v)) {
                return v;
            }
            return ResultSlot.super.value();
        }

        @Override
        public Result<T> toResult() {
            return result;
        }
    }

    record Skipped<T extends @Nullable Object>() implements ResultSlot<T> {
        private static final ResultSlot<?> INSTANCE = new Skipped<>();
        private static final Result<?> RESULT = Result.error(ValidationErrors.empty());

        @Override
        @SuppressWarnings("unchecked")
        public Result<T> toResult() {
            return (Result<T>) RESULT;
        }
    }

    default boolean isOk() {
        return false;
    }

    default T value() {
        throw new IllegalStateException("Result slot does not contain a success value");
    }

    Result<T> toResult();

    static <T extends @Nullable Object> ResultSlot<T> of(Result<T> result) {
        return new Value<>(result);
    }

    @SuppressWarnings("unchecked")
    static <T extends @Nullable Object> ResultSlot<T> skipped() {
        return (ResultSlot<T>) Skipped.INSTANCE;
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
            if (!slot.isOk()) {
                return false;
            }
        }
        return true;
    }

    static <R extends @Nullable Object> Result<R> combine(Supplier<R> onSuccess, ResultSlot<?>... slots) {
        Validation validation = Validation.create();
        boolean hasSkipped = false;

        for (ResultSlot<?> slot : slots) {
            if (slot instanceof Value<?>(Result<?> result)) {
                validation.addAll(result.errors());
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
