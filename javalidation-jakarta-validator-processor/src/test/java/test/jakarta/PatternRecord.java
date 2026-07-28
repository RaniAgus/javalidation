package test.jakarta;

import jakarta.validation.constraints.*;

public record PatternRecord(@Pattern(regexp = "^[\\p{IsLatin}\\p{M}]+$") String value) {}
