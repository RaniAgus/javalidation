package test.jakarta;

import jakarta.validation.constraints.*;

public record MaxPrimitiveRecord(@Max(100) long value) {}
