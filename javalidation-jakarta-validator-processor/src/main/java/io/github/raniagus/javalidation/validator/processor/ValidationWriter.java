package io.github.raniagus.javalidation.validator.processor;

import java.util.stream.Stream;

public interface ValidationWriter {
    default Stream<String> imports() {
        return Stream.empty();
    }

    default void writePropertiesTo(ValidationOutput out) {}

    void writeBodyTo(ValidationOutput out);
}
