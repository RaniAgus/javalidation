package test.jakarta;

import jakarta.validation.constraints.*;
import java.math.*;

public record DecimalMinInclusiveRecord(@DecimalMin("10.5") BigDecimal value) {}
