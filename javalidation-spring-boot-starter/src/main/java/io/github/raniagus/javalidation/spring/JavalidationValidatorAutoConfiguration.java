package io.github.raniagus.javalidation.spring;

import io.github.raniagus.javalidation.format.FieldKeyFormatter;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import io.github.raniagus.javalidation.validator.JakartaValidatorAdapter;
import jakarta.validation.Validator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(name = "jakarta.validation.Validator")
public class JavalidationValidatorAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(Validator.class)
    public Validator validator(FieldKeyFormatter fieldKeyFormatter, TemplateStringFormatter templateStringFormatter) {
        return new JakartaValidatorAdapter(fieldKeyFormatter, templateStringFormatter);
    }
}
