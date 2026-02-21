package test.jakarta;

import jakarta.validation.constraints.*;

public record NotEmptyRecord(@NotEmpty String value) {}
