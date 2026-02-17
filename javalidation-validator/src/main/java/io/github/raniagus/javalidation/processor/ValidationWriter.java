package io.github.raniagus.javalidation.processor;

import java.util.stream.Stream;

public sealed interface ValidationWriter {
    default Stream<String> imports() {
        return Stream.empty();
    }

    default void writePropertiesTo(ValidationOutput out, String field) {
    }

    void writeBodyTo(ValidationOutput out);

    sealed interface NullSafeWriter extends ValidationWriter {}
    sealed interface NullUnsafeWriter extends ValidationWriter {}

    record NotNull(String message) implements NullSafeWriter {
        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    if (%s == null) {\
                    """.formatted(out.getVariable()));
            out.incrementIndentationLevel();
            out.write("""
                    %sValidation.addRootError("%s");\
                    """.formatted(out.getVariable(), message));
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

    record NullSafeCondition(String accessor, String message) implements NullSafeWriter {
        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    if (%1$s == null || %1$s.%2$s()) {\
                    """.formatted(out.getVariable(), accessor));
            out.incrementIndentationLevel();
            out.write("""
                    %sValidation.addRootError("%s");\
                    """.formatted(out.getVariable(), message));
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

    record Size(
            String accessor,
            String message,
            int min,
            int max
    ) implements NullUnsafeWriter {
        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    if (%1$s.%2$s() < %3$d || %1$s.%2$s() > %4$d) {\
                    """.formatted(out.getVariable(), accessor, min, max));
            out.incrementIndentationLevel();
            out.write("""
                    %sValidation.addRootError("%s", %d, %d);\
                    """.formatted(out.getVariable(), message, min, max));
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

    record MoreThan(String message, long value) implements NullUnsafeWriter {
        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    if (%s <= %d) {\
                    """.formatted(out.getVariable(), value));
            out.incrementIndentationLevel();
            out.write("""
                    %sValidation.addRootError("%s", %d);\
                    """.formatted(out.getVariable(), message, value));
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

    record MoreThanOrEqual(String message, long value) implements NullUnsafeWriter {
        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    if (%s < %d) {\
                    """.formatted(out.getVariable(), value));
            out.incrementIndentationLevel();
            out.write("""
                    %sValidation.addRootError("%s", %d);\
                    """.formatted(out.getVariable(), message, value));
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

    record LessThan(String message, long value) implements NullUnsafeWriter {
        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    if (%s >= %d) {\
                    """.formatted(out.getVariable(), value));
            out.incrementIndentationLevel();
            out.write("""
                    %sValidation.addRootError("%s", %d);\
                    """.formatted(out.getVariable(), message, value));
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

    record LessThanOrEqual(String message, long value) implements NullUnsafeWriter {
        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    if (%s > %d) {\
                    """.formatted(out.getVariable(), value));
            out.incrementIndentationLevel();
            out.write("""
                    %sValidation.addRootError("%s", %d);\
                    """.formatted(out.getVariable(), message, value));
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

    record Pattern(String regex, String message) implements NullUnsafeWriter {
        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    if (!%s.matches("%s")) {\
                    """.formatted(out.getVariable(), regex));
            out.incrementIndentationLevel();
            out.write("""
                    %sValidation.addRootError("%s");\
                    """.formatted(out.getVariable(), message));
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

    record Validate(
            String referredTypeImport, // "com.example.RecordName"
            String referredTypeEnclosingClassPrefix, // "" or "EnclosingClass."
            String referredTypeName, // "RecordName"
            String referredValidatorName, // "RecordNameValidator" or "EnclosingClass$RecordNameValidator"
            String referredValidatorFullName // "com.example.RecordNameValidator" or "com.example.EnclosingClass$RecordNameValidator"
    ) implements NullUnsafeWriter {
        @Override
        public Stream<String> imports() {
            return Stream.of(referredTypeImport, referredValidatorFullName);
        }

        @Override
        public void writePropertiesTo(ValidationOutput out, String field) {
            out.write("""
                    private final Validator<%s%s> %s = new %s();
                    """.formatted(
                            referredTypeEnclosingClassPrefix,
                            referredTypeName,
                            validatorProperty(field),
                            referredValidatorName
                    ));
        }

        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    %sValidation.addAll(%s.validate(%s));\
                    """.formatted(out.getVariable(), validatorProperty(out.getVariable()), out.getVariable()));
        }

        private String validatorProperty(String field) {
            return field + "Validator";
        }
    }
}
