package io.github.raniagus.javalidation.spring;

import static io.github.raniagus.javalidation.spring.JavalidationProperties.PREFIX;

import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.format.TemplateString;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import io.github.raniagus.javalidation.jackson.FlattenedErrorsSerializer;
import io.github.raniagus.javalidation.jackson.JavalidationModule;
import io.github.raniagus.javalidation.jackson.TemplateStringSerializer;
import java.util.Optional;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ValueSerializer;

@AutoConfiguration
@ConditionalOnClass(name = "tools.jackson.databind.json.JsonMapper")
@EnableConfigurationProperties(JavalidationProperties.class)
public class JavalidationAutoConfiguration {

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

    @Bean
    @ConditionalOnProperty(prefix = PREFIX, name = "flatten-errors", havingValue = "true")
    public ValueSerializer<ValidationErrors> flattenedErrorsSerializer() {
        return new FlattenedErrorsSerializer();
    }

    @Bean
    public ValueSerializer<TemplateString> templateStringValueSerializer(TemplateStringFormatter formatter) {
        return new TemplateStringSerializer(formatter);
    }

    @Bean
    public JavalidationModule javalidationModule(
            Optional<ValueSerializer<ValidationErrors>> validationErrorsValueSerializer,
            ValueSerializer<TemplateString> templateStringValueSerializer
    ) {
        var builder = JavalidationModule.builder().withTemplateStringSerializer(templateStringValueSerializer);
        validationErrorsValueSerializer.ifPresent(builder::withValidationErrorsSerializer);
        return builder.build();
    }
}
