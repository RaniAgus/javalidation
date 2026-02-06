package io.github.raniagus.javalidation;

public class ValidationException extends RuntimeException {
    private final ValidationErrors errors;

    public ValidationException(String message) {
        super(message);
        this.errors = ValidationErrors.of(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errors = ValidationErrors.of(message);
    }

    public ValidationException(String field, String message) {
        super(field + ": " + message);
        this.errors = ValidationErrors.of(field, message);
    }

    public ValidationException(String field, String message, Throwable cause) {
        super(field + ": " + message, cause);
        this.errors = ValidationErrors.of(field, message);
    }

    public ValidationException(ValidationErrors errors) {
        this.errors = errors;
    }

    public ValidationErrors getErrors() {
        return this.errors;
    }
}
