package io.github.raniagus.javalidation.validator;

import io.github.raniagus.javalidation.ValidationErrors;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface Validator<T> {
    ValidationErrors validate(@Nullable T value);
}
