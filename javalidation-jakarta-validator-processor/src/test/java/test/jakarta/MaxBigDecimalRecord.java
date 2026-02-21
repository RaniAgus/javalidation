package test.jakarta;

import jakarta.validation.constraints.Max;
import java.math.BigDecimal;
import java.math.BigInteger;

public record MaxBigDecimalRecord(@Max(100) BigDecimal value) {}
