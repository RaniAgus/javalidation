package io.github.raniagus.javalidation.validator.processor;

import java.util.List;
import java.util.stream.Stream;

public record ValidatorsClassWriter(List<RecordValidatorClassWriter> classWriters) implements ClassWriter {
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
                        "io.github.raniagus.javalidation.validator.Validator",
                        "io.github.raniagus.javalidation.validator.ValidatorsHolder"
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
                    private static final ValidatorsHolder HOLDER;

                    private %1$s() {}

                    static {
                        HOLDER = new ValidatorsHolder(Map.ofEntries(\
                """.formatted(className()));
        boolean first = true;
        for (RecordValidatorClassWriter writer : classWriters) {
            out.write("""
                                  %s Map.entry(%s%s.class, new %s())\
                    """.formatted(first ? " " : ",", writer.enclosingClassPrefix(), writer.recordName(), writer.className()));
            first = false;
        }
        out.write("""
                        ));
                        HOLDER.initialize();
                    }

                    public static boolean hasValidator(Class<?> clazz) {
                        return HOLDER.hasValidator(clazz);
                    }

                    public static <T> ValidationErrors validate(T instance) {
                         return HOLDER.validate(instance);
                    }

                    public static <T> Validator<T> getValidator(Class<T> clazz) {
                         return HOLDER.getValidator(clazz);
                    }
                }
                """);
    }
}
