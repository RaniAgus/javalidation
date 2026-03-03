package io.github.raniagus.javalidation.format;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.FieldKeyPart;

public class PropertyPathNotationFormatter implements FieldKeyFormatter {
    @Override
    public String format(FieldKey fieldKey) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (FieldKeyPart part : fieldKey.parts()) {
            switch (part) {
                case FieldKeyPart.StringKey s -> {
                    if (first) {
                        builder.append(s);
                    } else {
                        builder.append('.').append(s);
                    }
                }
                case FieldKeyPart.IntKey i ->
                    builder.append('[').append(i).append(']');
            }
            first = false;
        }
        return builder.toString();
    }
}
