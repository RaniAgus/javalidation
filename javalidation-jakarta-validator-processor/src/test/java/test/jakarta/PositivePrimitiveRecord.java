package test.jakarta;

import jakarta.validation.constraints.*;

public record PositivePrimitiveRecord(@Positive long value) {}
