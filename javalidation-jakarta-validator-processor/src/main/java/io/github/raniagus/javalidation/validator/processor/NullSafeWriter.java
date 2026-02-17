package io.github.raniagus.javalidation.validator.processor;

public sealed interface NullSafeWriter extends ValidationWriter {

    default void writePropertiesTo(ValidationOutput out, String field) {
    }

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

}
