package test.collection;

import jakarta.validation.constraints.*;
import java.util.List;

public record NestedIterableRecord(
        @NotEmpty List<@NotEmpty List<@NotNull Integer>> scores
) {}