package io.github.raniagus.javalidation.spring;

import io.github.raniagus.javalidation.ValidationErrors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = JavalidationProperties.PREFIX)
public class JavalidationProperties {
    public static final String PREFIX = "io.github.raniagus.javalidation";

    /**
     * Whether to use {@link org.springframework.context.MessageSource} to resolve error messages. When false,
     * the default formatter with {@link java.text.MessageFormat#format(String, Object...)} is used.
     * This feature is enabled by default.
     */
    private boolean useMessageSource = true;

    /**
     * Whether to flatten ValidationErrors into a single object. When false, the nested structure is preserved,
     * separating {@link ValidationErrors#rootErrors()} and {@link ValidationErrors#fieldErrors()}.
     * This feature is enabled by default.
     */
    private boolean flattenErrors = true;

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
}
