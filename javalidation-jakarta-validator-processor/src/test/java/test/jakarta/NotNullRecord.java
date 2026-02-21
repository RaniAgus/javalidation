package test.jakarta;

import jakarta.validation.constraints.*;

public record NotNullRecord(@NotNull String value) {}
