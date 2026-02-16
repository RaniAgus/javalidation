package io.github.raniagus.javalidation.processor;

import java.util.List;
import java.util.stream.Stream;

public record ValidatorsClassWriter(List<ValidatorClassWriter> classWriters) implements ClassWriter {
    @Override
    public String packageName() {
        return "io.github.raniagus.javalidation.validator";
    }

    @Override
    public Stream<String> imports() {
        return Stream.concat(
                Stream.of(
                        "java.util.Map",
                        "io.github.raniagus.javalidation.validator.Validator"
                ),
                classWriters.stream().flatMap(writer -> Stream.concat(
                        Stream.of(writer.fullName()),
                        Stream.of(writer.recordImportName())
                ))
        );
    }

    @Override
    public String className() {
        return "Validators";
    }

    @Override
    public void writeBody(ValidationOutput out) {
        out.write("""
                public final class %1$s {
                    private static final Map<Class<?>, Validator<?>> CACHE;

                    private %1$s() {}

                    static {
                        CACHE = Map.ofEntries(\
                """.formatted(className()));
        boolean first = true;
        for (ValidatorClassWriter writer : classWriters) {
            out.write("""
                                  %s Map.entry(%s%s.class, new %s())\
                    """.formatted(first ? " " : ",", writer.enclosingClassPrefix(), writer.recordName(), writer.className()));
            first = false;
        }
        out.write("""
                        );
                    }
                
                    @SuppressWarnings("unchecked")
                    public static <T> Validator<T> getValidator(Class<T> clazz) {
                        return (Validator<T>) CACHE.get(clazz);
                    }
                }
                """);
    }
}
