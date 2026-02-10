package io.github.raniagus.javalidation;

import io.github.raniagus.javalidation.format.TemplateString;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Immutable container for accumulated validation errors.
 * <p>
 * Validation errors are organized into two categories:
 * <ul>
 *   <li><b>Root errors</b>: General validation failures not tied to specific fields</li>
 *   <li><b>Field errors</b>: Validation failures associated with specific field names</li>
 * </ul>
 * <p>
 * This record provides deep immutability through defensive copying in its compact constructor,
 * ensuring thread-safety and safe sharing across contexts.
 * <p>
 * Errors are stored as {@link TemplateString} instances, which defer formatting until serialization
 * time. This enables internationalization support where error messages can be formatted differently
 * based on locale.
 * <p>
 * <b>Creating validation errors:</b>
 * <pre>{@code
 * // Single root error
 * ValidationErrors errors = ValidationErrors.of("Invalid input");
 *
 * // Single field error
 * ValidationErrors errors = ValidationErrors.of("email", "Invalid email format");
 *
 * // Build complex errors
 * ValidationErrors errors = Validation.create()
 *     .addRootError("User validation failed")
 *     .addFieldError("name", "Name is required")
 *     .addFieldError("age", "Must be at least {0}", 18)
 *     .finish();
 * }</pre>
 * <p>
 * <b>Hierarchical error composition:</b>
 * <pre>{@code
 * // Validate nested objects with prefixes
 * ValidationErrors addressErrors = validateAddress(address);
 * ValidationErrors prefixed = addressErrors.withPrefix("user.address");
 * // Errors become: "user.address.street", "user.address.zipCode", etc.
 * }</pre>
 * <p>
 * <b>Merging errors:</b>
 * <pre>{@code
 * ValidationErrors merged = errors1.mergeWith(errors2);
 * }</pre>
 *
 * @param rootErrors list of root-level validation errors
 * @param fieldErrors map of field names to their validation errors
 * @see Validation
 * @see TemplateString
 * @see Result.Err
 */
public record ValidationErrors(
        List<TemplateString> rootErrors,
        Map<String, List<TemplateString>> fieldErrors
) {
    /**
     * Compact constructor that ensures deep immutability through defensive copying.
     * <p>
     * Creates unmodifiable copies of both the root errors list and the field errors map,
     * including copies of each list within the map.
     */
    public ValidationErrors {
        rootErrors = List.copyOf(rootErrors);
        fieldErrors = Map.copyOf(fieldErrors.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> List.copyOf(e.getValue()))));
    }

    /**
     * Creates an empty {@code ValidationErrors} with no errors.
     * <p>
     * Example:
     * <pre>{@code
     * ValidationErrors empty = ValidationErrors.empty();
     * assert empty.isEmpty();
     * }</pre>
     *
     * @return an empty validation errors instance
     */
    public static ValidationErrors empty() {
        return new ValidationErrors(List.of(), Map.of());
    }

    /**
     * Creates a {@code ValidationErrors} with a single root error.
     * <p>
     * The message supports MessageFormat placeholders ({0}, {1}, etc.):
     * <pre>{@code
     * ValidationErrors errors = ValidationErrors.of("User must be at least {0} years old", 18);
     * }</pre>
     *
     * @param message the error message template
     * @param args optional arguments for the message template
     * @return validation errors containing the single root error
     */
    public static ValidationErrors ofRoot(String message, Object... args) {
        return new ValidationErrors(List.of(new TemplateString(message, args)), Map.of());
    }

    /**
     * Creates a {@code ValidationErrors} with a single field error.
     * <p>
     * The message supports MessageFormat placeholders:
     * <pre>{@code
     * ValidationErrors errors = ValidationErrors.of("age", "Must be at least {0}", 18);
     * }</pre>
     *
     * @param field the field name
     * @param message the error message template
     * @param args optional arguments for the message template
     * @return validation errors containing the single field error
     */
    public static ValidationErrors ofField(String field, String message, Object... args) {
        return new ValidationErrors(List.of(), Map.of(field, List.of(new TemplateString(message, args))));
    }

    /**
     * Merges this {@code ValidationErrors} with another, combining all errors.
     * <p>
     * Root errors from both are combined into a single list. Field errors for the same field
     * are concatenated.
     * <p>
     * Example:
     * <pre>{@code
     * ValidationErrors errors1 = ValidationErrors.of("email", "Invalid format");
     * ValidationErrors errors2 = ValidationErrors.of("email", "Already exists");
     * ValidationErrors merged = errors1.mergeWith(errors2);
     * // merged has two errors for "email" field
     * }</pre>
     *
     * @param other the validation errors to merge with this one
     * @return a new {@code ValidationErrors} containing all errors from both
     */
    public ValidationErrors mergeWith(ValidationErrors other) {
        return Validation.create()
                .addAll(this)
                .addAll(other)
                .finish();
    }

    /**
     * Returns a new {@code ValidationErrors} with all field paths prefixed.
     * <p>
     * This is essential for validating nested objects and maintaining hierarchical error paths.
     * Root errors are converted to field errors with the given prefix. Field errors have the
     * prefix prepended with a dot separator.
     * <p>
     * Example:
     * <pre>{@code
     * // Original errors:
     * // - rootErrors: ["Invalid address"]
     * // - fieldErrors: {"street": ["Required"], "zipCode": ["Invalid"]}
     *
     * ValidationErrors prefixed = errors.withPrefix("user.address");
     *
     * // Result:
     * // - rootErrors: []
     * // - fieldErrors: {
     * //     "user.address": ["Invalid address"],
     * //     "user.address.street": ["Required"],
     * //     "user.address.zipCode": ["Invalid"]
     * //   }
     * }</pre>
     *
     * @param prefix the prefix to add to all field paths
     * @return a new {@code ValidationErrors} with prefixed field paths
     * @see #withPrefix(Object, Object...)
     */
    public ValidationErrors withPrefix(String prefix) {
        Map<String, List<TemplateString>> prefixedFieldErrors = new HashMap<>(fieldErrors.size() + 1);
        if (!rootErrors.isEmpty()) {
            prefixedFieldErrors.put(prefix, rootErrors);
        }
        String dotPrefix = prefix + '.';
        for (Map.Entry<String, List<TemplateString>> entry : fieldErrors.entrySet()) {
            prefixedFieldErrors.put(dotPrefix + entry.getKey(), entry.getValue());
        }
        return new ValidationErrors(List.of(), prefixedFieldErrors);
    }

    /**
     * Returns a new {@code ValidationErrors} with all field paths prefixed by concatenating the given objects.
     * <p>
     * This is a convenience method for building prefixes from multiple parts, particularly useful
     * for array/list indices.
     * <p>
     * Example:
     * <pre>{@code
     * for (int i = 0; i < items.size(); i++) {
     *     ValidationErrors itemErrors = validateItem(items.get(i));
     *     ValidationErrors prefixed = itemErrors.withPrefix("items[", i, "]");
     *     // produces: "items[0]", "items[1]", etc.
     * }
     * }</pre>
     *
     * @param first the first part of the prefix
     * @param rest additional parts to concatenate
     * @return a new {@code ValidationErrors} with prefixed field paths
     * @see #withPrefix(String)
     */
    public ValidationErrors withPrefix(Object first, Object... rest) {
        // Pre-calculate prefix once
        StringBuilder sb = new StringBuilder();
        sb.append(first);
        for (Object o : rest) {
            sb.append(o);
        }
        return withPrefix(sb.toString());
    }

    /**
     * Returns {@code true} if there are no errors (both root and field errors are empty).
     * <p>
     * Example:
     * <pre>{@code
     * if (errors.isEmpty()) {
     *     // validation passed
     * }
     * }</pre>
     *
     * @return {@code true} if no errors exist
     */
    public boolean isEmpty() {
        return rootErrors.isEmpty() && fieldErrors.isEmpty();
    }

    /**
     * Returns {@code true} if there are any errors (either root or field errors).
     * <p>
     * Example:
     * <pre>{@code
     * if (errors.isNotEmpty()) {
     *     // validation failed, handle errors
     * }
     * }</pre>
     *
     * @return {@code true} if any errors exist
     */
    public boolean isNotEmpty() {
        return !isEmpty();
    }

    /**
     * Returns the total number of errors (root and field).
     * <p>
     * Example:
     * <pre>{@code
     * logger.warn("Validation failed with {} errors", errors.errorCount());
     * }</pre>
     *
     * @return the total number of errors
     */
    public int count() {
        return rootErrors.size() + fieldErrors.values().stream().mapToInt(List::size).sum();
    }
}
