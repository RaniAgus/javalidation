package test.collection;

import io.github.raniagus.javalidation.validator.Validate;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Validate
public record ValidatedMapRecord(
        @NotEmpty Map<@NotNull String, @NotNull Person> friends
) {
    @Validate
    public record Person(@NotNull String name) {}
}