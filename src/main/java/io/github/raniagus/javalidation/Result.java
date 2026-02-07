package io.github.raniagus.javalidation;

import io.github.raniagus.javalidation.combiner.ResultCombiner2;
import java.util.function.Function;
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
            case Err<T>(ValidationErrors errors) -> throw new JavalidationException(errors);
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

    default Result<T> withPrefix(Object first, Object... remaining) {
        StringBuilder sb = new StringBuilder().append(first);
        for (Object o : remaining) {
            sb.append(o);
        }
        return withPrefix(sb.toString());
    }

     default <U> ResultCombiner2<T, U> and(Result<U> result) {
         return new ResultCombiner2<>(this, result);
     }

     default <U> Result<U> map(Function<T, U> mapper) {
         return switch (this) {
             case Ok<T>(T value) -> new Ok<>(mapper.apply(value));
             case Err<T>(ValidationErrors errors) -> new Err<>(errors);
         };
     }

     default <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
         return switch (this) {
             case Ok<T>(T value) -> mapper.apply(value);
             case Err<T>(ValidationErrors errors) -> new Err<>(errors);
         };
     }

     default Result<T> mapErr(Function<ValidationErrors, ValidationErrors> mapper) {
         return switch (this) {
             case Ok<T>(T ignored) -> this;
             case Err<T>(ValidationErrors errors) -> new Err<>(mapper.apply(errors));
         };
     }

     default <U> U fold(Function<T, U> onSuccess, Function<ValidationErrors, U> onFailure) {
         return switch (this) {
             case Ok<T>(T value) -> onSuccess.apply(value);
             case Err<T>(ValidationErrors errors) -> onFailure.apply(errors);
         };
     }

     default T getOrElse(T defaultValue) {
         return switch (this) {
             case Ok<T>(T value) -> value;
             case Err<T>(ValidationErrors ignored) -> defaultValue;
         };
     }

     default T getOrElse(Supplier<T> supplier) {
         return switch (this) {
             case Ok<T>(T value) -> value;
             case Err<T>(ValidationErrors ignored) -> supplier.get();
         };
     }

     static <T extends @Nullable Object> Result<T> of(Supplier<T> supplier) {
        try {
            return new Ok<>(supplier.get());
        } catch (JavalidationException e) {
            return new Err<>(e.getErrors());
        }
    }

    static Result<Void> of(Runnable runnable) {
        return of(() -> {
            runnable.run();
            return null;
        });
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

    static <R> Result<R> combine(Supplier<R> onSuccess, Result<?>... results) {
        Validation validation = Validation.create();
        for (Result<?> result : results) {
            if (result instanceof Err(ValidationErrors errors)) {
                validation.addAll(errors);
            }
        }
        return validation.asResult(onSuccess);
    }
}
