package io.github.raniagus.javalidation.spring;

import static io.github.raniagus.javalidation.spring.JavalidationProperties.PREFIX;

import io.github.raniagus.javalidation.format.FieldKeyFormatter;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import io.github.raniagus.javalidation.validator.JakartaValidatorAdapter;
import jakarta.validation.Validator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(name = {
        "jakarta.validation.Validator",
        "io.github.raniagus.javalidation.validator.JakartaValidatorAdapter"
})
public class JavalidationValidatorAutoConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = PREFIX, name = "use-static-validators", havingValue = "true")
    public Validator validator(FieldKeyFormatter fieldKeyFormatter, TemplateStringFormatter templateStringFormatter) {
        return new JakartaValidatorAdapter(fieldKeyFormatter, templateStringFormatter);
    }
}
