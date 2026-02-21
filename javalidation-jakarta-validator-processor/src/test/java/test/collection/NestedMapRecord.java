package test.collection;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record NestedMapRecord(
        Map<@NotNull String, @NotEmpty Map<@NotNull String, @NotNull Integer>> scores
) {}