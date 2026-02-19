package io.github.raniagus.javalidation.validator.processor;

import java.util.List;
import java.util.stream.Stream;

public interface ClassWriter {
    default String fullName() {
        return packageName() + "." + className();
    }

    default void write(ValidationOutput out) {
        out.write("package %s;".formatted(packageName()));
        out.write("");

        List<String> imports = Stream
                .concat(imports(), Stream.of(
                        "javax.annotation.processing.Generated",
                        "org.jspecify.annotations.NullMarked"
                ))
                .filter(s -> !packageName().equals(s.substring(0, s.lastIndexOf('.'))))
                .sorted()
                .distinct()
                .toList();

        for (String importStatement : imports) {
            out.write("import %s;".formatted(importStatement));
        }

        out.write("");
        out.write("""
                @NullMarked
                @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")\
                """);
        writeBody(out);
    }

    String packageName();

    Stream<String> imports();

    String className();

    void writeBody(ValidationOutput out);
}
