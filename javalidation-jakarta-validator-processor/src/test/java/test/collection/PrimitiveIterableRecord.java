package test.collection;

import io.github.raniagus.javalidation.validator.*;
import jakarta.validation.constraints.*;
import java.util.List;

@Validate
public record PrimitiveIterableRecord(
        @NotNull List<@Size(min = 3, max = 10) String> tags
) {}