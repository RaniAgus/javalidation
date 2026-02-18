package test.jakarta;

import io.github.raniagus.javalidation.validator.*;
import jakarta.validation.constraints.*;

@Validate
public record PatternRecord(@Pattern(regexp = "^[a-z]+$") String value) {}
