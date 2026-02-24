package io.github.raniagus.javalidation.validator;

public interface InitializableValidator<T> extends Validator<T> {
    void initialize(ValidatorsHolder holder);
}
