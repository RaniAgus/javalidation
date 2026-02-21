package test.jakarta;

import jakarta.validation.constraints.*;
import java.math.*;

public record DecimalMaxExclusiveRecord(@DecimalMax(value = "10.5", inclusive = false) BigDecimal value) {}
