package test.jakarta;

import jakarta.validation.constraints.*;

public record PatternFlagsRecord(@Pattern(regexp = "^[a-z]+$", flags = {Pattern.Flag.CASE_INSENSITIVE, Pattern.Flag.MULTILINE}) String value) {}
