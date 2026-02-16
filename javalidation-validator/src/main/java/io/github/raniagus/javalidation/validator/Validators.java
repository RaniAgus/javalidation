package io.github.raniagus.javalidation.validator;

public final class Validators {
    private Validators() {}

    public static <T> Validator<T> getValidator(Class<T> type) {
        throw new IllegalStateException(
                "No generated mapper registry found for " + type.getName() +
                ". Is the annotation processor enabled?"
        );
    }
}
