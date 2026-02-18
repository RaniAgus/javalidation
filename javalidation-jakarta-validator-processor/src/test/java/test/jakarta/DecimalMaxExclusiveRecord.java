package test.jakarta;

import io.github.raniagus.javalidation.validator.*;
import jakarta.validation.constraints.*;
import java.math.*;

@Validate
public record DecimalMaxExclusiveRecord(@DecimalMax(value = "10.5", inclusive = false) BigDecimal value) {}
