package io.github.raniagus.javalidation.validator.processor;

import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

public sealed interface FieldWriter extends ValidationWriter {
    @Override
    default Stream<String> imports() {
        return Stream.concat(
                Stream.ofNullable(nullSafeWriter()).flatMap(ValidationWriter::imports),
                nullUnsafeWriters().stream().flatMap(ValidationWriter::imports)
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
        out.removeVariable();
    }

    String field();

    default @Nullable NullSafeWriter nullSafeWriter() {
        return null;
    }

    List<NullUnsafeWriter> nullUnsafeWriters();

    @Override
    default void writeBodyTo(ValidationOutput out) {
        NullSafeWriter nullSafeWriter = nullSafeWriter();
        List<NullUnsafeWriter> nullUnsafeWriters = nullUnsafeWriters();
        if (nullSafeWriter == null && nullUnsafeWriters.isEmpty()) {
            return;
        }

        out.write("validation.validateField(\"%s\", () -> {".formatted(field()));
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
            List<NullUnsafeWriter> nullUnsafeWriters
    ) implements FieldWriter {
        @Override
        public void writeNestedFieldsTo(ValidationOutput out) {
            nullUnsafeWriters.forEach(writer -> writer.writeBodyTo(out));
        }
    }

    record ObjectWriter(
            String field,
            @Nullable NullSafeWriter nullSafeWriter,
            List<NullUnsafeWriter> nullUnsafeWriters
    ) implements FieldWriter, WithNestedObjectWriters {
        @Override
        public void writeNestedFieldsTo(ValidationOutput out) {
            WithNestedObjectWriters.super.writeNestedFieldsTo(out);
        }
    }
}
