package io.github.raniagus.javalidation.validator.processor;

import java.util.stream.Stream;

public interface ValidationWriter {
    default Stream<String> imports() {
        return Stream.empty();
    }

    void writeBodyTo(ValidationOutput out);
}
