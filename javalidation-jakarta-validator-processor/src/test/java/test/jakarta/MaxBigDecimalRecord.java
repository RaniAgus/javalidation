package test.jakarta;

import io.github.raniagus.javalidation.validator.Validate;
import jakarta.validation.constraints.Max;
import java.math.BigDecimal;
import java.math.BigInteger;

@Validate
public record MaxBigDecimalRecord(@Max(100) BigDecimal value) {}
