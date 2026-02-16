package io.github.raniagus.javalidation.format;

import io.github.raniagus.javalidation.FieldKey;

public class DotNotationFormatter implements FieldKeyFormatter {
    @Override
    public String format(FieldKey fieldKey) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Object key : fieldKey.parts()) {
            if (first) {
                builder.append(key);
            } else {
                builder.append('.').append(key);
            }
            first = false;
        }
        return builder.toString();
    }
}
