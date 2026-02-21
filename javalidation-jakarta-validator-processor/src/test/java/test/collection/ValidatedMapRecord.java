package test.collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record ValidatedMapRecord(
        @NotEmpty Map<@NotNull String, @NotNull @Valid Person> friends
) {
    public record Person(@NotNull String name) {}
}