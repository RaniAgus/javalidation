package io.github.raniagus.javalidation.validator;

import io.github.raniagus.javalidation.ValidationErrors;
import java.util.HashMap;
import java.util.Map;

public class ValidatorsHolder {
    private final Map<Class<?>, InitializableValidator<?>> validators;

    public ValidatorsHolder(Map<Class<?>, InitializableValidator<?>> validators) {
        this.validators = new HashMap<>(validators);
    }

    public void initialize() {
        for (InitializableValidator<?> validator : validators.values()) {
            validator.initialize(this);
        }
    }

    public boolean hasValidator(Class<?> clazz) {
        return validators.containsKey(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> ValidationErrors validate(T instance) {
        Validator<T> validator = getValidator((Class<T>) instance.getClass());
        return validator.validate(instance);
    }

    @SuppressWarnings("unchecked")
    public <T> Validator<T> getValidator(Class<T> clazz) {
        if (!hasValidator(clazz)) {
            throw new IllegalArgumentException(
                    "No validator registered for " + clazz.getName()
            );
        }
        return (Validator<T>) validators.get(clazz);
    }
}
