package test.jakarta;

import jakarta.validation.constraints.*;

public record MaxFloatRecord(@Max(100) Float value) {}
