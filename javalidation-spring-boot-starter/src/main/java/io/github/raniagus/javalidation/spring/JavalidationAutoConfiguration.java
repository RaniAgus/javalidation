package io.github.raniagus.javalidation.spring;

import static io.github.raniagus.javalidation.spring.JavalidationProperties.PREFIX;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.TemplateString;
import io.github.raniagus.javalidation.format.BracketNotationFormatter;
import io.github.raniagus.javalidation.format.DefaultNotationFormatter;
import io.github.raniagus.javalidation.format.DotNotationFormatter;
import io.github.raniagus.javalidation.format.FieldKeyFormatter;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import io.github.raniagus.javalidation.jackson.FieldKeySerializer;
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
    // -- JavalidationModule --

    @Bean
    public JavalidationModule javalidationModule(
            ValueSerializer<FieldKey> fieldKeySerializer,
            ValueSerializer<TemplateString> templateStringValueSerializer,
            Optional<ValueSerializer<ValidationErrors>> validationErrorsValueSerializer
    ) {
        var builder = JavalidationModule.builder()
                .withFieldKeySerializer(fieldKeySerializer)
                .withTemplateStringSerializer(templateStringValueSerializer);
        validationErrorsValueSerializer.ifPresent(builder::withValidationErrorsSerializer);
        return builder.build();
    }

    @Bean
    public ValueSerializer<FieldKey> fieldKeySerializer(FieldKeyFormatter formatter) {
        return new FieldKeySerializer(formatter);
    }

    // -- FieldKey serialization --

    @Bean
    @ConditionalOnMissingBean(JavalidationProperties.class)
    @ConditionalOnProperty(prefix = PREFIX, name = "key-notation", havingValue = "property_path", matchIfMissing = true)
    public FieldKeyFormatter propertyPathNotationFieldKeyFormatter() {
        return new DefaultNotationFormatter();
    }

    @Bean
    @ConditionalOnProperty(prefix = PREFIX, name = "key-notation", havingValue = "dot")
    public FieldKeyFormatter dotNotationFieldKeyFormatter() {
        return new DotNotationFormatter();
    }

    @Bean
    @ConditionalOnProperty(prefix = PREFIX, name = "key-notation", havingValue = "bracket")
    public FieldKeyFormatter bracketNotationFieldKeyFormatter() {
        return new BracketNotationFormatter();
    }

    // -- TemplateString serialization --

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
    public ValueSerializer<TemplateString> templateStringValueSerializer(TemplateStringFormatter formatter) {
        return new TemplateStringSerializer(formatter);
    }

    // -- ValidationErrors serialization --

    @Bean
    @ConditionalOnProperty(prefix = PREFIX, name = "flatten-errors", havingValue = "true")
    public ValueSerializer<ValidationErrors> flattenedErrorsSerializer() {
        return new FlattenedErrorsSerializer();
    }

}
