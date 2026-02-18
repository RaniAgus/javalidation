package test.jakarta;

import io.github.raniagus.javalidation.validator.*;
import jakarta.validation.constraints.*;

@Validate
public record NotNullAndMinRecord(@NotNull @Min(10) Integer value) {}
