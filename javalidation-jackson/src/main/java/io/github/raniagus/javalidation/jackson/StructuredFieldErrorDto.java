package io.github.raniagus.javalidation.jackson;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * DTO for serializing and deserializing field-specific validation errors with structured key parts.
 * <p>
 * Unlike string-based field paths (e.g., "user.address[0].street"), this DTO preserves the
 * structured parts of the field key as an array, enabling proper reconstruction of {@link io.github.raniagus.javalidation.FieldKey}.
 * <p>
 * Example:
 * <pre>{@code
 * {
 *   "key": ["user", "address", 0, "street"],
 *   "errors": [
 *     {"message": "Required field", "code": "Required field", "args": []}
 *   ]
 * }
 * }</pre>
 *
 * @param key the field key parts array (e.g., ["user", "address", 0, "street"])
 * @param errors the list of errors for this field
 */
record StructuredFieldErrorDto(Object[] key, List<StructuredErrorDto> errors) {
    /**
     * Compact constructor with defensive copying.
     */
    public StructuredFieldErrorDto {
        key = Arrays.copyOf(key, key.length);
        errors = List.copyOf(errors);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StructuredFieldErrorDto(Object[] k, List<StructuredErrorDto> e))) return false;
        return Arrays.equals(key, k) && Objects.equals(errors, e);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(key);
        result = 31 * result + Objects.hashCode(errors);
        return result;
    }

    @Override
    public String toString() {
        return "StructuredFieldErrorDto{" +
                "key=" + Arrays.toString(key) +
                ", errors=" + errors +
                '}';
    }
}
