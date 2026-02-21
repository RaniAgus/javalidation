package test.collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record ValidatedIterableRecord(
        @NotNull List<@NotNull @Valid Person> friends
) {
    public record Person(@NotNull String name) {}
}