package io.github.raniagus.javalidation.validator.processor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

public interface NullUnsafeWriter extends ValidationWriter {

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
                    validation.addRootError("%s", %d, %d);\
                    """.formatted(message, min, max));
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
                    validation.addRootError("%s");\
                    """.formatted(message));
            out.decrementIndentationLevel();
            out.write("}");
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
                case BIG_DECIMAL, NUMBER, CHAR_SEQUENCE -> Stream.of("java.math.BigDecimal");
                case BIG_INTEGER -> Stream.of("java.math.BigInteger");
                default -> Stream.empty();
            };
        }

        @Override
        public void writeBodyTo(ValidationOutput out) {
            String comparison = switch (kind) {
                case BIG_DECIMAL -> "%s.compareTo(new BigDecimal(\"%s\")) %s 0".formatted(out.getVariable(), value, operator);
                case BIG_INTEGER -> "%s.compareTo(new BigInteger(\"%s\")) %s 0".formatted(out.getVariable(), value, operator);
                case BYTE, SHORT, INTEGER, LONG -> "%s %s %s".formatted(out.getVariable(), operator, value);
                case NUMBER, CHAR_SEQUENCE -> "new BigDecimal(%s.toString()).compareTo(new BigDecimal(\"%s\")) %s 0".formatted(out.getVariable(), value, operator);
            };
            out.write("""
                    if (!(%s)) {\
                    """.formatted(comparison));
            out.incrementIndentationLevel();
            out.write("""
                    validation.addRootError("%s"%s);\
                    """.formatted(message, formatArg()));
            out.decrementIndentationLevel();
            out.write("}");
        }

        private String formatArg() {
            if (!useValueAsArg) return "";
            if (value instanceof String) return ", \"" + value + "\"";
            return ", " + value;
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
            return switch (kind) {
                case INSTANT, LONG, INTEGER, SHORT, BYTE, DATE, CALENDAR -> Stream.of("java.time.Instant");
                case LOCAL_DATE -> Stream.of("java.time.LocalDate");
                case LOCAL_TIME -> Stream.of("java.time.LocalTime");
                case LOCAL_DATE_TIME -> Stream.of("java.time.LocalDateTime");
                case OFFSET_DATE_TIME -> Stream.of("java.time.OffsetDateTime");
                case OFFSET_TIME -> Stream.of("java.time.OffsetTime");
                case ZONED_DATE_TIME -> Stream.of("java.time.ZonedDateTime");
                case YEAR -> Stream.of("java.time.Year");
                case YEAR_MONTH -> Stream.of("java.time.YearMonth");
                case MONTH_DAY -> Stream.of("java.time.MonthDay");
                case HIJRAH_DATE -> Stream.of("java.time.chrono.HijrahDate");
                case JAPANESE_DATE -> Stream.of("java.time.chrono.JapaneseDate");
                case MINGUO_DATE -> Stream.of("java.time.chrono.MinguoDate");
                case THAI_BUDDHIST_DATE -> Stream.of("java.time.chrono.ThaiBuddhistDate");
            };
        }

        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                if (!(%s.%s(%s) == %s)) {\
                """.formatted(normalized(out.getVariable()), accessor, now(), result));
            out.incrementIndentationLevel();
            out.write("""
                validation.addRootError("%s");\
                """.formatted(message));
            out.decrementIndentationLevel();
            out.write("}");
        }

        private String now() {
            return switch (kind) {
                case INSTANT, LONG, INTEGER, SHORT, BYTE, DATE, CALENDAR -> "Instant.now()";
                case LOCAL_DATE -> "LocalDate.now()";
                case LOCAL_TIME -> "LocalTime.now()";
                case LOCAL_DATE_TIME -> "LocalDateTime.now()";
                case OFFSET_DATE_TIME -> "OffsetDateTime.now()";
                case OFFSET_TIME -> "OffsetTime.now()";
                case ZONED_DATE_TIME -> "ZonedDateTime.now()";
                case YEAR -> "Year.now()";
                case YEAR_MONTH -> "YearMonth.now()";
                case MONTH_DAY -> "MonthDay.now()";
                case HIJRAH_DATE -> "HijrahDate.now()";
                case JAPANESE_DATE -> "JapaneseDate.now()";
                case MINGUO_DATE -> "MinguoDate.now()";
                case THAI_BUDDHIST_DATE -> "ThaiBuddhistDate.now()";
            };
        }

        private String normalized(String variable) {
            return switch (kind) {
                case LONG, INTEGER, SHORT, BYTE -> "Instant.ofEpochMilli(%s)".formatted(variable);
                case DATE -> "Instant.ofEpochMilli(%s.getTime())".formatted(variable);
                case CALENDAR -> "Instant.ofEpochMilli(%s.getTimeInMillis())".formatted(variable);
                default -> variable;
            };
        }
    }

    record Pattern(String regex, String message, Object... args) implements NullUnsafeWriter {
        @Override
        public Stream<String> imports() {
            return Stream.of("java.util.Objects");
        }

        @Override
        public void writeBodyTo(ValidationOutput out) {
            out.write("""
                    if (!Objects.toString(%s).matches("%s")) {\
                    """.formatted(out.getVariable(), regex));
            out.incrementIndentationLevel();
            out.write("""
                    validation.addRootError("%s"%s);\
                    """.formatted(message, formatArgs()));
            out.decrementIndentationLevel();
            out.write("}");
        }

        private String formatArgs() {
            if (args.length == 0) return "";
            return Stream.of(args)
                    .map(arg -> arg instanceof String ? "\"" + arg + "\"" : arg.toString())
                    .collect(Collectors.joining(", ", ",", ""));
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
                case BIG_DECIMAL, NUMBER, CHAR_SEQUENCE -> Stream.of("java.math.BigDecimal");
                case BIG_INTEGER -> Stream.of("java.math.BigInteger");
                default -> Stream.empty();
            };
        }

        @Override
        public void writeBodyTo(ValidationOutput out) {
            if (kind == NumericKind.CHAR_SEQUENCE) {
                String pattern = "^-?\\\\d{0," + integer + "}(\\\\.\\\\d{0," + fraction + "})?$";
                out.write("""
                    if (!%s.toString().matches("%s")) {\
                    """.formatted(out.getVariable(), pattern));
            } else {
                String bdExpr = switch (kind) {
                    case BIG_DECIMAL, NUMBER -> "new BigDecimal(%s.toString()).stripTrailingZeros()".formatted(out.getVariable());
                    default -> "new BigDecimal(%s.toString())".formatted(out.getVariable());
                };
                out.write("var %s_bd = %s;".formatted(out.getVariable(), bdExpr));
                out.write("""
                    if (!(%1$s_bd.precision() - %1$s_bd.scale() <= %2$s && Math.max(%1$s_bd.scale(), 0) <= %3$s)) {\
                    """.formatted(out.getVariable(), integer, fraction));
            }
            out.incrementIndentationLevel();
            out.write("""
                validation.addRootError("%s", %s, %s);\
                """.formatted(message, integer, fraction));
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
        public void writePropertiesTo(ValidationOutput out) {
            out.write("""
                    private final Validator<%s%s> %sValidator = new %s();\
                    """.formatted(
                    referredTypeEnclosingClassPrefix,
                    referredTypeName,
                    out.getVariable(),
                    referredValidatorName
            ));
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
            out.write("");
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
            out.write("");
        }
    }
}
