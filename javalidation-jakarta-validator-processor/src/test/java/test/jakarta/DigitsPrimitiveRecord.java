package test.jakarta;

import jakarta.validation.constraints.Digits;

public record DigitsPrimitiveRecord(@Digits(integer = 5, fraction = 2) Integer value) {}
