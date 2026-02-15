package io.github.raniagus.javalidation;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface Validator<T> {
    ValidationErrors validate(@Nullable T value);
}
