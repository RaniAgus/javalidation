package io.github.raniagus.javalidation.spring;

import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.format.BracketNotationFormatter;
import io.github.raniagus.javalidation.format.DotNotationFormatter;
import io.github.raniagus.javalidation.format.PropertyPathNotationFormatter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = JavalidationProperties.PREFIX)
public class JavalidationProperties {
    public static final String PREFIX = "io.github.raniagus.javalidation";

    /**
     * The formatter to use for serializing field keys. This can be set to one of the following values:
     * <ul>
     *     <li>{@code property_path} (default): uses dots for properties and square brackets for indices
     *         ({@link PropertyPathNotationFormatter})</li>
     *     <li>{@code dots}: uses dots as separators ({@link DotNotationFormatter})</li>
     *     <li>{@code brackets}: uses square brackets as separators ({@link BracketNotationFormatter})</li>
     * </ul>
     */
    private KeyNotation keyNotation = KeyNotation.PROPERTY_PATH;

    /**
     * Whether to use {@link org.springframework.context.MessageSource} to resolve error messages. When false,
     * the default formatter with {@link java.text.MessageFormat#format(String, Object...)} is used.
     * This feature is enabled by default.
     */
    private boolean useMessageSource = true;

    /**
     * Whether to flatten ValidationErrors into a single object, with empty string as parts for root errors. When false,
     * the nested structure is preserved, separating {@link ValidationErrors#rootErrors()} and
     * {@link ValidationErrors#fieldErrors()}.
     * This feature is disabled by default.
     */
    private boolean flattenErrors = false;

    /**
     * Whether to use compile-time-generated {@link io.github.raniagus.javalidation.validator.Validators} when using
     * {@link jakarta.validation.Valid} annotation. When true, make sure that {@code key-notation} is set to the same
     * value as the one passed to the compiler argument {@code -Aio.github.raniagus.javalidation.key-notation}.
     * This feature is disabled by default.
     */
    private boolean useStaticValidators = false;

    public KeyNotation getKeyNotation() {
        return keyNotation;
    }

    public void setKeyNotation(KeyNotation keyNotation) {
        this.keyNotation = keyNotation;
    }

    public boolean isUseMessageSource() {
        return useMessageSource;
    }

    public void setUseMessageSource(boolean useMessageSource) {
        this.useMessageSource = useMessageSource;
    }

    public boolean isFlattenErrors() {
        return flattenErrors;
    }

    public void setFlattenErrors(boolean flattenErrors) {
        this.flattenErrors = flattenErrors;
    }

    public boolean isUseStaticValidators() {
        return useStaticValidators;
    }

    public void setUseStaticValidators(boolean useStaticValidators) {
        this.useStaticValidators = useStaticValidators;
    }
}
