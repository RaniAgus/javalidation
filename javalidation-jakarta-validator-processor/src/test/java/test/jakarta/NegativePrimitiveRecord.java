package test.jakarta;

import jakarta.validation.constraints.*;

public record NegativePrimitiveRecord(@Negative long value) {}
