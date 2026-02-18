package test.jakarta;

import io.github.raniagus.javalidation.validator.*;
import jakarta.validation.constraints.*;

@Validate
public record MinRecord(@Min(10) long value) {}
