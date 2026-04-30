package io.github.raniagus.javalidation.format;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.FieldKeyPart;

/**
 * A {@link FieldKeyFormatter} that uses bracket notation.
 * <p>
 * Examples:
 * <ul>
 *   <li>{@code "items"} — single string segment (no brackets for the leading bare name)</li>
 *   <li>{@code "[0]"} — single index segment</li>
 *   <li>{@code "items[0][price]"} — mixed path (all non-first segments wrapped in {@code []})</li>
 * </ul>
 */
public class BracketNotationFormatter implements FieldKeyFormatter {
    @Override
    public String format(FieldKey fieldKey) {
        FieldKeyPart[] parts = fieldKey.parts();
        if (parts.length == 0) return "";

        StringBuilder sb = new StringBuilder();

        switch (parts[0]) {
            case FieldKeyPart.StringKey s -> sb.append(s.objValue());
            case FieldKeyPart.IntKey i -> sb.append('[').append(i.objValue()).append(']');
        }

        for (int i = 1; i < parts.length; i++) {
            switch (parts[i]) {
                case FieldKeyPart.StringKey s -> sb.append('[').append(s.objValue()).append(']');
                case FieldKeyPart.IntKey i2 -> sb.append('[').append(i2.objValue()).append(']');
            }
        }

        return sb.toString();
    }
}
