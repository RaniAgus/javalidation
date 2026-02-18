package io.github.raniagus.javalidation.validator.processor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

public interface NullUnsafeWriter extends ValidationWriter {

    default void writePropertiesTo(
            ValidationOutput out,
            String field
    ) {}

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
                    %sValidation.addRootError("%s"%s);\
                    """.formatted(out.getVariable(), message, formatArg()));
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
                %sValidation.addRootError("%s");\
                """.formatted(out.getVariable(), message));
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
                    %sValidation.addRootError("%s"%s);\
                    """.formatted(out.getVariable(), message, formatArgs()));
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
                %sValidation.addRootError("%s", %s, %s);\
                """.formatted(out.getVariable(), message, integer, fraction));
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
            @Nullable NullSafeWriter nullSafeWriter,
            List<NullUnsafeWriter> nullUnsafeWriters
    ) implements NullUnsafeWriter, WithWriters {

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
            writeNestedFieldsTo(nullSafeWriter, nullUnsafeWriters, out);
            out.removeVariable();

            out.write("%sValidation.addAll(%sItemValidation.finish(), new Object[]{%sIndex++});".formatted(field, out.getVariable(), field));

            out.decrementIndentationLevel();
            out.write("}");
            out.write("");
        }
    }
}
