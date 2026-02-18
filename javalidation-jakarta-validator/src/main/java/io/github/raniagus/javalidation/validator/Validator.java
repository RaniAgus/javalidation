package io.github.raniagus.javalidation.validator;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;

public interface Validator<T> {
    default ValidationErrors validate(T value) {
        Validation validation = Validation.create();
        validate(validation, value);
        return validation.finish();
    }

    void validate(Validation validation, T value);
}
