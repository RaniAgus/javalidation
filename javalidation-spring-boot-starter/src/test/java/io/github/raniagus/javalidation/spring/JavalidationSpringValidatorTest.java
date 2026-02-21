package io.github.raniagus.javalidation.spring;

import static io.github.raniagus.javalidation.spring.JavalidationSpringValidator.toValidationErrors;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.raniagus.javalidation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;
import org.springframework.validation.SimpleErrors;

class JavalidationSpringValidatorTest {
    JavalidationSpringValidator validator = new JavalidationSpringValidator();

    @Test
    void givenErrors_whenToValidationErrors_thenShouldConvertToValidationErrors() {
        record Value(String field) {}

        Validation validation = Validation.create();
        validation.addError("root error");
        validation.addErrorAt("field", "error");

        Errors errors = new SimpleErrors(new Value("test"));
        validator.toErrors(validation.finish(), errors);

        assertThat(toValidationErrors(errors)).isEqualTo(validation.finish());
    }
}
