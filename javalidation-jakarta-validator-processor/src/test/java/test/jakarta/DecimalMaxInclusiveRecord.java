package test.jakarta;

import jakarta.validation.constraints.*;
import java.math.*;

public record DecimalMaxInclusiveRecord(@DecimalMax("10.5") BigDecimal value) {}
