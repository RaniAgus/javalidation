package io.github.raniagus.javalidation.format;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable container for an error message template with optional arguments.
 * <p>
 * Rather than formatting error messages immediately, {@code TemplateString} stores the template
 * and arguments separately. This enables:
 * <ul>
 *   <li><b>Internationalization</b>: Messages can be formatted using different locales at serialization time</li>
 *   <li><b>Flexible formatting</b>: Different formatters can be plugged in (MessageFormat, Spring MessageSource, etc.)</li>
 *   <li><b>Testability</b>: Assertions can verify the raw template and arguments</li>
 * </ul>
 * <p>
 * Templates use Java's {@link java.text.MessageFormat} syntax by default, with placeholders like
 * {@code {0}}, {@code {1}}, etc.:
 * <pre>{@code
 * TemplateString ts = new TemplateString("User must be at least {0} years old", new Object[]{18});
 * // Format later based on locale/formatter:
 * // English: "User must be at least 18 years old"
 * // Spanish: "El usuario debe tener al menos 18 años"
 * }</pre>
 * <p>
 * This record provides defensive array copying to ensure immutability, and custom {@code equals()},
 * {@code hashCode()}, and {@code toString()} implementations that properly handle array semantics.
 * <p>
 * Formatting is performed by {@link TemplateStringFormatter} implementations at serialization time.
 *
 * @param message the message template (must not be null)
 * @param args the arguments for the template placeholders (defensive copy is made)
 * @see TemplateStringFormatter
 * @see io.github.raniagus.javalidation.ValidationErrors
 */
public record TemplateString(String message, Object[] args) {
    /**
     * Compact constructor that creates a defensive copy of the arguments array.
     * <p>
     * This ensures the {@code TemplateString} is truly immutable even if the caller
     * retains a reference to the original array.
     */
    public TemplateString {
        args = Arrays.copyOf(args, args.length);
    }

    /**
     * Static factory method for creating a {@code TemplateString}.
     * <p>
     * Example:
     * <pre>{@code
     * TemplateString ts = TemplateString.of("Age must be at least {0}", 18);
     * }</pre>
     *
     * @param message the message template
     * @param args the arguments for the template
     * @return a new {@code TemplateString}
     */
    public static TemplateString of(String message, Object... args) {
        return new TemplateString(message, args);
    }

    @Override
    public String toString() {
        return "TemplateString{" +
                "message='" + message + '\'' +
                ", args=" + Arrays.toString(args) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TemplateString(String template1, Object[] values1))) return false;
        return Objects.equals(message(), template1) && Arrays.equals(args(), values1);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(message());
        result = 31 * result + Arrays.hashCode(args());
        return result;
    }
}
