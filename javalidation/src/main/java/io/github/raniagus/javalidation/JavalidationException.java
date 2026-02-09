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
 * throw JavalidationException.ofRoot("Invalid request");
 *
 * // Single field error
 * throw JavalidationException.ofField("email", "Invalid email format");
 *
 * // From accumulated errors
 * throw new JavalidationException(validationErrors);
 * }</pre>
 *
 * @see ValidationErrors
 * @see Result
 * @see Validation
 */
public class JavalidationException extends RuntimeException {
    private final ValidationErrors errors;

    /**
     * Creates an exception with the given validation errors.
     *
     * @param errors the accumulated validation errors
     */
    public JavalidationException(ValidationErrors errors) {
        super(buildMessage(errors));
        this.errors = errors;
    }

    /**
     * Creates an exception with a single root error.
     * <p>
     * The message supports MessageFormat placeholders.
     *
     * @param message the error message template
     * @param args arguments for the message template
     */
    public static JavalidationException ofRoot(String message, Object... args) {
        return new JavalidationException(ValidationErrors.ofRoot(message, args));
    }

    /**
     * Creates an exception with a single field error.
     * <p>
     * The message supports MessageFormat placeholders.
     *
     * @param field the field name
     * @param message the error message template
     * @param args arguments for the message template
     */
    public static JavalidationException ofField(String field, String message, Object... args) {
        return new JavalidationException(ValidationErrors.ofField(field, message, args));
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
