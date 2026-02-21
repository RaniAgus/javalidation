package test.jakarta;

import jakarta.validation.constraints.*;

public record PatternRecord(@Pattern(regexp = "^[a-z]+$") String value) {}
