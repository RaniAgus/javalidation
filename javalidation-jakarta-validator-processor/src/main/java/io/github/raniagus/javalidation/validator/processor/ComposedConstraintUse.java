package io.github.raniagus.javalidation.validator.processor;

import java.util.stream.Stream;

/** A configured scalar-validator instance used from a generated record validator. */
record ComposedConstraintUse(String validatorType, String message, boolean configurable) implements ValidationWriter {
    private String constantName(ValidationOutput out) {
        String simpleName = validatorType.substring(validatorType.lastIndexOf('.') + 1);
        return (out.getVariable() + "_" + simpleName).replace('$', '_').toUpperCase() + "_VALIDATOR";
    }

    @Override
    public Stream<String> imports() {
        return Stream.of(validatorType);
    }

    @Override
    public void writePropertiesTo(ValidationOutput out) {
        String simpleName = validatorType.substring(validatorType.lastIndexOf('.') + 1);
        String constructor = configurable ? "new %s(%s)".formatted(simpleName, NullUnsafeWriter.javaStringLiteral(message))
                : "new %s()".formatted(simpleName);
        out.write("private static final %s %s = %s;".formatted(simpleName, constantName(out), constructor));
    }

    @Override
    public void writeBodyTo(ValidationOutput out) {
        out.write("%s.validate(validation, %s);".formatted(constantName(out), out.getVariable()));
    }
}
