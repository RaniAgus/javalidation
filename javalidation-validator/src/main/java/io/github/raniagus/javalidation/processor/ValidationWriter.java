package io.github.raniagus.javalidation.processor;

import java.util.stream.Stream;

public sealed interface ValidationWriter {
    default Stream<String> imports() {
        return Stream.empty();
    }

    default void writePropertiesTo(ValidationOutput out, String field) {
    }

    void writeBodyTo(ValidationOutput out, String field);

    sealed interface NullSafeWriter extends ValidationWriter {}
    sealed interface NullUnsafeWriter extends ValidationWriter {}

    record NotNull(String message) implements NullSafeWriter {
        @Override
        public void writeBodyTo(ValidationOutput out, String field) {
            out.write("""
                    if (%s.%s() == null) {\
                    """.formatted(out.getVariable(), field));
            out.incrementIndentationLevel();
            out.write("""
                    validation.addFieldError("%s", "%s");\
                    """.formatted(out.getFullKey(field), message));
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

    record NullSafeCondition(String accessor, String message) implements NullSafeWriter {
        @Override
        public void writeBodyTo(ValidationOutput out, String field) {
            out.write("""
                    if (%1$s.%2$s() == null || %1$s.%2$s().%3$s()) {\
                    """.formatted(out.getVariable(), field, accessor));
            out.incrementIndentationLevel();
            out.write("""
                    validation.addFieldError("%s", "%s");\
                    """.formatted(out.getFullKey(field), message));
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
        public void writeBodyTo(ValidationOutput out, String field) {
            out.write("""
                    if (%1$s.%2$s().%3$s() < %4$d || %1$s.%2$s().%3$s() > %5$d) {\
                    """.formatted(out.getVariable(), field, accessor, min, max));
            out.incrementIndentationLevel();
            out.write("""
                    validation.addFieldError("%s", "%s", %d, %d);\
                    """.formatted(out.getFullKey(field), message, min, max));
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

    record MoreThan(String message, long value) implements NullUnsafeWriter {
        @Override
        public void writeBodyTo(ValidationOutput out, String field) {
            out.write("""
                    if (%s.%s() <= %d) {\
                    """.formatted(out.getVariable(), field, value));
            out.incrementIndentationLevel();
            out.write("""
                    validation.addFieldError("%s", "%s", %d);\
                    """.formatted(out.getFullKey(field), message, value));
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

    record MoreThanOrEqual(String message, long value) implements NullUnsafeWriter {
        @Override
        public void writeBodyTo(ValidationOutput out, String field) {
            out.write("""
                    if (%s.%s() < %d) {\
                    """.formatted(out.getVariable(), field, value));
            out.incrementIndentationLevel();
            out.write("""
                    validation.addFieldError("%s", "%s", %d);\
                    """.formatted(out.getFullKey(field), message, value));
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

    record LessThan(String message, long value) implements NullUnsafeWriter {
        @Override
        public void writeBodyTo(ValidationOutput out, String field) {
            out.write("""
                    if (%s.%s() >= %d) {\
                    """.formatted(out.getVariable(), field, value));
            out.incrementIndentationLevel();
            out.write("""
                    validation.addFieldError("%s", "%s", %d);\
                    """.formatted(out.getFullKey(field), message, value));
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

    record LessThanOrEqual(String message, long value) implements NullUnsafeWriter {
        @Override
        public void writeBodyTo(ValidationOutput out, String field) {
            out.write("""
                    if (%s.%s() > %d) {\
                    """.formatted(out.getVariable(), field, value));
            out.incrementIndentationLevel();
            out.write("""
                    validation.addFieldError("%s", "%s", %d);\
                    """.formatted(out.getFullKey(field), message, value));
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

    record Pattern(String regex, String message) implements NullUnsafeWriter {
        @Override
        public void writeBodyTo(ValidationOutput out, String field) {
            out.write("""
                    if (!%s.%s().matches("%s")) {\
                    """.formatted(out.getVariable(), field, regex));
            out.incrementIndentationLevel();
            out.write("""
                    validation.addFieldError("%s", "%s");\
                    """.formatted(out.getFullKey(field), message));
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

    record Validator(
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
        public void writeBodyTo(ValidationOutput out, String field) {
            out.write("""
                    validation.addAll(%s.validate(%s.%s()), new Object[]{"%s"});
                    """.formatted(validatorProperty(field), out.getVariable(), field, out.getFullKey(field)));
        }

        private String validatorProperty(String field) {
            return field + "Validator";
        }
    }
}
