package test.jakarta;

import jakarta.validation.constraints.*;

public record PositiveOrZeroPrimitiveRecord(@PositiveOrZero long value) {}
