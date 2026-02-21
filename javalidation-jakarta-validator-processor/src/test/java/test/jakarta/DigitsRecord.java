package test.jakarta;

import jakarta.validation.constraints.*;
import java.math.*;

public record DigitsRecord(@Digits(integer = 5, fraction = 2) BigDecimal value) {}
