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
 *
 * @see DotNotationParser
 * @see FieldKeyNotation#DOTS
 */
public class DotNotationFormatter implements FieldKeyFormatter {
    @Override
    public String format(FieldKey fieldKey) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (FieldKeyPart part : fieldKey.parts()) {
            if (first) {
                builder.append(part);
            } else {
                builder.append('.').append(part);
            }
            first = false;
        }
        return builder.toString();
    }
}
