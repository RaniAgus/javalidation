package io.github.raniagus.javalidation;

/**
 * Unchecked exception thrown when validation fails.
 * <p>
 * This exception carries accumulated {@link ValidationErrors} and is thrown by:
 * <ul>
 *   <li>{@link Result#getOrThrow()} when the result is {@link Result.Err}</li>
 *   <li>{@link Validation#check()} when errors have been accumulated</li>
 *   <li>{@link Validation#checkAndGet(java.util.function.Supplier)} when errors exist</li>
 * </ul>
 * <p>
 * Unlike checked exceptions, {@code JavalidationException} allows validation errors to propagate
 * up the call stack without forcing intermediate methods to declare or handle them. This is appropriate
 * for validation failures which are often handled at application boundaries (controllers, service layers).
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * try {
 *     Result<User> result = validateUser(user);
 *     User validUser = result.getOrThrow();  // may throw
 *     // proceed with valid user
 * } catch (JavalidationException e) {
 *     ValidationErrors errors = e.getErrors();
 *     // handle validation errors (log, return to client, etc.)
 * }
 * }</pre>
 * <p>
 * <b>Creating directly:</b>
 * <pre>{@code
 * // Single root error
 * throw JavalidationException.of("Invalid request");
 *
 * // Single field error
 * throw JavalidationException.at("email", "Invalid email format");
 *
 * // From accumulated errors
 * throw JavalidationException.of(validationErrors);
 * }</pre>
 *
 * @see ValidationErrors
 * @see Result
 * @see Validation
 */
public class JavalidationException extends RuntimeException {
    private final ValidationErrors errors;

    public JavalidationException(ValidationErrors errors) {
        super(buildMessage(errors));
        this.errors = errors;
    }

    private JavalidationException(ValidationErrors errors, JavalidationException cause) {
        super(buildMessage(errors), cause);
        this.errors = errors;
    }

    /**
     * Creates an exception with the given validation errors.
     *
     * @param errors the accumulated validation errors
     */
    public static JavalidationException of(ValidationErrors errors) {
        return new JavalidationException(errors);
    }

    /**
     * Creates an exception with a single root error.
     * <p>
     * The message supports MessageFormat placeholders.
     *
     * @param message the error message template
     * @param args arguments for the message template
     */
    public static JavalidationException of(String message, Object... args) {
        return new JavalidationException(ValidationErrors.of(message, args));
    }

    /**
     * Creates an exception with a single field error.
     * <p>
     * The message supports MessageFormat placeholders.
     *
     * @param field the field name or identifier
     * @param message the error message template
     * @param args arguments for the message template
     */
    public static JavalidationException at(String field, String message, Object... args) {
        return new JavalidationException(ValidationErrors.at(field, message, args));
    }

    /**
     * @see #at(String, String, Object...)
     * @param field the field index
     * @param message the error message template
     * @param args arguments for the message template
     */
    public static JavalidationException at(int field, String message, Object... args) {
        return new JavalidationException(ValidationErrors.at(field, message, args));
    }

    /**
     * Returns the validation errors carried by this exception.
     *
     * @return the validation errors
     */
    public ValidationErrors getErrors() {
        return this.errors;
    }

    /**
     * Returns a new {@code JavalidationException} with all field paths prefixed with the given string segments.
     * <p>
     * Useful when catching an exception thrown by a nested validator and rethrowing it with context:
     * <pre>{@code
     * try {
     *     validateAddress(user.address());
     * } catch (JavalidationException e) {
     *     throw e.withPrefix("address");
     *     // field "street" becomes "address.street", root errors become "address"
     * }
     * }</pre>
     * The original exception is set as the {@link #getCause() cause} of the returned one,
     * so the original throw site remains visible in the stack trace.
     *
     * @param prefix the string parts to prepend
     * @return a new exception wrapping the prefixed errors, with this exception as its cause
     */
    public JavalidationException withPrefix(String... prefix) {
        return new JavalidationException(errors.withPrefix(prefix), this);
    }

    /**
     * Returns a new {@code JavalidationException} with all field paths prefixed with the given numeric index segments.
     * The original exception is set as the cause of the returned one.
     *
     * @param prefix the numeric parts to prepend
     * @return a new exception wrapping the prefixed errors, with this exception as its cause
     * @see #withPrefix(String...)
     */
    public JavalidationException withPrefix(Number... prefix) {
        return new JavalidationException(errors.withPrefix(prefix), this);
    }

    /**
     * Returns a new {@code JavalidationException} with all field paths prefixed with the given mixed segments.
     * Each element is treated as a string segment unless it is a {@link Number}, which becomes an index segment.
     * The original exception is set as the cause of the returned one.
     *
     * @param prefix the parts to prepend, mixed strings and numbers
     * @return a new exception wrapping the prefixed errors, with this exception as its cause
     * @see #withPrefix(String...)
     */
    public JavalidationException withPrefix(Object... prefix) {
        return new JavalidationException(errors.withPrefix(prefix), this);
    }

    /**
     * Builds a concise exception message summarizing the validation errors.
     * <p>
     * The message format is: "Validation failed with N error(s)" where N is the total
     * count of root errors plus field errors.
     *
     * @param errors the validation errors
     * @return a summary message string
     */
    private static String buildMessage(ValidationErrors errors) {
        return "Validation failed with " + errors.count() + " error(s)";
    }
}
