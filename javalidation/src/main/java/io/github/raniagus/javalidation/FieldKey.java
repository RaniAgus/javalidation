package io.github.raniagus.javalidation;

import io.github.raniagus.javalidation.format.FieldKeyParser;
import java.util.Arrays;
import java.util.Collection;

/**
 * Represents the path to a field in a validated object, composed of one or more {@link FieldKeyPart} segments.
 * <p>
 * A {@code FieldKey} models paths like {@code "address.street"} (two {@link FieldKeyPart.StringKey} segments)
 * or {@code "items[0].price"} (a {@link FieldKeyPart.StringKey}, a {@link FieldKeyPart.IntKey}, and another
 * {@link FieldKeyPart.StringKey}).
 * <p>
 * Instances are created via the factory methods:
 * <ul>
 *   <li>{@link #of(String...)} – from string segments</li>
 *   <li>{@link #of(Object...)} – from mixed strings and integers (integers become {@link FieldKeyPart.IntKey})</li>
 *   <li>{@link #of(FieldKeyPart...)} – from explicit parts</li>
 *   <li>{@link #of(Collection, FieldKeyPart...)} – from a prefix list plus additional parts</li>
 * </ul>
 * <p>
 * Comparison is lexicographic: segments are compared element-by-element, with {@link FieldKeyPart.StringKey}
 * considered less than {@link FieldKeyPart.IntKey} at the same position.
 *
 * @param parts the ordered segments that form this field path
 * @see FieldKeyPart
 * @see ValidationErrors
 */
public record FieldKey(FieldKeyPart[] parts) implements Comparable<FieldKey> {

    /**
     * Creates a {@code FieldKey} from one or more string segments.
     * <p>
     * Example: {@code FieldKey.of("user", "address")} produces a key for the path {@code "user.address"}.
     *
     * @param path the string segments
     * @return a new {@code FieldKey}
     */
    public static FieldKey of(String... path) {
        return new FieldKey(FieldKeyPart.ofPath(path));
    }

    /**
     * Creates a {@code FieldKey} from one or more number segments.
     * <p>
     * Each element is converted to an {@code int} via {@link Number#intValue()}.
     *
     * @param path the numeric segments
     * @return a new {@code FieldKey}
     */
    public static FieldKey of(Number... path) {
        return new FieldKey(FieldKeyPart.ofPath(path));
    }

    /**
     * Creates a {@code FieldKey} from a mixed sequence of strings and integers.
     * <p>
     * Each {@link Number} element becomes a {@link FieldKeyPart.IntKey}; all other elements are
     * converted to strings via {@link Object#toString()} and become {@link FieldKeyPart.StringKey}.
     * <p>
     * Example: {@code FieldKey.of("items", 0, "price")} produces the path {@code "items[0].price"}.
     *
     * @param path the mixed segments
     * @return a new {@code FieldKey}
     * @throws IllegalArgumentException if any element is neither a {@link String} nor a {@link Number}
     */
    public static FieldKey of(Object... path) {
        return new FieldKey(FieldKeyPart.ofPath(path));
    }

    /**
     * Creates a {@code FieldKey} directly from {@link FieldKeyPart} instances.
     *
     * @param parts the parts
     * @return a new {@code FieldKey}
     */
    public static FieldKey of(FieldKeyPart... parts) {
        return new FieldKey(parts);
    }

    /**
     * Parses a field-path string into a {@code FieldKey} using the default
     * {@link FieldKeyParser} (property-path notation, e.g. {@code "items[0].price"}).
     *
     * @param path the string to parse
     * @return the parsed {@code FieldKey}
     * @throws IllegalArgumentException if the string is not valid property-path notation
     */
    public static FieldKey parse(String path) {
        return FieldKeyParser.getDefault().parse(path);
    }

    /**
     * Creates a {@code FieldKey} by prepending a prefix list to additional parts.
     * <p>
     * Used internally when building keys from a scoped {@link Validation} prefix.
     *
     * @param prefix the leading parts
     * @param path   the trailing parts to append after the prefix
     * @return a new {@code FieldKey}
     */
    public static FieldKey of(Collection<FieldKeyPart> prefix, FieldKeyPart... path) {
        FieldKeyPart[] newKey = new FieldKeyPart[prefix.size() + path.length];
        prefix.toArray(newKey);
        System.arraycopy(path, 0, newKey, prefix.size(), path.length);
        return new FieldKey(newKey);
    }

    /**
     * Returns a new {@code FieldKey} with the given segments prepended to this key's parts.
     *
     * @param prefix the segments to prepend
     * @return a new {@code FieldKey} with the prefix prepended
     */
    public FieldKey withPrefix(FieldKeyPart... prefix) {
        FieldKeyPart[] newKey = Arrays.copyOf(prefix, prefix.length + parts.length);
        System.arraycopy(parts, 0, newKey, prefix.length, parts.length);
        return new FieldKey(newKey);
    }

    public int compareTo(FieldKey other) {
        int minLength = Math.min(parts.length, other.parts.length);
        for (int i = 0; i < minLength; i++) {
            int cmp = parts[i].compareTo(other.parts[i]);
            if (cmp != 0) {
                return cmp;
            }
        }
        return Integer.compare(parts.length, other.parts.length);
    }

    @Override
    public String toString() {
        return "FieldKey{" +
                "parts=" + Arrays.toString(parts) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FieldKey(FieldKeyPart[] key1))) return false;
        return Arrays.equals(parts(), key1);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(parts());
    }
}
