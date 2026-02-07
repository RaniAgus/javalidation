package io.github.raniagus.javalidation;

public class ValidationException extends RuntimeException {
    private final ValidationErrors errors;

    public ValidationException(ValidationErrors errors) {
        this.errors = errors;
    }

    public ValidationException(String message, Object... args) {
        this(ValidationErrors.of(message, args));
    }

    public ValidationException(String field, String message, Object... args) {
        this(ValidationErrors.of(field, message, args));
    }

    public ValidationErrors getErrors() {
        return this.errors;
    }
}
