package io.github.raniagus.javalidation.validator.processor;

import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

public sealed interface FieldWriter extends ValidationWriter {
    @Override
    default Stream<String> imports() {
        return Stream.concat(
                Stream.ofNullable(nullSafeWriter()).flatMap(ValidationWriter::imports),
                Stream.concat(nullUnsafeWriters().stream().flatMap(ValidationWriter::imports),
                        composedConstraintUses().stream().flatMap(ValidationWriter::imports))
        );
    }

    default void writePropertiesTo(ValidationOutput out) {
        NullSafeWriter nullSafeWriter = nullSafeWriter();
        out.registerVariable(field());
        if (nullSafeWriter != null) {
            nullSafeWriter.writePropertiesTo(out);
        }
        for (NullUnsafeWriter writer : nullUnsafeWriters()) {
            writer.writePropertiesTo(out);
        }
        for (ComposedConstraintUse use : composedConstraintUses()) {
            use.writePropertiesTo(out);
        }
        out.removeVariable();
    }

    default void writePropertiesInitTo(ValidationOutput out) {
        NullSafeWriter nullSafeWriter = nullSafeWriter();
        out.registerVariable(field());
        if (nullSafeWriter != null) {
            nullSafeWriter.writePropertiesInitTo(out);
        }
        for (NullUnsafeWriter writer : nullUnsafeWriters()) {
            writer.writePropertiesInitTo(out);
        }
        out.removeVariable();
    }

    String field();

    default @Nullable NullSafeWriter nullSafeWriter() {
        return null;
    }

    List<NullUnsafeWriter> nullUnsafeWriters();

    default List<ComposedConstraintUse> composedConstraintUses() {
        return List.of();
    }

    @Override
    default void writeBodyTo(ValidationOutput out) {
        NullSafeWriter nullSafeWriter = nullSafeWriter();
        List<NullUnsafeWriter> nullUnsafeWriters = nullUnsafeWriters();
        if (nullSafeWriter == null && nullUnsafeWriters.isEmpty() && composedConstraintUses().isEmpty()) {
            return;
        }

        out.write("validation.withField(\"%s\", () -> {".formatted(field()));
        out.incrementIndentationLevel();

        out.write("var %s = %s.%s();".formatted(field(), out.getVariable(), field()));
        out.registerVariable(field());

        writeNestedFieldsTo(out);

        out.removeVariable();
        out.decrementIndentationLevel();
        out.write("});");
    }

    void writeNestedFieldsTo(ValidationOutput out);

    record PrimitiveWriter(
            String field,
            List<NullUnsafeWriter> nullUnsafeWriters,
            List<ComposedConstraintUse> composedConstraintUses
    ) implements FieldWriter {
        @Override
        public void writeNestedFieldsTo(ValidationOutput out) {
            nullUnsafeWriters.forEach(writer -> writer.writeBodyTo(out));
            composedConstraintUses.forEach(writer -> writer.writeBodyTo(out));
        }
    }

    record ObjectWriter(
            String field,
            @Nullable NullSafeWriter nullSafeWriter,
            List<NullUnsafeWriter> nullUnsafeWriters,
            List<ComposedConstraintUse> composedConstraintUses
    ) implements FieldWriter, WithNestedObjectWriters {
        @Override
        public void writeNestedFieldsTo(ValidationOutput out) {
            if (composedConstraintUses.isEmpty()) {
                writeNestedFieldsTo(nullSafeWriter, nullUnsafeWriters, out);
                return;
            }
            if (nullSafeWriter != null) {
                nullSafeWriter.writeBodyTo(out);
            } else if (!nullUnsafeWriters.isEmpty()) {
                out.write("if (%s == null) {".formatted(out.getVariable()));
                out.incrementIndentationLevel();
                composedConstraintUses.forEach(writer -> writer.writeBodyTo(out));
                out.write("return;");
                out.decrementIndentationLevel();
                out.write("}");
            }
            nullUnsafeWriters.forEach(writer -> writer.writeBodyTo(out));
            composedConstraintUses.forEach(writer -> writer.writeBodyTo(out));
        }
    }
}
