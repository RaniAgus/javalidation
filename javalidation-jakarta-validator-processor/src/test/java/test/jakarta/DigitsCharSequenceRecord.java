package test.jakarta;

import jakarta.validation.constraints.Digits;

public record DigitsCharSequenceRecord(@Digits(integer = 5, fraction = 2) String value) {}

