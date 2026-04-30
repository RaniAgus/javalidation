package io.github.raniagus.javalidation.format;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.FieldKeyPart;

/**
 * A {@link FieldKeyFormatter} that uses property-path notation.
 * <p>
 * Examples:
 * <ul>
 *   <li>{@code "items"} — single string segment</li>
 *   <li>{@code "[0]"} — single index segment</li>
 *   <li>{@code "items[0].price"} — mixed path</li>
 * </ul>
 */
public class PropertyPathNotationFormatter implements FieldKeyFormatter {
    @Override
    public String format(FieldKey fieldKey) {
        FieldKeyPart[] parts = fieldKey.parts();
        if (parts.length == 0) return "";

        StringBuilder sb = new StringBuilder();
        switch (parts[0]) {
            case FieldKeyPart.StringKey s -> sb.append(s);
            case FieldKeyPart.IntKey i   -> sb.append('[').append(i).append(']');
        }

        for (int idx = 1; idx < parts.length; idx++) {
            switch (parts[idx]) {
                case FieldKeyPart.StringKey s -> sb.append('.').append(s);
                case FieldKeyPart.IntKey i   -> sb.append('[').append(i).append(']');
            }
        }

        return sb.toString();
    }
}
