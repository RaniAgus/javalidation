package test.jakarta;

import jakarta.validation.constraints.*;
import java.math.*;

public record DecimalMinExclusiveRecord(@DecimalMin(value = "10.5", inclusive = false) BigDecimal value) {}
