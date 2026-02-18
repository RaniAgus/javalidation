package io.github.raniagus.javalidation.validator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

public class ValidatorUtils {
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
