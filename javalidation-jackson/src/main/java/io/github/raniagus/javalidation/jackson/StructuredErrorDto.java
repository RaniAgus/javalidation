package io.github.raniagus.javalidation.jackson;

import io.github.raniagus.javalidation.TemplateString;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import java.util.Arrays;
import java.util.Objects;

/**
 * DTO for serializing and deserializing {@link TemplateString} with full structure preservation.
 * <p>
 * This record includes three components:
 * <ul>
 *   <li><b>message</b>: Pre-formatted message using configured {@link TemplateStringFormatter} (for immediate display)</li>
 *   <li><b>code</b>: Message code/template pattern (i18n key or template string for client-side re-formatting)</li>
 *   <li><b>args</b>: Arguments array (for reconstruction and re-formatting)</li>
 * </ul>
 * <p>
 * This hybrid approach enables both immediate use (via {@code message}) and full reconstruction
 * capabilities (via {@code code} and {@code args}). The {@code code} field serves dual purposes:
 * <ul>
 *   <li>As an i18n message code (e.g., {@code "user.age.minimum"}) for Spring MessageSource resolution</li>
 *   <li>As a MessageFormat template pattern (e.g., {@code "Must be at least {0}"}) for client-side formatting</li>
 * </ul>
 *
 * @param message the formatted error message
 * @param code the message code (i18n key) or template pattern
 * @param args the template arguments
 */
record StructuredErrorDto(String message, String code, Object[] args) {
    /**
     * Compact constructor with defensive array copying.
     */
    public StructuredErrorDto {
        args = Arrays.copyOf(args, args.length);
    }

    /**
     * Creates a DTO from a {@link TemplateString}, formatting the message with the provided formatter.
     *
     * @param templateString the template string to convert
     * @param formatter the formatter to use for the message field
     * @return a new DTO with formatted message and preserved template structure
     */
    static StructuredErrorDto from(TemplateString templateString, TemplateStringFormatter formatter) {
        return new StructuredErrorDto(
                formatter.format(templateString),
                templateString.message(),
                templateString.args()
        );
    }

    /**
     * Converts this DTO back to a {@link TemplateString}.
     *
     * @return a new TemplateString with the code and args
     */
    TemplateString toTemplateString() {
        return TemplateString.of(code, args);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StructuredErrorDto(String msg, String cd, Object[] argz))) return false;
        return Objects.equals(message, msg) 
            && Objects.equals(code, cd) 
            && Arrays.equals(args, argz);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(message);
        result = 31 * result + Objects.hashCode(code);
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }

    @Override
    public String toString() {
        return "StructuredErrorDto{" +
                "message='" + message + '\'' +
                ", code='" + code + '\'' +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
