package io.github.raniagus.javalidation.spring;

import static io.github.raniagus.javalidation.spring.JavalidationProperties.PREFIX;

import io.github.raniagus.javalidation.format.FieldKeyFormatter;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ConditionalOnClass(name = {
        "io.github.raniagus.javalidation.annotation.Validate",
        "org.springframework.web.servlet.config.annotation.WebMvcConfigurer",
})
public class JavalidationValidatorAutoConfiguration {
    @Bean
    @Primary
    @ConditionalOnProperty(prefix = PREFIX, name = "use-static-validators", havingValue = "true")
    public JavalidationSpringValidator javalidationSpringValidator(FieldKeyFormatter fieldKeyFormatter, TemplateStringFormatter templateStringFormatter) {
        return new JavalidationSpringValidator(fieldKeyFormatter, templateStringFormatter);
    }

    @Bean
    @ConditionalOnBean(JavalidationSpringValidator.class)
    public WebMvcConfigurer javalidationMvcConfigurer(JavalidationSpringValidator validator) {
        return new WebMvcConfigurer() {
            @Override
            public Validator getValidator() {
                return validator;
            }
        };
    }
}
