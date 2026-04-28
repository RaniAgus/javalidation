package io.github.raniagus.javalidation;

public interface Validator<T> {
    default ValidationErrors validate(T value) {
        Validation validation = Validation.create();
        validate(validation, value);
        return validation.finish();
    }

    void validate(Validation validation, T value);
}
