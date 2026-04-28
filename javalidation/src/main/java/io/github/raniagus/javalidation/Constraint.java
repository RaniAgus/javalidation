package io.github.raniagus.javalidation;

import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

public record Constraint<T extends @Nullable Object>(
        Predicate<T> predicate,
        String message,
        Object... args
) implements Validator<T> {
    @Override
    public void validate(Validation validation, T obj) {
        if (!predicate.test(obj)) {
            validation.addError(message, args);
        }
    }
}
