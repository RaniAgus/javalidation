package io.github.raniagus.javalidation.processor;

public interface ClassWriter {
    String fullName();
    void write(ValidationOutput out);
}
