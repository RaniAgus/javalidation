package io.github.raniagus.javalidation.validator;

import io.github.raniagus.javalidation.ValidationErrors;

public final class Validators {
    private Validators() {}

    public static boolean hasValidator(Class<?> clazz) {
        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T> ValidationErrors validate(T instance) {
        Validator<T> validator = getValidator((Class<T>) instance.getClass());
        return validator.validate(instance);
    }

    public static <T> Validator<T> getValidator(Class<T> type) {
        throw new IllegalStateException(
                "No generated mapper registry found for " + type.getName() +
                ". Is the annotation processor enabled?"
        );
    }
}
