package io.github.raniagus.javalidation.validator;

import java.math.BigDecimal;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class Predicates {
    private static final Pattern EMAIL_REGEX = Pattern.compile("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");

    public static boolean isEmail(CharSequence value) {
        return EMAIL_REGEX.matcher(value).matches();
    }

    public static Predicate<CharSequence> digits(int integer, int fraction) {
        Pattern regex = Pattern.compile("^-?\\d{1,%d}(\\.\\d{1,%d})?$".formatted(integer, fraction));
        return value -> regex.matcher(value).matches();
    }

    public static boolean digits(BigDecimal value, int integer, int fraction) {
        BigDecimal normalized = value.stripTrailingZeros();
        return normalized.precision() - normalized.scale() <= integer && Math.max(0, normalized.scale()) <= fraction;
    }
}
