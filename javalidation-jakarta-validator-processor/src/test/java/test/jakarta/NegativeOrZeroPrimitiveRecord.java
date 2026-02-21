package test.jakarta;

import jakarta.validation.constraints.*;

public record NegativeOrZeroPrimitiveRecord(@NegativeOrZero long value) {}
