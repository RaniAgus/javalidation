package test.jakarta;

import jakarta.validation.constraints.*;

public record MinIntegerRecord(@Min(10) int value) {}
