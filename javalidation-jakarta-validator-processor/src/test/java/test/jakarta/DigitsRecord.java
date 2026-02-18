package test.jakarta;

import io.github.raniagus.javalidation.validator.*;
import jakarta.validation.constraints.*;
import java.math.*;

@Validate
public record DigitsRecord(@Digits(integer = 5, fraction = 2) BigDecimal value) {}
