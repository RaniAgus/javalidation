package io.github.raniagus.javalidation.validator.processor;

public enum TemporalKind {
    INSTANT, LOCAL_DATE, LOCAL_TIME, LOCAL_DATE_TIME,
    OFFSET_DATE_TIME, OFFSET_TIME, ZONED_DATE_TIME,
    YEAR, YEAR_MONTH, MONTH_DAY,
    DATE, CALENDAR,
    HIJRAH_DATE, JAPANESE_DATE, MINGUO_DATE, THAI_BUDDHIST_DATE,
    LONG, INTEGER, SHORT, BYTE;

    /** The Java type name to use in the {@code Constraint<T>} type parameter. */
    public String javaTypeName() {
        return switch (this) {
            case INSTANT -> "Instant";
            case LOCAL_DATE -> "LocalDate";
            case LOCAL_TIME -> "LocalTime";
            case LOCAL_DATE_TIME -> "LocalDateTime";
            case OFFSET_DATE_TIME -> "OffsetDateTime";
            case OFFSET_TIME -> "OffsetTime";
            case ZONED_DATE_TIME -> "ZonedDateTime";
            case YEAR -> "Year";
            case YEAR_MONTH -> "YearMonth";
            case MONTH_DAY -> "MonthDay";
            case HIJRAH_DATE -> "HijrahDate";
            case JAPANESE_DATE -> "JapaneseDate";
            case MINGUO_DATE -> "MinguoDate";
            case THAI_BUDDHIST_DATE -> "ThaiBuddhistDate";
            case LONG, INTEGER, SHORT, BYTE -> "Long";
            case DATE -> "java.util.Date";
            case CALENDAR -> "java.util.Calendar";
        };
    }

    /**
     * The method reference / lambda expression to pass as the {@code now} supplier
     * to {@code Constraints.past(...)} etc.
     */
    public String nowExpression() {
        return switch (this) {
            case INSTANT -> "Instant::now";
            case LOCAL_DATE -> "LocalDate::now";
            case LOCAL_TIME -> "LocalTime::now";
            case LOCAL_DATE_TIME -> "LocalDateTime::now";
            case OFFSET_DATE_TIME -> "OffsetDateTime::now";
            case OFFSET_TIME -> "OffsetTime::now";
            case ZONED_DATE_TIME -> "ZonedDateTime::now";
            case YEAR -> "Year::now";
            case YEAR_MONTH -> "YearMonth::now";
            case MONTH_DAY -> "MonthDay::now";
            case HIJRAH_DATE -> "HijrahDate::now";
            case JAPANESE_DATE -> "JapaneseDate::now";
            case MINGUO_DATE -> "MinguoDate::now";
            case THAI_BUDDHIST_DATE -> "ThaiBuddhistDate::now";
            case LONG, INTEGER, SHORT, BYTE -> "() -> Instant.now().toEpochMilli()";
            case DATE -> "java.util.Date::new";
            case CALENDAR -> "java.util.Calendar::getInstance";
        };
    }

    /**
     * The import to add for this temporal kind.
     * Returns an empty string for kinds that use FQN (DATE, CALENDAR).
     */
    public String importPath() {
        return switch (this) {
            case INSTANT -> "java.time.Instant";
            case LOCAL_DATE -> "java.time.LocalDate";
            case LOCAL_TIME -> "java.time.LocalTime";
            case LOCAL_DATE_TIME -> "java.time.LocalDateTime";
            case OFFSET_DATE_TIME -> "java.time.OffsetDateTime";
            case OFFSET_TIME -> "java.time.OffsetTime";
            case ZONED_DATE_TIME -> "java.time.ZonedDateTime";
            case YEAR -> "java.time.Year";
            case YEAR_MONTH -> "java.time.YearMonth";
            case MONTH_DAY -> "java.time.MonthDay";
            case HIJRAH_DATE -> "java.time.chrono.HijrahDate";
            case JAPANESE_DATE -> "java.time.chrono.JapaneseDate";
            case MINGUO_DATE -> "java.time.chrono.MinguoDate";
            case THAI_BUDDHIST_DATE -> "java.time.chrono.ThaiBuddhistDate";
            case LONG, INTEGER, SHORT, BYTE -> "java.time.Instant";
            case DATE, CALENDAR -> "";
        };
    }

    /**
     * Whether the field value needs a {@code (long)} widening cast before being
     * passed to {@code Constraint.validate()}.  Only true for {@code int},
     * {@code short}, and {@code byte} temporal kinds (epoch-millis stored as
     * narrow primitives/boxed types).
     */
    public boolean needsLongCast() {
        return this == INTEGER || this == SHORT || this == BYTE;
    }
}
