package test.jakarta;

import jakarta.validation.constraints.*;

public record MultiPatternRecord(@Pattern(regexp = "^[a-z]+$") @Pattern(regexp = "^.{3,10}$") String value) {}
