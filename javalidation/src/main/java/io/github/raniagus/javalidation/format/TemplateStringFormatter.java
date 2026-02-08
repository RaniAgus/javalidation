package io.github.raniagus.javalidation.format;

/**
 * Strategy interface for formatting {@link TemplateString} instances into plain strings.
 * <p>
 * This functional interface allows pluggable formatting strategies for error messages.
 * Implementations typically use the template and arguments from {@link TemplateString} to
 * produce a formatted output string, potentially considering locale, resource bundles, or
 * other contextual information.
 * <p>
 * <b>Built-in implementations:</b>
 * <ul>
 *   <li>{@link MessageFormatTemplateStringFormatter} - Uses Java's {@link java.text.MessageFormat}</li>
 *   <li>{@code MessageSourceTemplateStringFormatter} - Uses Spring's {@code MessageSource} for i18n</li>
 * </ul>
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * TemplateStringFormatter formatter = TemplateStringFormatter.getDefault();
 * TemplateString template = new TemplateString("Age must be at least {0}", new Object[]{18});
 * String formatted = formatter.format(template); // "Age must be at least 18"
 * }</pre>
 * <p>
 * Custom formatters can be registered with Jackson (via the javalidation-jackson module)
 * or Spring Boot auto-configuration properties (via the javalidation-spring-boot-starter module).
 *
 * @see TemplateString
 * @see MessageFormatTemplateStringFormatter
 */
@FunctionalInterface
public interface TemplateStringFormatter {
    /**
     * Formats a template string into a plain string.
     * <p>
     * Implementations should use the {@link TemplateString#message()} as the template
     * and {@link TemplateString#args()} as the placeholder arguments.
     *
     * @param templateString the template to format (must not be null)
     * @return the formatted string (never null)
     */
    String format(TemplateString templateString);

    /**
     * Returns the default formatter using {@link java.text.MessageFormat} syntax.
     * <p>
     * The default formatter supports placeholders like {@code {0}}, {@code {1}}, etc.
     *
     * @return a {@link MessageFormatTemplateStringFormatter} instance
     */
    static TemplateStringFormatter getDefault() {
        return new MessageFormatTemplateStringFormatter();
    }
}
