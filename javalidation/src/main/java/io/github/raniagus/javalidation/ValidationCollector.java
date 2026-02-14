package io.github.raniagus.javalidation;

import org.jspecify.annotations.Nullable;

public class ValidationCollector<T extends @Nullable Object> implements ResultCollector<T, Validation, ValidationCollector<T>> {
    private final Validation validation;

    ValidationCollector(Validation validation) {
        this.validation = validation;
    }

    @Override
    public void add(Result<T> result) {
        result.peekErr(validation::addAll);
    }

    @Override
    public void add(Result<T> result, StringBuilder prefix) {
        result.peekErr(errors -> validation.addAll(errors, prefix));
    }

    @Override
    public ValidationCollector<T> combine(ValidationCollector<T> other) {
        validation.addAll(other.validation);
        return this;
    }

    @Override
    public Validation finish() {
        return validation;
    }
}
