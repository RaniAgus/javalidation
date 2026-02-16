package io.github.raniagus.javalidation.format;

import io.github.raniagus.javalidation.FieldKey;

public class DefaultNotationFormatter implements FieldKeyFormatter {
    @Override
    public String format(FieldKey fieldKey) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Object key : fieldKey.parts()) {
            if (key instanceof Number) {
                builder.append('[').append(key).append(']');
            } else if (first) {
                builder.append(key);
            } else {
                builder.append('.').append(key);
            }
            first = false;
        }
        return builder.toString();
    }
}
