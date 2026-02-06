package io.github.raniagus.javalidation;

import io.github.raniagus.javalidation.joiner.Joiner2;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

public sealed interface Result<T extends @Nullable Object> {
    record Ok<T extends @Nullable Object>(T value) implements Result<T> {
    }

    record Err<T extends @Nullable Object>(ValidationErrors errors) implements Result<T> {
    }

    default T getOrThrow() {
        return switch (this) {
            case Ok<T>(T value) -> value;
            case Err<T>(ValidationErrors errors) -> throw new ValidationException(errors);
        };
    }

    default ValidationErrors getErrors() {
        return switch (this) {
            case Ok<T>(T ignored) -> ValidationErrors.empty();
            case Err<T>(ValidationErrors errors) -> errors;
        };
    }

    default Result<T> withPrefix(String prefix) {
        return switch (this) {
            case Ok<T>(T value) -> new Ok<>(value);
            case Err<T>(ValidationErrors errors) -> new Err<>(errors.withPrefix(prefix));
        };
    }

    default Result<T> withPrefix(Object... prefix) {
        return withPrefix(prefix);
    }

    default <U> Joiner2<T, U> with(Result<U> result) {
        return new Joiner2<>(this, result);
    }

    static <T extends @Nullable Object> Result<T> of(Supplier<T> supplier) {
        try {
            return new Ok<>(supplier.get());
        } catch (ValidationException e) {
            return new Err<>(e.getErrors());
        }
    }

    static <T extends @Nullable Object> Result<T> ok(T value) {
        return new Ok<>(value);
    }

    static <T extends @Nullable Object> Result<T> err(String message) {
        return new Err<>(ValidationErrors.of(message));
    }

    static <T extends @Nullable Object> Result<T> err(String field, String message) {
        return new Err<>(ValidationErrors.of(field, message));
    }

    static <T extends @Nullable Object> Result<T> err(ValidationErrors errors) {
        return new Err<>(errors);
    }

    static <R> Result<R> merge(Supplier<R> onSuccess, Result<?>... results) {
        Validator validator = new Validator();
        for (Result<?> result : results) {
            if (result instanceof Err(ValidationErrors errors)) {
                validator.addAll(errors);
            }
        }
        return validator.asResult(onSuccess);
    }
}
