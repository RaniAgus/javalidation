package io.github.raniagus.javalidation.validator;

import java.math.BigDecimal;
import java.math.BigInteger;

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
}
