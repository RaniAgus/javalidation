package io.github.raniagus.javalidation.validator.processor;

import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

public interface NullUnsafeWriter extends ValidationWriter {

    enum SizeKind {
        /** {@code String} — uses {@code String.length()}. */
        LENGTH,
        /** {@code Collection<?>} — uses {@code Collection.size()}. */
        COLLECTION,
        /** {@code Map<?, ?>} — uses {@code Map.size()}. */
        MAP
    }

    record Size(
            SizeKind kind,
            String message,
            int min,
            int max
    ) implements NullUnsafeWriter {
        @Override
        public void writePropertiesTo(ValidationOutput out) {
            String typeName = switch (kind) {
                case LENGTH -> "String";
                case COLLECTION -> "java.util.Collection<?>";
                case MAP -> "java.util.Map<?, ?>";
            };
            String method = switch (kind) {
                case LENGTH -> "length";
                case COLLECTION -> "size";
                case MAP -> "sizeMap";
            };
            out.write("""
                    private static final Constraint<%s> %s_SIZE = Constraints.%s(%d, %d);
                    """.formatted(typeName, out.getVariable().toUpperCase(), method, min, max));
        }

        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    %s_SIZE.validate(validation, %s);\
                    """.formatted(out.getVariable().toUpperCase(), out.getVariable()));
        }
    }

    record EqualTo(String value, String message) implements NullUnsafeWriter {
        @Override
        public void writePropertiesTo(ValidationOutput out) {
            String constSuffix = "true".equals(value) ? "ASSERT_TRUE" : "ASSERT_FALSE";
            String method = "true".equals(value) ? "assertTrue" : "assertFalse";
            out.write("""
                    private static final Constraint<Boolean> %s_%s = Constraints.%s();
                    """.formatted(out.getVariable().toUpperCase(), constSuffix, method));
        }

        @Override
        public void writeBodyTo(ValidationOutput out) {
            String constSuffix = "true".equals(value) ? "ASSERT_TRUE" : "ASSERT_FALSE";
            out.write("""
                    %s_%s.validate(validation, %s);\
                    """.formatted(out.getVariable().toUpperCase(), constSuffix, out.getVariable()));
        }
    }

    record NumericCompare(
            String operator,
            Object value,
            NumericKind kind,
            String message,
            boolean useValueAsArg
    ) implements NullUnsafeWriter {

        @Override
        public Stream<String> imports() {
            return switch (kind) {
                case BIG_DECIMAL -> Stream.of("java.math.BigDecimal");
                case BIG_INTEGER -> Stream.of("java.math.BigInteger");
                case BYTE, SHORT, INTEGER, LONG, NUMBER, CHAR_SEQUENCE -> Stream.empty();
            };
        }

        @Override
        public void writePropertiesTo(ValidationOutput out) {
            String typeName = constraintTypeName();
            String call = constraintsCall();
            out.write("""
                    private static final Constraint<%s> %s = Constraints.%s;
                    """.formatted(typeName, constantName(out.getVariable()), call));
        }

        @Override
        public void writeBodyTo(ValidationOutput out) {
            String constName = constantName(out.getVariable());
            String arg = needsCast() ? "(long) " + out.getVariable() : out.getVariable();
            out.write("""
                    %s.validate(validation, %s);\
                    """.formatted(constName, arg));
        }

        private String constraintTypeName() {
            return switch (kind) {
                case BYTE, SHORT, INTEGER, LONG -> "Long";
                case BIG_DECIMAL -> "BigDecimal";
                case BIG_INTEGER -> "BigInteger";
                case NUMBER -> "Number";
                case CHAR_SEQUENCE -> "CharSequence";
            };
        }

        private String constraintsCall() {
            if (!useValueAsArg) {
                // @Positive / @PositiveOrZero / @Negative / @NegativeOrZero
                String suffix = switch (kind) {
                    case BYTE, SHORT, INTEGER, LONG -> "";
                    case BIG_DECIMAL -> "BigDecimal";
                    case BIG_INTEGER -> "BigInteger";
                    case NUMBER, CHAR_SEQUENCE -> "Number";
                };
                return switch (operator) {
                    case ">" -> "positive" + suffix + "()";
                    case ">=" -> "positiveOrZero" + suffix + "()";
                    case "<" -> "negative" + suffix + "()";
                    case "<=" -> "negativeOrZero" + suffix + "()";
                    default -> throw new IllegalStateException("Unexpected operator: " + operator);
                };
            }

            if (value instanceof String sv) {
                // @DecimalMin / @DecimalMax
                boolean isMin = ">=".equals(operator) || ">".equals(operator);
                boolean inclusive = ">=".equals(operator) || "<=".equals(operator);
                String method = isMin ? "decimalMin" : "decimalMax";
                return inclusive
                        ? "%s(\"%s\")".formatted(method, sv)
                        : "%s(\"%s\", false)".formatted(method, sv);
            }

            // @Min / @Max — value is a long
            long lv = ((Number) value).longValue();
            boolean isMin = ">=".equals(operator);
            return switch (kind) {
                case BYTE, SHORT, INTEGER, LONG -> isMin
                        ? "minLong(%dL)".formatted(lv)
                        : "maxLong(%dL)".formatted(lv);
                case BIG_DECIMAL -> isMin
                        ? "minBigDecimal(%dL)".formatted(lv)
                        : "maxBigDecimal(%dL)".formatted(lv);
                case BIG_INTEGER -> isMin
                        ? "minBigInteger(%dL)".formatted(lv)
                        : "maxBigInteger(%dL)".formatted(lv);
                case NUMBER, CHAR_SEQUENCE -> isMin
                        ? "minNumber(%dL)".formatted(lv)
                        : "maxNumber(%dL)".formatted(lv);
            };
        }

        private boolean needsCast() {
            return kind == NumericKind.BYTE || kind == NumericKind.SHORT || kind == NumericKind.INTEGER;
        }

        private String constantName(String variable) {
            if (!useValueAsArg) {
                // positive/negative — no value suffix
                String opPart = switch (operator) {
                    case ">" -> "GT_0";
                    case ">=" -> "GE_0";
                    case "<" -> "LT_0";
                    case "<=" -> "LE_0";
                    default -> operator + "_0";
                };
                return variable.toUpperCase() + "_" + opPart;
            }
            String opSuffix = switch (operator) {
                case ">=" -> "GE";
                case ">"  -> "GT";
                case "<=" -> "LE";
                case "<"  -> "LT";
                default   -> operator;
            };
            String valueSuffix = value.toString().replace("-", "N").replace(".", "_");
            return variable.toUpperCase() + "_" + opSuffix + "_" + valueSuffix;
        }
    }

    record TemporalCompare(
            String accessor,
            boolean result,
            TemporalKind kind,
            String message
    ) implements NullUnsafeWriter {

        @Override
        public Stream<String> imports() {
            String path = kind.importPath();
            return path.isEmpty() ? Stream.empty() : Stream.of(path);
        }

        @Override
        public void writePropertiesTo(ValidationOutput out) {
            String typeName = kind.javaTypeName();
            String constraintsMethod = constraintsMethod();
            String nowExpr = kind.nowExpression();
            String constName = constantName(out.getVariable());
            out.write("""
                    private static final Constraint<%s> %s = Constraints.%s(%s);
                    """.formatted(typeName, constName, constraintsMethod, nowExpr));
        }

        @Override
        public void writeBodyTo(ValidationOutput out) {
            String constName = constantName(out.getVariable());
            String arg = kind.needsLongCast() ? "(long) " + out.getVariable() : out.getVariable();
            out.write("""
                    %s.validate(validation, %s);\
                    """.formatted(constName, arg));
        }

        private String constraintsMethod() {
            // accessor="isBefore", result=true  → past
            // accessor="isBefore", result=false → futureOrPresent
            // accessor="isAfter",  result=true  → future
            // accessor="isAfter",  result=false → pastOrPresent
            return switch (accessor) {
                case "isBefore" -> result ? "past" : "futureOrPresent";
                case "isAfter"  -> result ? "future" : "pastOrPresent";
                default -> throw new IllegalStateException("Unexpected accessor: " + accessor);
            };
        }

        private String constantName(String variable) {
            String suffix = switch (accessor) {
                case "isBefore" -> result ? "PAST" : "FUTURE_OR_PRESENT";
                case "isAfter"  -> result ? "FUTURE" : "PAST_OR_PRESENT";
                default -> throw new IllegalStateException("Unexpected accessor: " + accessor);
            };
            return variable.toUpperCase() + "_" + suffix;
        }
    }

    record Pattern(String regex, String message, Object... args) implements NullUnsafeWriter {

        @Override
        public void writePropertiesTo(ValidationOutput out) {
            out.write("""
                    private static final Constraint<String> %s_PATTERN = Constraints.pattern("%s");
                    """.formatted(out.getVariable().toUpperCase(), regex));
        }

        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    %s_PATTERN.validate(validation, %s);\
                    """.formatted(out.getVariable().toUpperCase(), out.getVariable()));
        }
    }

    record Email(String message) implements NullUnsafeWriter {

        @Override
        public void writePropertiesTo(ValidationOutput out) {
            out.write("""
                    private static final Constraint<String> %s_EMAIL = Constraints.email();
                    """.formatted(out.getVariable().toUpperCase()));
        }

        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    %s_EMAIL.validate(validation, %s);\
                    """.formatted(out.getVariable().toUpperCase(), out.getVariable()));
        }
    }

    record Digits(
            int integer,
            int fraction,
            NumericKind kind,
            String message
    ) implements NullUnsafeWriter {

        @Override
        public Stream<String> imports() {
            return switch (kind) {
                case BIG_DECIMAL -> Stream.of("java.math.BigDecimal");
                case BIG_INTEGER -> Stream.of("java.math.BigInteger");
                case NUMBER, BYTE, SHORT, INTEGER, LONG, CHAR_SEQUENCE -> Stream.empty();
            };
        }

        @Override
        public void writePropertiesTo(ValidationOutput out) {
            String typeName = switch (kind) {
                case CHAR_SEQUENCE -> "String";
                case BIG_DECIMAL -> "BigDecimal";
                case BIG_INTEGER -> "BigInteger";
                case NUMBER -> "Number";
                case BYTE, SHORT, INTEGER, LONG -> "Long";
            };
            String method = kind == NumericKind.CHAR_SEQUENCE ? "digitsString" : "digits";
            out.write("""
                    private static final Constraint<%s> %s_DIGITS = Constraints.%s(%d, %d);
                    """.formatted(typeName, out.getVariable().toUpperCase(), method, integer, fraction));
        }

        @Override
        public void writeBodyTo(ValidationOutput out) {
            String arg = switch (kind) {
                case BYTE, SHORT, INTEGER -> "(long) " + out.getVariable();
                default -> out.getVariable();
            };
            out.write("""
                    %s_DIGITS.validate(validation, %s);\
                    """.formatted(out.getVariable().toUpperCase(), arg));
        }
    }

    record Validate(
            String referredTypeImport, // "com.example.RecordName"
            String referredTypeEnclosingClassPrefix, // "" or "EnclosingClass."
            String referredTypeName, // "RecordName"
            String referredValidatorName // "RecordNameValidator" or "EnclosingClass$RecordNameValidator"
    ) implements NullUnsafeWriter {
        @Override
        public Stream<String> imports() {
            return Stream.of(referredTypeImport, "io.github.raniagus.javalidation.Validator");
        }

        @Override
        public void writePropertiesTo(ValidationOutput out) {
            out.write("""
                    private Validator<%s%s> %sValidator;\
                    """.formatted(
                        referredTypeEnclosingClassPrefix,
                        referredTypeName,
                        out.getVariable()
                    )
            );
        }

        @Override
        public void writePropertiesInitTo(ValidationOutput out) {
            out.write("""
                    %sValidator = holder.getValidator(%s%s.class);\
                    """.formatted(out.getVariable(), referredTypeEnclosingClassPrefix, referredTypeName)
            );
        }

        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    %sValidator.validate(validation, %s);\
                    """.formatted(out.getVariable(), out.getVariable()));
        }
    }

    record IterableWriter(
            @Nullable NullSafeWriter nullSafeWriter,
            List<NullUnsafeWriter> nullUnsafeWriters
    ) implements NullUnsafeWriter, WithNestedObjectWriters {

        public Stream<String> imports() {
            return Stream.concat(
                    Stream.ofNullable(nullSafeWriter).flatMap(ValidationWriter::imports),
                    nullUnsafeWriters.stream().flatMap(ValidationWriter::imports)
            );
        }

        @Override
        public void writePropertiesTo(ValidationOutput out) {
            out.registerVariable(out.getVariable() + "Item");
            if (nullSafeWriter != null) {
                nullSafeWriter.writePropertiesTo(out);
            }
            nullUnsafeWriters.forEach(writer -> writer.writePropertiesTo(out));
            out.removeVariable();
        }

        @Override
        public void writePropertiesInitTo(ValidationOutput out) {
            out.registerVariable(out.getVariable() + "Item");
            if (nullSafeWriter != null) {
                nullSafeWriter.writePropertiesInitTo(out);
            }
            nullUnsafeWriters.forEach(writer -> writer.writePropertiesInitTo(out));
            out.removeVariable();
        }

        @Override
        public void writeBodyTo(ValidationOutput out) {
            if (nullSafeWriter == null && nullUnsafeWriters.isEmpty()) {
                return;
            }

            out.write("validation.withEach(%s, %sItem -> {".formatted(out.getVariable(), out.getVariable()));
            out.incrementIndentationLevel();

            out.registerVariable(out.getVariable() + "Item");
            writeNestedFieldsTo(nullSafeWriter, nullUnsafeWriters, out);
            out.removeVariable();

            out.decrementIndentationLevel();
            out.write("});");
        }
    }

    record MapWriter(
            @Nullable NullSafeWriter keyNullSafeWriter,
            List<NullUnsafeWriter> keyNullUnsafeWriters,
            @Nullable NullSafeWriter valueNullSafeWriter,
            List<NullUnsafeWriter> valueNullUnsafeWriters
    ) implements NullUnsafeWriter, WithNestedObjectWriters {

        @Override
        public Stream<String> imports() {
            return Stream.of(
                    Stream.ofNullable(keyNullSafeWriter).flatMap(ValidationWriter::imports),
                    keyNullUnsafeWriters.stream().flatMap(ValidationWriter::imports),
                    Stream.ofNullable(valueNullSafeWriter).flatMap(ValidationWriter::imports),
                    valueNullUnsafeWriters.stream().flatMap(ValidationWriter::imports)
            ).flatMap(s -> s);
        }

        @Override
        public void writePropertiesTo(ValidationOutput out) {
            out.registerVariable(out.getVariable() + "Key");
            if (keyNullSafeWriter != null) {
                keyNullSafeWriter.writePropertiesTo(out);
            }
            keyNullUnsafeWriters.forEach(w -> w.writePropertiesTo(out));
            out.removeVariable();

            out.registerVariable(out.getVariable() + "Value");
            if (valueNullSafeWriter != null) {
                valueNullSafeWriter.writePropertiesTo(out);
            }
            valueNullUnsafeWriters.forEach(w -> w.writePropertiesTo(out));
            out.removeVariable();
        }

        @Override
        public void writePropertiesInitTo(ValidationOutput out) {
            out.registerVariable(out.getVariable() + "Key");
            if (keyNullSafeWriter != null) {
                keyNullSafeWriter.writePropertiesInitTo(out);
            }
            keyNullUnsafeWriters.forEach(w -> w.writePropertiesInitTo(out));
            out.removeVariable();

            out.registerVariable(out.getVariable() + "Value");
            if (valueNullSafeWriter != null) {
                valueNullSafeWriter.writePropertiesInitTo(out);
            }
            valueNullUnsafeWriters.forEach(w -> w.writePropertiesInitTo(out));
            out.removeVariable();
        }

        @Override
        public void writeBodyTo(ValidationOutput out) {
            if (keyNullSafeWriter == null && keyNullUnsafeWriters.isEmpty()
                    && valueNullSafeWriter == null && valueNullUnsafeWriters.isEmpty()) {
                return;
            }

            String keyVar = out.getVariable() + "Key";
            String valueVar = out.getVariable() + "Value";

            out.write("%s.forEach((%s, %s) -> {".formatted(out.getVariable(), keyVar, valueVar));
            out.incrementIndentationLevel();

            out.registerVariable(keyVar);
            writeNestedFieldsTo(keyNullSafeWriter, keyNullUnsafeWriters, out);
            out.removeVariable();

            out.write("validation.withField(%s, () -> {".formatted(keyVar));
            out.incrementIndentationLevel();

            out.registerVariable(valueVar);
            writeNestedFieldsTo(valueNullSafeWriter, valueNullUnsafeWriters, out);
            out.removeVariable();

            out.decrementIndentationLevel();
            out.write("});");

            out.decrementIndentationLevel();
            out.write("});");
        }
    }
}
