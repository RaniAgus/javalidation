package test.collection;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record PrimitiveMapRecord(
        @NotEmpty Map<@NotNull String, @NotNull String> tags
) {}