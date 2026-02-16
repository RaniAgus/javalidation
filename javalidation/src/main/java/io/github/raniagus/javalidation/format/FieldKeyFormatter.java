package io.github.raniagus.javalidation.format;

import io.github.raniagus.javalidation.FieldKey;

@FunctionalInterface
public interface FieldKeyFormatter {
    String format(FieldKey fieldKey);

    static FieldKeyFormatter getDefault() {
        return new DefaultNotationFormatter();
    }
}
