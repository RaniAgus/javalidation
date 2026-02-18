package test.jakarta;

import io.github.raniagus.javalidation.validator.*;
import jakarta.validation.constraints.*;

@Validate
public record SizeMaxOnlyRecord(@Size(max = 10) String value) {}
