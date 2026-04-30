package io.github.raniagus.javalidation.format;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.FieldKeyPart;

/**
 * A {@link FieldKeyFormatter} that uses dot notation.
 * <p>
 * Examples:
 * <ul>
 *   <li>{@code "items"} — single string segment</li>
 *   <li>{@code "0"} — single index segment</li>
 *   <li>{@code "items.0.price"} — mixed path</li>
 * </ul>
 */
public class DotNotationFormatter implements FieldKeyFormatter {
    @Override
    public String format(FieldKey fieldKey) {
        FieldKeyPart[] parts = fieldKey.parts();
        if (parts.length == 0) return "";

        StringBuilder sb = new StringBuilder();
        sb.append(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            sb.append('.').append(parts[i]);
        }
        return sb.toString();
    }
}
