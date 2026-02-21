package io.github.raniagus.javalidation.validator.processor;

import java.util.List;
import java.util.stream.Stream;

public record SealedValidatorClassWriter(
        String packageName,
        String className,
        String enclosingClassPrefix,
        String recordName,
        String recordFullName,
        String recordImportName,
        List<ValidatorClassWriter> permittedWriters  // one per permitted record subtype
) implements ValidatorClassWriter {

    @Override
    public Stream<String> imports() {
        return Stream.concat(
                Stream.of(
                        "io.github.raniagus.javalidation.Validation",
                        "io.github.raniagus.javalidation.validator.Validator"
                ),
                permittedWriters.stream().flatMap(w -> Stream.of(
                        w.recordImportName(),
                        w.packageName() + "." + w.className()
                ))
        );
    }

    @Override
    public void writeBody(ValidationOutput out) {
        out.write("public class %s implements Validator<%s%s> {".formatted(
                className, enclosingClassPrefix, recordName));
        out.incrementIndentationLevel();

        for (ValidatorClassWriter w : permittedWriters) {
            out.write("private final Validator<%s%s> %s = new %s();".formatted(
                    w.enclosingClassPrefix(), w.recordName(),
                    decapitalize(w.recordName()) + "Validator",
                    w.className()
            ));
        }

        out.write("");
        out.registerVariable("root");
        out.write("@Override");
        out.write("public void validate(Validation validation, %s%s %s) {".formatted(
                enclosingClassPrefix, recordName, out.getVariable()));
        out.incrementIndentationLevel();

        out.write("switch (%s) {".formatted(out.getVariable()));
        out.incrementIndentationLevel();
        for (ValidatorClassWriter w : permittedWriters) {
            String varName = decapitalize(w.recordName());
            out.write("case %s%s %s -> %sValidator.validate(validation, %s);".formatted(
                    w.enclosingClassPrefix(), w.recordName(), varName, varName, varName));
        }
        out.decrementIndentationLevel();
        out.write("}");

        out.decrementIndentationLevel();
        out.write("}");
        out.decrementIndentationLevel();
        out.write("}");
    }

    private static String decapitalize(String name) {
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
