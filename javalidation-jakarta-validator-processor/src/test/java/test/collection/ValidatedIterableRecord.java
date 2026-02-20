package test.collection;

import io.github.raniagus.javalidation.validator.*;
import jakarta.validation.constraints.*;
import java.util.List;

@Validate
public record ValidatedIterableRecord(
        @NotNull List<@NotNull Person> friends
) {
    @Validate
    public record Person(@NotNull String name) {}
}