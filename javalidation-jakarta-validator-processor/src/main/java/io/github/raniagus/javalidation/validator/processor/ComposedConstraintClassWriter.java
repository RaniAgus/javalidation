package io.github.raniagus.javalidation.validator.processor;

import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/** Writes the reusable validator that represents a Jakarta composed constraint. */
record ComposedConstraintClassWriter(
        String packageName,
        String className,
        String valueType,
        @Nullable NullSafeWriter nullSafeWriter,
        List<NullUnsafeWriter> nullUnsafeWriters,
        boolean reportAsSingleViolation,
        String defaultMessage
) implements ClassWriter {
    @Override
    public Stream<String> imports() {
        return Stream.concat(Stream.of("io.github.raniagus.javalidation.Validation", "io.github.raniagus.javalidation.validator.Validator"),
                Stream.concat(Stream.ofNullable(nullSafeWriter).flatMap(ValidationWriter::imports),
                        nullUnsafeWriters.stream().flatMap(ValidationWriter::imports)));
    }

    @Override
    public void writeBody(ValidationOutput out) {
        out.write("public class %s implements Validator<%s> {".formatted(className, valueType));
        out.incrementIndentationLevel();
        if (reportAsSingleViolation) {
            out.write("private final String message;");
            out.write("");
            out.write("public %s() {".formatted(className));
            out.incrementIndentationLevel();
            out.write("this(%s);".formatted(NullUnsafeWriter.javaStringLiteral(defaultMessage)));
            out.decrementIndentationLevel();
            out.write("}");
            out.write("");
            out.write("public %s(String message) {".formatted(className));
            out.incrementIndentationLevel();
            out.write("this.message = message;");
            out.decrementIndentationLevel();
            out.write("}");
            out.write("");
        }
        out.registerVariable("value");
        if (nullSafeWriter != null) nullSafeWriter.writePropertiesTo(out);
        nullUnsafeWriters.forEach(w -> w.writePropertiesTo(out));
        out.removeVariable();
        out.write("");
        out.write("@Override");
        out.write("public void validate(Validation validation, %s value) {".formatted(valueType));
        out.incrementIndentationLevel();
        if (reportAsSingleViolation) {
            out.write("Validation composed = Validation.create();");
            out.write("validateComposing(composed, value);");
            out.write("if (!composed.finish().isEmpty()) {");
            out.incrementIndentationLevel();
            out.write("validation.addError(message);");
            out.decrementIndentationLevel();
            out.write("}");
        } else {
            out.write("validateComposing(validation, value);");
        }
        out.decrementIndentationLevel();
        out.write("}");
        out.write("");
        out.write("private void validateComposing(Validation validation, %s value) {".formatted(valueType));
        out.incrementIndentationLevel();
        out.registerVariable("value");
        if (nullSafeWriter == null && !nullUnsafeWriters.isEmpty()) {
            out.write("if (value == null) return;");
        } else if (nullSafeWriter instanceof NullSafeWriter.NullSafeAccessor accessor
                && valueType.equals("java.lang.CharSequence") && accessor.accessor().equals("isBlank")) {
            out.write("if (value == null || value.toString().isBlank()) {");
            out.incrementIndentationLevel();
            out.write("validation.addError(%s);".formatted(NullUnsafeWriter.javaStringLiteral(accessor.message())));
            out.write("return;");
            out.decrementIndentationLevel();
            out.write("}");
        } else if (nullSafeWriter != null) {
            nullSafeWriter.writeBodyTo(out);
        }
        nullUnsafeWriters.forEach(w -> w.writeBodyTo(out));
        out.removeVariable();
        out.decrementIndentationLevel();
        out.write("}");
        out.decrementIndentationLevel();
        out.write("}");
    }
}
