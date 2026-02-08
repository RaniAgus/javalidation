package io.github.raniagus.javalidation;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class JavalidationException extends RuntimeException {
    private final ValidationErrors errors;

    public JavalidationException(ValidationErrors errors) {
        this.errors = errors;
    }

    public JavalidationException(String message, Object... args) {
        this(ValidationErrors.of(message, args));
    }

    public JavalidationException(String field, String message, Object... args) {
        this(ValidationErrors.of(field, message, args));
    }

    public ValidationErrors getErrors() {
        return this.errors;
    }
}
