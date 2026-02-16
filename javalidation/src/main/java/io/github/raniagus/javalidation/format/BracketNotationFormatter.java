package io.github.raniagus.javalidation.format;

import io.github.raniagus.javalidation.FieldKey;

public class BracketNotationFormatter implements FieldKeyFormatter {
    @Override
    public String format(FieldKey fieldKey) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Object key : fieldKey.parts()) {
            if (first && !(key instanceof Number)) {
                builder.append(key);
            } else {
                builder.append('[').append(key).append(']');
            }
            first = false;
        }
        return builder.toString();
    }
}
