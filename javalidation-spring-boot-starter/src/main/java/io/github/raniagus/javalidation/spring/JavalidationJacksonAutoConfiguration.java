package io.github.raniagus.javalidation.spring;

import static io.github.raniagus.javalidation.spring.JavalidationProperties.PREFIX;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.TemplateString;
import io.github.raniagus.javalidation.format.FieldKeyFormatter;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import io.github.raniagus.javalidation.jackson.FieldKeySerializer;
import io.github.raniagus.javalidation.jackson.FlattenedErrorsSerializer;
import io.github.raniagus.javalidation.jackson.JavalidationModule;
import io.github.raniagus.javalidation.jackson.TemplateStringSerializer;
import java.util.Optional;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ValueSerializer;

@AutoConfiguration
@ConditionalOnClass(name = "tools.jackson.databind.json.JsonMapper")
public class JavalidationJacksonAutoConfiguration {

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

    // -- FieldKey serialization --

    @Bean
    public ValueSerializer<FieldKey> fieldKeySerializer(FieldKeyFormatter formatter) {
        return new FieldKeySerializer(formatter);
    }

    // -- TemplateString serialization --

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
