package io.github.raniagus.javalidation.processor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

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

    record Null(String message) implements NullSafeWriter {
        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    if (%s != null) {\
                    """.formatted(out.getVariable()));
            out.incrementIndentationLevel();
            out.write("""
                    %sValidation.addRootError("%s");\
                    """.formatted(out.getVariable(), message));
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

    record NullSafeAccessor(String accessor, String message) implements NullSafeWriter {
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

    record EqualTo(String value, String message) implements NullUnsafeWriter {
        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    if (!%s.equals(%s)) {\
                    """.formatted(out.getVariable(), value));
            out.incrementIndentationLevel();
            out.write("""
                    %sValidation.addRootError("%s");\
                    """.formatted(out.getVariable(), message));
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

    record RawCompare(String operator, Number value, String message) implements NullUnsafeWriter {
        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    if (!(%s %s %s)) {\
                    """.formatted(out.getVariable(), operator, value));
            out.incrementIndentationLevel();
            out.write("""
                    %sValidation.addRootError("%s", %s);\
                    """.formatted(out.getVariable(), message, value));
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

    record DecimalCompare(String operator, BigDecimal value, String message) implements NullUnsafeWriter {
        @Override
        public Stream<String> imports() {
            return Stream.of("io.github.raniagus.javalidation.validator.ValidatorUtils");
        }

        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    if (!(ValidatorUtils.compare(%s, "%s") %s 0)) {\
                    """.formatted(out.getVariable(), value, operator));
            out.incrementIndentationLevel();
            out.write("""
                    %sValidation.addRootError("%s", "%s");\
                    """.formatted(out.getVariable(), message, value));
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

    record TemporalCompare(String accessor, boolean result, String message) implements NullUnsafeWriter {
        @Override
        public Stream<String> imports() {
            return Stream.of(
                    "io.github.raniagus.javalidation.validator.ValidatorUtils",
                    "java.time.Instant"
            );
        }

        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    if (!(ValidatorUtils.toInstant(%s).%s(Instant.now()) == %s)) {\
                    """.formatted(out.getVariable(), accessor, result));
            out.incrementIndentationLevel();
            out.write("""
                    %sValidation.addRootError("%s");\
                    """.formatted(out.getVariable(), message));
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
                    private final Validator<%s%s> %s = new %s();\
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

    record IterableWriter(
            ValidationWriter.@Nullable NullSafeWriter nullSafeWriter,
            List<NullUnsafeWriter> nullUnsafeWriters
    ) implements NullUnsafeWriter, WithFieldWriters {

        public Stream<String> imports() {
            return Stream.concat(
                    Stream.ofNullable(nullSafeWriter).flatMap(ValidationWriter::imports),
                    nullUnsafeWriters.stream().flatMap(ValidationWriter::imports)
            );
        }

        @Override
        public void writePropertiesTo(ValidationOutput out, String field) {
            if (nullSafeWriter != null) {
                nullSafeWriter.writePropertiesTo(out, field + "Item");
            }
            nullUnsafeWriters.forEach(writer -> writer.writePropertiesTo(out, field + "Item"));
        }

        @Override
        public void writeBodyTo(ValidationOutput out) {
            if (nullSafeWriter == null && nullUnsafeWriters.isEmpty()) {
                return;
            }

            out.write("int %sIndex = 0;".formatted(out.getVariable()));
            out.write("for (var %sItem : %s) {".formatted(out.getVariable(), out.getVariable()));
            out.incrementIndentationLevel();

            out.write("var %sItemValidation = Validation.create();".formatted(out.getVariable()));

            String field = out.getVariable();
            out.registerVariable(field + "Item");
            writeNestedFieldsTo(field, nullSafeWriter, nullUnsafeWriters, out);
            out.removeVariable();

            out.write("%sValidation.addAll(%sItemValidation.finish(), new Object[]{%sIndex++});".formatted(field, out.getVariable(), field));

            out.decrementIndentationLevel();
            out.write("}");
            out.write("");
        }
    }
}
