package io.github.raniagus.javalidation;

import org.jspecify.annotations.Nullable;

public interface Validator<T> {
    ValidationErrors validate(@Nullable T value);
}
