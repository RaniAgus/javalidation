package io.github.raniagus.javalidation.format;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.FieldKeyPart;

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
