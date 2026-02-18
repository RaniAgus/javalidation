package test.jakarta;

import io.github.raniagus.javalidation.validator.*;
import jakarta.validation.constraints.*;

@Validate
public record NotNullAndSizeRecord(@NotNull @Size(min = 3, max = 10) String value) {}
