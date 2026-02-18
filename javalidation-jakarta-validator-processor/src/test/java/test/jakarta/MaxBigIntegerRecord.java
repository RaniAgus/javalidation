package test.jakarta;

import io.github.raniagus.javalidation.validator.Validate;
import jakarta.validation.constraints.Max;
import java.math.BigInteger;

@Validate
public record MaxBigIntegerRecord(@Max(100) BigInteger value) {}
