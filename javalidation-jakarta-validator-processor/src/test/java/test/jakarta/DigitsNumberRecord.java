package test.jakarta;

import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;

public record DigitsNumberRecord(@Digits(integer = 5, fraction = 2) Number value) {}
