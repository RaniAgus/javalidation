package io.github.raniagus.javalidation;

import java.util.Arrays;
import java.util.List;

public record FieldKey(Object[] parts) implements Comparable<FieldKey> {
    public static FieldKey of(Object... key) {
        return new FieldKey(key);
    }

    public static FieldKey of(List<Object> prefix, Object... key) {
        Object[] newKey = Arrays.copyOf(prefix.toArray(), prefix.size() + key.length);
        System.arraycopy(key, 0, newKey, prefix.size(), key.length);
        return new FieldKey(newKey);
    }

    public FieldKey withPrefix(Object... prefix) {
        Object[] newKey = Arrays.copyOf(prefix, prefix.length + parts.length);
        System.arraycopy(parts, 0, newKey, prefix.length, parts.length);
        return new FieldKey(newKey);
    }

    public int compareTo(FieldKey other) {
        return Arrays.toString(parts).compareTo(Arrays.toString(other.parts));
    }

    @Override
    public String toString() {
        return "FieldKey{" +
                "parts=" + Arrays.toString(parts) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FieldKey(Object[] key1))) return false;
        return Arrays.equals(parts(), key1);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(parts());
    }
}
