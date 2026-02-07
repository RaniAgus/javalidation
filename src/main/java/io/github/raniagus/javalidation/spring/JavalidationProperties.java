package io.github.raniagus.javalidation.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = JavalidationProperties.PREFIX)
public class JavalidationProperties {
    public static final String PREFIX = "io.github.raniagus.javalidation";

    /**
     * Whether to use MessageSource for template resolution
     */
    private boolean useMessageSource = true;

    public boolean isUseMessageSource() {
        return useMessageSource;
    }

    public void setUseMessageSource(boolean useMessageSource) {
        this.useMessageSource = useMessageSource;
    }
}
