package test.jakarta;

import jakarta.validation.constraints.*;

public record NotNullAndSizeRecord(@NotNull @Size(min = 3, max = 10) String value) {}
