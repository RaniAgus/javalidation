package test.jakarta;

import io.github.raniagus.javalidation.validator.*;
import jakarta.validation.constraints.*;

@Validate
public record SizeMinMaxRecord(@Size(min = 1, max = 10) String value) {}
