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
 *
 * @see BracketNotationParser
 * @see FieldKeyNotation#BRACKETS
 */
public class BracketNotationFormatter implements FieldKeyFormatter {
    @Override
    public String format(FieldKey fieldKey) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (FieldKeyPart part : fieldKey.parts()) {
            switch (part) {
                case FieldKeyPart.StringKey s -> {
                    if (!first) builder.append('[');
                    builder.append(s);
                    if (!first) builder.append(']');
                }
                case FieldKeyPart.IntKey i -> builder.append('[')
                        .append(i)
                        .append(']');
            }
            first = false;
        }
        return builder.toString();
    }
}
