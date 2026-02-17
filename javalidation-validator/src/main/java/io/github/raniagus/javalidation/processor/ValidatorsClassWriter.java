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
                        "io.github.raniagus.javalidation.ValidationErrors",
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

                    public static boolean hasValidator(Class<?> clazz) {
                        return CACHE.containsKey(clazz);
                    }

                    @SuppressWarnings("unchecked")
                    public static <T> ValidationErrors validate(T instance) {
                        Validator<T> validator = getValidator((Class<T>) instance.getClass());
                        return validator.validate(instance);
                    }

                    @SuppressWarnings("unchecked")
                    public static <T> Validator<T> getValidator(Class<T> clazz) {
                         Validator<?> validator = CACHE.get(clazz);
                         if (validator == null) {
                             throw new IllegalArgumentException(
                                 "No validator registered for " + clazz.getName()
                             );
                         }
                         return (Validator<T>) validator;
                    }
                }
                """);
    }
}
