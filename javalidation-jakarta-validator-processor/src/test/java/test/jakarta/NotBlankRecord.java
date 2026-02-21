package test.jakarta;

import jakarta.validation.constraints.*;

public record NotBlankRecord(@NotBlank String value) {}
