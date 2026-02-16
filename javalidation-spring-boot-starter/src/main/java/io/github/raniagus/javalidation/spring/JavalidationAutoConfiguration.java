package io.github.raniagus.javalidation.spring;

import static io.github.raniagus.javalidation.spring.JavalidationProperties.PREFIX;

import io.github.raniagus.javalidation.format.BracketNotationFormatter;
import io.github.raniagus.javalidation.format.DotNotationFormatter;
import io.github.raniagus.javalidation.format.FieldKeyFormatter;
import io.github.raniagus.javalidation.format.PropertyPathNotationFormatter;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(JavalidationProperties.class)
public class JavalidationAutoConfiguration {

    // -- FieldKey formatting --

    @Bean
    @ConditionalOnMissingBean(JavalidationProperties.class)
    @ConditionalOnProperty(prefix = PREFIX, name = "key-notation", havingValue = "property_path", matchIfMissing = true)
    public FieldKeyFormatter propertyPathNotationFieldKeyFormatter() {
        return new PropertyPathNotationFormatter();
    }

    @Bean
    @ConditionalOnProperty(prefix = PREFIX, name = "key-notation", havingValue = "dots")
    public FieldKeyFormatter dotNotationFieldKeyFormatter() {
        return new DotNotationFormatter();
    }

    @Bean
    @ConditionalOnProperty(prefix = PREFIX, name = "key-notation", havingValue = "brackets")
    public FieldKeyFormatter bracketNotationFieldKeyFormatter() {
        return new BracketNotationFormatter();
    }

    // -- TemplateString formatting --

    @Bean
    @ConditionalOnMissingBean(JavalidationProperties.class)
    @ConditionalOnProperty(prefix = PREFIX, name = "use-message-source", havingValue = "false")
    public TemplateStringFormatter defaultTemplateStringFormatter() {
        return TemplateStringFormatter.getDefault();
    }

    @Bean
    @ConditionalOnMissingBean(TemplateStringFormatter.class)
    @ConditionalOnBean(MessageSource.class)
    @ConditionalOnProperty(prefix = PREFIX, name = "use-message-source", havingValue = "true", matchIfMissing = true)
    public TemplateStringFormatter messageSourceTemplateStringFormatter(MessageSource messageSource) {
        return new MessageSourceTemplateStringFormatter(messageSource, defaultTemplateStringFormatter());
    }

}
