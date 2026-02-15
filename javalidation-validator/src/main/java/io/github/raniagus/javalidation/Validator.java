package io.github.raniagus.javalidation;

import org.jspecify.annotations.Nullable;

public interface Validator<T extends @Nullable Object> {
    ValidationErrors validate(T value);
}
