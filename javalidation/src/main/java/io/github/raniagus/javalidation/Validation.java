package io.github.raniagus.javalidation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * A mutable builder for accumulating validation errors imperatively.
 * <p>
 * {@code Validation} provides a fluent API for building up validation errors in an imperative style,
 * distinguishing between root-level errors and field-specific errors. Once all errors are accumulated,
 * you can convert to:
 * <ul>
 *   <li>{@link ValidationErrors} (immutable record) via {@link #finish()}</li>
 *   <li>{@link Result} via {@link #asResult(Object)} or {@link #asResult(Supplier)}</li>
 *   <li>Throw {@link JavalidationException} via {@link #check()} or {@link #checkAndGet(Supplier)}</li>
 * </ul>
 * <p>
 * This class is particularly useful for:
 * <ul>
 *   <li>Complex validation logic with conditional error accumulation</li>
 *   <li>Validating multiple fields of an object</li>
 *   <li>Merging validation results from nested objects with prefixes</li>
 * </ul>
 * <p>
 * <b>Basic usage:</b>
 * <pre>{@code
 * Validation validation = Validation.create();
 *
 * if (user.name() == null || user.name().isEmpty()) {
 *     validation.addFieldError("name", "Name is required");
 * }
 * if (user.age() < 18) {
 *     validation.addFieldError("age", "Must be 18 or older");
 * }
 *
 * // Convert to Result
 * Result<User> result = validation.asResult(user);
 *
 * // Or throw if errors present
 * validation.check();
 * }</pre>
 * <p>
 * <b>Nested object validation with prefixes:</b>
 * <pre>{@code
 * Validation validation = Validation.create();
 *
 * // Validate address fields with "address" prefix
 * ValidationErrors addressErrors = validateAddress(user.address());
 * validation.addAll("address", addressErrors);
 * // Errors become: "address.street", "address.zipCode", etc.
 *
 * return validation.asResult(user);
 * }</pre>
 * <p>
 * <b>Thread safety:</b> This class is NOT thread-safe. Each validation operation should use
 * its own {@code Validation} instance.
 *
 * @see ValidationErrors
 * @see Result
 * @see JavalidationException
 */
public class Validation {
    private final List<TemplateString> rootErrors = new ArrayList<>();
    private final Map<FieldKey, List<TemplateString>> fieldErrors = new HashMap<>();

    private Validation() {}

    /**
     * Adds a root-level validation error.
     * <p>
     * Root errors are not associated with any specific field. Use this for general validation
     * failures that apply to the entire object.
     * <p>
     * The message supports MessageFormat placeholders ({0}, {1}, etc.) for internationalization:
     * <pre>{@code
     * validation.addRootError("User must be at least {0} years old", 18);
     * }</pre>
     *
     * @param message the error message template (must not be null)
     * @param args optional arguments for the message template
     * @return this validation for method chaining
     * @throws NullPointerException if message is null
     */
    public Validation addRootError(String message, Object... args) {
        Objects.requireNonNull(message);
        rootErrors.add(new TemplateString(message, args));
        return this;
    }

    private void addRootErrors(List<TemplateString> messages) {
        Objects.requireNonNull(messages);
        rootErrors.addAll(messages);
    }

    /**
     * Adds a field-specific validation error.
     * <p>
     * Multiple errors can be added to the same field, and they will be accumulated in order.
     * <p>
     * The message supports MessageFormat placeholders for internationalization:
     * <pre>{@code
     * validation.addFieldError("age", "Must be at least {0}", 18);
     * validation.addFieldError("email", "Invalid email format");
     * validation.addFieldError("email", "Email already exists");  // second error for same field
     * }</pre>
     *
     * @param field the field name (must not be null)
     * @param message the error message template (must not be null)
     * @param args optional arguments for the message template
     * @return this validation for method chaining
     * @throws NullPointerException if field or message is null
     */
    public Validation addFieldError(String field, String message, Object... args) {
        Objects.requireNonNull(field);
        Objects.requireNonNull(message);
        fieldErrors.computeIfAbsent(FieldKey.of(field), k -> new ArrayList<>(1))
                .add(new TemplateString(message, args));
        return this;
    }

    private void addFieldErrors(FieldKey field, List<TemplateString> messages) {
        Objects.requireNonNull(field);
        Objects.requireNonNull(messages);
        if (!messages.isEmpty()) {
            fieldErrors.computeIfAbsent(field, k -> new ArrayList<>(messages.size()))
                    .addAll(messages);
        }
    }

    private void addFieldErrors(Map<FieldKey, List<TemplateString>> fieldErrors) {
        Objects.requireNonNull(fieldErrors);
        fieldErrors.forEach(this::addFieldErrors);
    }

    /**
     * Merges all errors from another {@link Validation} into this validation.
     * <p>
     * Root errors are added as root errors, field errors are added as field errors with their
     * original field names.
     * <p>
     * Example:
     * <pre>{@code
     * Validation validation = Validation.create();
     * Validation addressErrors = validateAddress(address);
     * validation.addAll(addressErrors);
     * }</pre>
     *
     * @param validation the validation to merge (must not be null)
     * @return this validation for method chaining
     * @throws NullPointerException if errors is null
     */
    public Validation addAll(Validation validation) {
        Objects.requireNonNull(validation);
        addRootErrors(validation.rootErrors);
        addFieldErrors(validation.fieldErrors);
        return this;
    }

    /**
     * Merges all errors from another {@link ValidationErrors} into this validation.
     * <p>
     * Root errors are added as root errors, field errors are added as field errors with their
     * original field names.
     * <p>
     * Example:
     * <pre>{@code
     * Validation validation = Validation.create();
     * ValidationErrors addressErrors = validateAddress(address);
     * validation.addAll(addressErrors);
     * }</pre>
     *
     * @param errors the validation errors to merge (must not be null)
     * @return this validation for method chaining
     * @throws NullPointerException if errors is null
     */
    public Validation addAll(ValidationErrors errors) {
        Objects.requireNonNull(errors);
        addRootErrors(errors.rootErrors());
        addFieldErrors(errors.fieldErrors());
        return this;
    }

    /**
     * Merges all errors from another {@link ValidationErrors} with a field prefix.
     * <p>
     * This is essential for validating nested objects. Root errors from the source become field
     * errors with the given prefix. Field errors have the prefix prepended to their field names
     * with a dot separator.
     * <p>
     * Example:
     * <pre>{@code
     * // Validate nested address
     * ValidationErrors addressErrors = validateAddress(user.address());
     * validation.addAll("address", addressErrors);
     *
     * // If addressErrors had:
     * //   - rootErrors: ["Invalid address"]
     * //   - fieldErrors: {"street": ["Required"], "zipCode": ["Invalid format"]}
     * //
     * // Results in:
     * //   - fieldErrors: {
     * //       "address": ["Invalid address"],
     * //       "address.street": ["Required"],
     * //       "address.zipCode": ["Invalid format"]
     * //     }
     * }</pre>
     *
     * @param errors the validation errors to merge (must not be null)
     * @param prefix the prefix to add to all field paths (must not be null)
     * @return this validation for method chaining
     * @throws NullPointerException if prefix or errors is null
     */
    public Validation addAll(ValidationErrors errors, Object[] prefix) {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(errors);
        if (!errors.rootErrors().isEmpty()) {
            addFieldErrors(FieldKey.of(prefix), errors.rootErrors());
        }
        for (Map.Entry<FieldKey, List<TemplateString>> entry : errors.fieldErrors().entrySet()) {
            addFieldErrors(entry.getKey().withPrefix(prefix), entry.getValue());
        }
        return this;
    }

    /**
     * Converts this mutable validation into an immutable {@link ValidationErrors}.
     * <p>
     * This creates a snapshot of the current errors. Further modifications to this {@code Validation}
     * will not affect the returned {@code ValidationErrors}.
     * <p>
     * Example:
     * <pre>{@code
     * ValidationErrors errors = validation.finish();
     * if (errors.isNotEmpty()) {
     *     // handle errors
     * }
     * }</pre>
     *
     * @return an immutable snapshot of the current validation errors
     */
    public ValidationErrors finish() {
        return new ValidationErrors(rootErrors, fieldErrors);
    }

    /**
     * Converts this validation into a {@link Result}, containing either the provided value or the accumulated errors.
     * <p>
     * If no errors have been accumulated, returns {@link Result.Ok} with the value.
     * If errors exist, returns {@link Result.Err} with the errors.
     * <p>
     * Example:
     * <pre>{@code
     * Validation validation = Validation.create();
     * if (user.age() < 18) {
     *     validation.addFieldError("age", "Must be 18 or older");
     * }
     * return validation.asResult(user);
     * }</pre>
     *
     * @param value the value to wrap in {@link Result.Ok} if no errors exist
     * @param <T> the type of the value
     * @return {@link Result.Ok} with the value if no errors, otherwise {@link Result.Err}
     */
    public <T extends @Nullable Object> Result<T> asResult(T value) {
        return asResult(() -> value);
    }

    /**
     * Converts this validation into a {@link Result}, lazily computing the success value only if no errors exist.
     * <p>
     * The supplier is only called if no errors have been accumulated. This allows expensive object
     * construction to be deferred until validation passes.
     * <p>
     * If the supplier throws {@link JavalidationException}, it is caught and converted to {@link Result.Err}.
     * <p>
     * Example:
     * <pre>{@code
     * return validation.asResult(() -> {
     *     // Only construct if validation passed
     *     return new ExpensiveObject(data);
     * });
     * }</pre>
     *
     * @param supplier supplies the value if no errors exist
     * @param <T> the type of the value
     * @return {@link Result.Ok} with the supplied value if no errors, otherwise {@link Result.Err}
     */
    public <T extends @Nullable Object> Result<T> asResult(Supplier<T> supplier) {
        ValidationErrors errors = finish();
        if (errors.isNotEmpty()) {
            return Result.err(errors);
        }
        return Result.of(supplier);
    }

    /**
     * Throws {@link JavalidationException} if any errors have been accumulated.
     * <p>
     * This is the primary way to fail-fast with validation errors in imperative code.
     * <p>
     * Example:
     * <pre>{@code
     * Validation validation = Validation.create();
     * if (user.age() < 18) {
     *     validation.addFieldError("age", "Must be 18 or older");
     * }
     * validation.check();  // throws if errors exist
     * // Continue with valid user...
     * }</pre>
     *
     * @throws JavalidationException if any errors have been accumulated
     */
    public void check() {
        ValidationErrors errors = finish();
        if (errors.isNotEmpty()) {
            throw JavalidationException.of(errors);
        }
    }

    /**
     * Throws {@link JavalidationException} if any errors exist, otherwise returns the supplied value.
     * <p>
     * This combines {@link #check()} with value retrieval in a single operation.
     * The supplier is only called if validation passes.
     * <p>
     * Example:
     * <pre>{@code
     * User validUser = validation.checkAndGet(() -> user);
     * }</pre>
     *
     * @param supplier supplies the value to return if no errors exist
     * @param <T> the type of the value
     * @return the supplied value
     * @throws JavalidationException if any errors have been accumulated
     */
    public <T extends @Nullable Object> T checkAndGet(Supplier<T> supplier) {
        check();
        return supplier.get();
    }

    /**
     * Creates a new empty {@code Validation} instance.
     * <p>
     * This is the standard factory method for creating validations.
     * <p>
     * Example:
     * <pre>{@code
     * Validation validation = Validation.create();
     * }</pre>
     *
     * @return a new empty validation
     */
    public static Validation create() {
        return new Validation();
    }
}
