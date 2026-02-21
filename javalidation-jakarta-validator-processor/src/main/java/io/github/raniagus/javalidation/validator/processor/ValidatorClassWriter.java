package io.github.raniagus.javalidation.validator.processor;

public interface ValidatorClassWriter extends ClassWriter {
    String enclosingClassPrefix();
    String recordName();
    String recordImportName();
}
