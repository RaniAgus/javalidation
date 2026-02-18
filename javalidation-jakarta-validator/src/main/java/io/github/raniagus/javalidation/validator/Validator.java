package io.github.raniagus.javalidation.validator;

import io.github.raniagus.javalidation.ValidationErrors;

@FunctionalInterface
public interface Validator<T> {
    ValidationErrors validate(T value);
}
