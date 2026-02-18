package test.jakarta;

import io.github.raniagus.javalidation.validator.*;
import jakarta.validation.constraints.*;

@Validate
public record MaxPrimitiveRecord(@Max(100) long value) {}
