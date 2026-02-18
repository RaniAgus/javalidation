package test.iterable;

import io.github.raniagus.javalidation.validator.*;
import jakarta.validation.constraints.*;
import java.util.List;

@Validate
public record NestedIterableRecord(
        List<@NotEmpty List<@NotNull Integer>> scores
) {}