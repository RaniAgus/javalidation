package test.jakarta;

import io.github.raniagus.javalidation.validator.*;
import jakarta.validation.constraints.*;
import java.math.*;

@Validate
public record DecimalMinInclusiveRecord(@DecimalMin("10.5") BigDecimal value) {}
