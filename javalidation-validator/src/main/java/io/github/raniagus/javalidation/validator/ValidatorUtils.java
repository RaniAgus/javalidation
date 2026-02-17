package io.github.raniagus.javalidation.validator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

public class ValidatorUtils {
    public static int compare(Object o1, String o2) {
        return toBigDecimal(o1).compareTo(new BigDecimal(o2));
    }

    public static BigDecimal toBigDecimal(Object value) {
        return switch (value) {
            case BigDecimal bd -> bd;
            case BigInteger bi -> new BigDecimal(bi);
            case CharSequence cs -> new BigDecimal(cs.toString());
            case Byte b -> BigDecimal.valueOf(b);
            case Short s -> BigDecimal.valueOf(s);
            case Integer i -> BigDecimal.valueOf(i);
            case Long l -> BigDecimal.valueOf(l);
            default -> new BigDecimal(value.toString());
        };
    }

    public static Instant toInstant(Object value) {
        return switch (value) {
            case Instant i -> i;
            case Long l -> Instant.ofEpochMilli(l);
            case Integer i -> Instant.ofEpochMilli(i);
            case Short s -> Instant.ofEpochMilli(s);
            case Byte b -> Instant.ofEpochMilli(b);
            case BigInteger bi -> Instant.ofEpochMilli(bi.longValueExact());
            case BigDecimal bd -> Instant.ofEpochMilli(bd.longValueExact());
            default -> Instant.parse(value.toString());
        };
    }
}
