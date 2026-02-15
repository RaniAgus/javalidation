package io.github.raniagus.javalidation.processor;

import java.util.Map;

public final class JakartaValidationAdapter {
    private static final Map<String, String> DEFAULT_MESSAGES = Map.ofEntries(
            Map.entry("{jakarta.validation.constraints.NotNull.message}", "must not be null"),
            Map.entry("{jakarta.validation.constraints.NotBlank.message}", "must not be blank"),
            Map.entry("{jakarta.validation.constraints.NotEmpty.message}", "must not be empty"),
            Map.entry("{jakarta.validation.constraints.Size.message}", "size must be between {min} and {max}"),
            Map.entry("{jakarta.validation.constraints.Min.message}", "must be greater than or equal to {value}"),
            Map.entry("{jakarta.validation.constraints.Max.message}", "must be less than or equal to {value}"),
            Map.entry("{jakarta.validation.constraints.Email.message}", "must be a well-formed email address"),
            Map.entry("{jakarta.validation.constraints.Pattern.message}", "must match \"{regexp}\""),
            Map.entry("{jakarta.validation.constraints.Positive.message}", "must be greater than 0"),
            Map.entry("{jakarta.validation.constraints.PositiveOrZero.message}", "must be greater than or equal to 0"),
            Map.entry("{jakarta.validation.constraints.Negative.message}", "must be less than 0"),
            Map.entry("{jakarta.validation.constraints.NegativeOrZero.message}", "must be less than or equal to 0")
    );

    private JakartaValidationAdapter() {}

    /**
     * Resolves a Jakarta validation message and replaces named placeholders with positional ones.
     *
     * @param message The message template (e.g., "{jakarta.validation.constraints.Size.message}" or "custom message")
     * @param params The parameter names in the order they should appear (e.g., "min", "max")
     * @return Message with {paramName} replaced by {0}, {1}, etc.
     * Example:
     * resolveMessage("{jakarta.validation.constraints.Size.message}", "min", "max")
     * -> "size must be between {0} and {1}"
     */
    public static String resolveMessage(String message, String... params) {
        // First, resolve default message if it's a key reference
        String resolved = DEFAULT_MESSAGES.getOrDefault(message, message);

        // Replace named placeholders with positional ones
        for (int i = 0; i < params.length; i++) {
            resolved = resolved.replace(params[i], "{" + i + "}");
        }

        return resolved;
    }

}
