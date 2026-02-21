package io.github.raniagus.javalidation.validator.processor;

public sealed interface NullSafeWriter extends ValidationWriter {

    record NotNull(String message) implements NullSafeWriter {
        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    if (%s == null) {\
                    """.formatted(out.getVariable()));
            out.incrementIndentationLevel();
            out.write("""
                    validation.addError("%s");\
                    """.formatted(message));
            out.write("return;");
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
                    validation.addError("%s");\
                    """.formatted(message));
            out.write("return;");
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
                    validation.addError("%s");\
                    """.formatted(message));
            out.write("return;");
            out.decrementIndentationLevel();
            out.write("}");
        }
    }

}
