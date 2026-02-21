package test.collection;

import jakarta.validation.constraints.*;
import java.util.List;

public record PrimitiveIterableRecord(
        @NotNull List<@Size(min = 3, max = 10) String> tags
) {}