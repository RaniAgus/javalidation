package test.jakarta;

import jakarta.validation.constraints.*;

public record NotNullAndMinRecord(@NotNull @Min(10) Integer value) {}
