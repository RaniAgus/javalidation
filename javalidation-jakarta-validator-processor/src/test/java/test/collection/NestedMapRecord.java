package test.collection;

import io.github.raniagus.javalidation.validator.Validate;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Validate
public record NestedMapRecord(
        Map<@NotNull String, @NotEmpty Map<@NotNull String, @NotNull Integer>> scores
) {}