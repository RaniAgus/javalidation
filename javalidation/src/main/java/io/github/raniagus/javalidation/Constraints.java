package io.github.raniagus.javalidation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

public final class Constraints {

    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";

    private Constraints() {}

    // -------------------------------------------------------------------------
    // NullSafeWriter-mapped constraints
    // -------------------------------------------------------------------------

    public static <T extends @Nullable Object> Constraint<T> notNull() {
        return new Constraint<>(Objects::nonNull, "io.github.raniagus.javalidation.constraints.NotNull.message");
    }

    public static <T extends @Nullable Object> Constraint<T> nullValue() {
        return new Constraint<>(Objects::isNull, "io.github.raniagus.javalidation.constraints.Null.message");
    }

    public static <T extends @Nullable String> Constraint<T> notEmpty() {
        return new Constraint<>(obj -> obj != null && !obj.isEmpty(), "io.github.raniagus.javalidation.constraints.NotEmpty.message");
    }

    public static <T extends @Nullable Collection<?>> Constraint<T> notEmptyCollection() {
        return new Constraint<>(obj -> obj != null && !obj.isEmpty(), "io.github.raniagus.javalidation.constraints.NotEmpty.message");
    }

    public static <T extends @Nullable String> Constraint<T> notBlank() {
        return new Constraint<>(obj -> obj != null && !obj.isBlank(), "io.github.raniagus.javalidation.constraints.NotBlank.message");
    }

    // -------------------------------------------------------------------------
    // Size — named after the underlying accessor to avoid overload conflicts
    // -------------------------------------------------------------------------

    /** For {@code String} fields — maps to {@code String.length()}. */
    public static <T extends @Nullable String> Constraint<T> length(int min, int max) {
        return new Constraint<>(
                obj -> obj != null && obj.length() >= min && obj.length() <= max,
                "io.github.raniagus.javalidation.constraints.Size.message",
                min, max
        );
    }

    /** For {@code Collection} fields — maps to {@code Collection.size()}. */
    public static <T extends @Nullable Collection<?>> Constraint<T> size(int min, int max) {
        return new Constraint<>(
                obj -> obj != null && obj.size() >= min && obj.size() <= max,
                "io.github.raniagus.javalidation.constraints.Size.message",
                min, max
        );
    }

    /** For {@code Map} fields — maps to {@code Map.size()}. */
    public static <T extends @Nullable Map<?, ?>> Constraint<T> sizeMap(int min, int max) {
        return new Constraint<>(
                obj -> obj != null && obj.size() >= min && obj.size() <= max,
                "io.github.raniagus.javalidation.constraints.Size.message",
                min, max
        );
    }

    // -------------------------------------------------------------------------
    // AssertTrue / AssertFalse
    // -------------------------------------------------------------------------

    public static <T extends @Nullable Boolean> Constraint<T> assertTrue() {
        return new Constraint<>(Boolean.TRUE::equals, "io.github.raniagus.javalidation.constraints.AssertTrue.message");
    }

    public static <T extends @Nullable Boolean> Constraint<T> assertFalse() {
        return new Constraint<>(Boolean.FALSE::equals, "io.github.raniagus.javalidation.constraints.AssertFalse.message");
    }

    // -------------------------------------------------------------------------
    // Min — separate overloads per numeric kind
    // -------------------------------------------------------------------------

    public static Constraint<Long> minLong(long value) {
        return new Constraint<>(obj -> obj >= value, "io.github.raniagus.javalidation.constraints.Min.message", value);
    }

    public static Constraint<BigDecimal> minBigDecimal(long value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        return new Constraint<>(obj -> obj.compareTo(bd) >= 0, "io.github.raniagus.javalidation.constraints.Min.message", value);
    }

    public static Constraint<BigInteger> minBigInteger(long value) {
        BigInteger bi = BigInteger.valueOf(value);
        return new Constraint<>(obj -> obj.compareTo(bi) >= 0, "io.github.raniagus.javalidation.constraints.Min.message", value);
    }

    public static Constraint<BigDecimal> min(BigDecimal value) {
        return new Constraint<>(obj -> obj.compareTo(value) >= 0, "io.github.raniagus.javalidation.constraints.Min.message", value);
    }

    public static Constraint<BigInteger> min(BigInteger value) {
        return new Constraint<>(obj -> obj.compareTo(value) >= 0, "io.github.raniagus.javalidation.constraints.Min.message", value);
    }

    /** For {@code Number} and {@code CharSequence} fields — converts via {@code BigDecimal}. */
    public static <T> Constraint<T> minNumber(long value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        return new Constraint<>(obj -> new BigDecimal(obj.toString()).compareTo(bd) >= 0,
                "io.github.raniagus.javalidation.constraints.Min.message", value);
    }

    // -------------------------------------------------------------------------
    // Max — separate overloads per numeric kind
    // -------------------------------------------------------------------------

    public static Constraint<Long> maxLong(long value) {
        return new Constraint<>(obj -> obj <= value, "io.github.raniagus.javalidation.constraints.Max.message", value);
    }

    public static Constraint<BigDecimal> maxBigDecimal(long value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        return new Constraint<>(obj -> obj.compareTo(bd) <= 0, "io.github.raniagus.javalidation.constraints.Max.message", value);
    }

    public static Constraint<BigInteger> maxBigInteger(long value) {
        BigInteger bi = BigInteger.valueOf(value);
        return new Constraint<>(obj -> obj.compareTo(bi) <= 0, "io.github.raniagus.javalidation.constraints.Max.message", value);
    }

    public static Constraint<BigDecimal> max(BigDecimal value) {
        return new Constraint<>(obj -> obj.compareTo(value) <= 0, "io.github.raniagus.javalidation.constraints.Max.message", value);
    }

    public static Constraint<BigInteger> max(BigInteger value) {
        return new Constraint<>(obj -> obj.compareTo(value) <= 0, "io.github.raniagus.javalidation.constraints.Max.message", value);
    }

    /** For {@code Number} and {@code CharSequence} fields — converts via {@code BigDecimal}. */
    public static <T> Constraint<T> maxNumber(long value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        return new Constraint<>(obj -> new BigDecimal(obj.toString()).compareTo(bd) <= 0,
                "io.github.raniagus.javalidation.constraints.Max.message", value);
    }

    // -------------------------------------------------------------------------
    // Positive / PositiveOrZero / Negative / NegativeOrZero
    // -------------------------------------------------------------------------

    public static Constraint<Long> positive() {
        return new Constraint<>(obj -> obj > 0, "io.github.raniagus.javalidation.constraints.Positive.message");
    }

    public static Constraint<BigDecimal> positiveBigDecimal() {
        return new Constraint<>(obj -> obj.compareTo(BigDecimal.ZERO) > 0, "io.github.raniagus.javalidation.constraints.Positive.message");
    }

    public static Constraint<BigInteger> positiveBigInteger() {
        return new Constraint<>(obj -> obj.compareTo(BigInteger.ZERO) > 0, "io.github.raniagus.javalidation.constraints.Positive.message");
    }

    public static <T> Constraint<T> positiveNumber() {
        return new Constraint<>(obj -> new BigDecimal(obj.toString()).compareTo(BigDecimal.ZERO) > 0,
                "io.github.raniagus.javalidation.constraints.Positive.message");
    }

    public static Constraint<Long> positiveOrZero() {
        return new Constraint<>(obj -> obj >= 0, "io.github.raniagus.javalidation.constraints.PositiveOrZero.message");
    }

    public static Constraint<BigDecimal> positiveOrZeroBigDecimal() {
        return new Constraint<>(obj -> obj.compareTo(BigDecimal.ZERO) >= 0, "io.github.raniagus.javalidation.constraints.PositiveOrZero.message");
    }

    public static Constraint<BigInteger> positiveOrZeroBigInteger() {
        return new Constraint<>(obj -> obj.compareTo(BigInteger.ZERO) >= 0, "io.github.raniagus.javalidation.constraints.PositiveOrZero.message");
    }

    public static <T> Constraint<T> positiveOrZeroNumber() {
        return new Constraint<>(obj -> new BigDecimal(obj.toString()).compareTo(BigDecimal.ZERO) >= 0,
                "io.github.raniagus.javalidation.constraints.PositiveOrZero.message");
    }

    public static Constraint<Long> negative() {
        return new Constraint<>(obj -> obj < 0, "io.github.raniagus.javalidation.constraints.Negative.message");
    }

    public static Constraint<BigDecimal> negativeBigDecimal() {
        return new Constraint<>(obj -> obj.compareTo(BigDecimal.ZERO) < 0, "io.github.raniagus.javalidation.constraints.Negative.message");
    }

    public static Constraint<BigInteger> negativeBigInteger() {
        return new Constraint<>(obj -> obj.compareTo(BigInteger.ZERO) < 0, "io.github.raniagus.javalidation.constraints.Negative.message");
    }

    public static <T> Constraint<T> negativeNumber() {
        return new Constraint<>(obj -> new BigDecimal(obj.toString()).compareTo(BigDecimal.ZERO) < 0,
                "io.github.raniagus.javalidation.constraints.Negative.message");
    }

    public static Constraint<Long> negativeOrZero() {
        return new Constraint<>(obj -> obj <= 0, "io.github.raniagus.javalidation.constraints.NegativeOrZero.message");
    }

    public static Constraint<BigDecimal> negativeOrZeroBigDecimal() {
        return new Constraint<>(obj -> obj.compareTo(BigDecimal.ZERO) <= 0, "io.github.raniagus.javalidation.constraints.NegativeOrZero.message");
    }

    public static Constraint<BigInteger> negativeOrZeroBigInteger() {
        return new Constraint<>(obj -> obj.compareTo(BigInteger.ZERO) <= 0, "io.github.raniagus.javalidation.constraints.NegativeOrZero.message");
    }

    public static <T> Constraint<T> negativeOrZeroNumber() {
        return new Constraint<>(obj -> new BigDecimal(obj.toString()).compareTo(BigDecimal.ZERO) <= 0,
                "io.github.raniagus.javalidation.constraints.NegativeOrZero.message");
    }

    // -------------------------------------------------------------------------
    // DecimalMin / DecimalMax
    // -------------------------------------------------------------------------

    public static <T> Constraint<T> decimalMin(String value) {
        return decimalMin(value, true);
    }

    public static <T> Constraint<T> decimalMin(String value, boolean inclusive) {
        BigDecimal bd = new BigDecimal(value);
        String messageKey = "io.github.raniagus.javalidation.constraints.DecimalMin" + (inclusive ? "" : ".exclusive") + ".message";
        return new Constraint<>(
                obj -> inclusive
                        ? new BigDecimal(obj.toString()).compareTo(bd) >= 0
                        : new BigDecimal(obj.toString()).compareTo(bd) > 0,
                messageKey, value
        );
    }

    public static <T> Constraint<T> decimalMax(String value) {
        return decimalMax(value, true);
    }

    public static <T> Constraint<T> decimalMax(String value, boolean inclusive) {
        BigDecimal bd = new BigDecimal(value);
        String messageKey = "io.github.raniagus.javalidation.constraints.DecimalMax" + (inclusive ? "" : ".exclusive") + ".message";
        return new Constraint<>(
                obj -> inclusive
                        ? new BigDecimal(obj.toString()).compareTo(bd) <= 0
                        : new BigDecimal(obj.toString()).compareTo(bd) < 0,
                messageKey, value
        );
    }

    // -------------------------------------------------------------------------
    // Email / Pattern
    // -------------------------------------------------------------------------

    public static <T extends @Nullable String> Constraint<T> email() {
        Pattern compiled = Pattern.compile(EMAIL_REGEX);
        return new Constraint<>(obj -> obj != null && compiled.matcher(obj).matches(),
                "io.github.raniagus.javalidation.constraints.Email.message");
    }

    public static <T extends @Nullable String> Constraint<T> pattern(String regex) {
        Pattern compiled = Pattern.compile(regex);
        return new Constraint<>(obj -> obj != null && compiled.matcher(obj).matches(),
                "io.github.raniagus.javalidation.constraints.Pattern.message", regex);
    }

    // -------------------------------------------------------------------------
    // Digits
    // -------------------------------------------------------------------------

    public static <T> Constraint<T> digits(int integer, int fraction) {
        return new Constraint<>(obj -> {
            BigDecimal bd = new BigDecimal(obj.toString()).stripTrailingZeros();
            return bd.precision() - bd.scale() <= integer && Math.max(bd.scale(), 0) <= fraction;
        }, "io.github.raniagus.javalidation.constraints.Digits.message", integer, fraction);
    }

    public static <T extends @Nullable String> Constraint<T> digitsString(int integer, int fraction) {
        Pattern compiled = Pattern.compile("^-?\\d{0," + integer + "}(\\.\\d{0," + fraction + "})?$");
        return new Constraint<>(obj -> obj != null && compiled.matcher(obj).matches(),
                "io.github.raniagus.javalidation.constraints.Digits.message", integer, fraction);
    }

    // -------------------------------------------------------------------------
    // Temporal — past / future / pastOrPresent / futureOrPresent
    // Uses Supplier<? extends T> so the "now" value is computed at validation
    // time, not at Constraint construction time (which would be stale for static
    // fields).  T must be Comparable<T> so compareTo can determine ordering.
    // -------------------------------------------------------------------------

    public static <T extends Comparable<? super T>> Constraint<T> past(Supplier<T> now) {
        return new Constraint<>(obj -> obj.compareTo(now.get()) < 0,
                "io.github.raniagus.javalidation.constraints.Past.message");
    }

    public static <T extends Comparable<? super T>> Constraint<T> pastOrPresent(Supplier<T> now) {
        return new Constraint<>(obj -> obj.compareTo(now.get()) <= 0,
                "io.github.raniagus.javalidation.constraints.PastOrPresent.message");
    }

    public static <T extends Comparable<? super T>> Constraint<T> future(Supplier<T> now) {
        return new Constraint<>(obj -> obj.compareTo(now.get()) > 0,
                "io.github.raniagus.javalidation.constraints.Future.message");
    }

    public static <T extends Comparable<? super T>> Constraint<T> futureOrPresent(Supplier<T> now) {
        return new Constraint<>(obj -> obj.compareTo(now.get()) >= 0,
                "io.github.raniagus.javalidation.constraints.FutureOrPresent.message");
    }
}
