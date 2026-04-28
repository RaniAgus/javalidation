package io.github.raniagus.javalidation.validator;

import io.github.raniagus.javalidation.Validator;

public interface InitializableValidator<T> extends Validator<T> {
    void initialize(ValidatorsHolder holder);
}
