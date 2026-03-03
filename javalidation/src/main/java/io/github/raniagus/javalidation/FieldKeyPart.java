package io.github.raniagus.javalidation;

/**
 * A type-safe part of a {@link FieldKey} path, representing either a named string segment or a
 * numeric index segment.
 * <p>
 * {@code FieldKeyPart} is a sealed interface with two concrete implementations:
 * <ul>
 *   <li>{@link StringKey} – a named field segment, e.g. {@code "address"} or {@code "items"}</li>
 *   <li>{@link IntKey} – a numeric index segment, e.g. {@code 0} or {@code 1}</li>
 * </ul>
 * <p>
 * String segments are considered less than integer segments when compared.
 *
 * @see FieldKey
 */
public sealed interface FieldKeyPart extends Comparable<FieldKeyPart> {
    /** Returns the raw value of this part as an {@link Object}: a {@link String} or {@link Integer}. */
    Object objValue();

    static FieldKeyPart of(Object key) {
        if (key instanceof String s) {
            return new StringKey(s);
        } else if (key instanceof Number i) {
            return new IntKey(i.intValue());
        } else {
            throw new IllegalArgumentException("Unsupported key type: " + key.getClass().getName());
        }
    }

    /**
     * A named string segment in a field path, e.g. {@code "address"} or {@code "items"}.
     *
     * @param key the field name
     */
    record StringKey(String key) implements FieldKeyPart {
        @Override
        public String objValue() {
            return key;
        }

        @Override
        public String toString() {
            return key;
        }

        @Override
        public int compareTo(FieldKeyPart o) {
            if (o instanceof StringKey(String key1)) {
                return key.compareTo(key1);
            } else {
                return -1; // StringKey is considered less than IntKey
            }
        }
    }

    /**
     * A numeric index segment in a field path, e.g. {@code 0} or {@code 1}.
     *
     * @param key the 0-based index
     */
    record IntKey(int key) implements FieldKeyPart {
        @Override
        public Integer objValue() {
            return key;
        }

        @Override
        public String toString() {
            return Integer.toString(key);
        }

        @Override
        public int compareTo(FieldKeyPart o) {
            if (o instanceof IntKey(int key1)) {
                return Integer.compare(key, key1);
            } else {
                return 1; // IntKey is considered greater than StringKey
            }
        }
    }
}
