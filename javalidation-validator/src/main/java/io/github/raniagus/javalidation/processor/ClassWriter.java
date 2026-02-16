package io.github.raniagus.javalidation.processor;

import java.util.stream.Stream;

public interface ClassWriter {
    default void write(ValidationOutput out) {
        out.write("package %s;".formatted(packageName()));
        out.write("");
        for (String importStatement : imports().sorted().toList()) {
            out.write("import %s;".formatted(importStatement));
        }
        out.write("");
        writeBody(out);
    }

    String packageName();

    Stream<String> imports();

    String fullName();

    void writeBody(ValidationOutput out);
}
