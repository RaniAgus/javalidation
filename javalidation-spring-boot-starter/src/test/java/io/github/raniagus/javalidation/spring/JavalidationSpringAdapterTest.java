package io.github.raniagus.javalidation.spring;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.format.TemplateString;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

class JavalidationSpringAdapterTest {
    record Person(String name, int age) {
    }

    @Test
    void givenComplexSpringErrors_whenToValidationErrors_thenConvertsAllErrors() {
        // given
        Person target = new Person("", 0);
        Errors errors = new BeanPropertyBindingResult(target, "target");

        errors.reject("global.error", "global error message");
        errors.rejectValue("name", "name.required", "name is required");
        errors.rejectValue("name", "name.tooShort", new Object[]{3}, "name too short");
        errors.rejectValue("age", "age.invalid", new Object[]{18}, "age must be >= {0}");

        // when
        ValidationErrors validationErrors =
                JavalidationSpringAdapter.toValidationErrors(errors);

        // then
        assertThat(validationErrors.isNotEmpty()).isTrue();

        // global / root errors
        assertThat(validationErrors.rootErrors())
                .hasSize(1)
                .first()
                .extracting(TemplateString::message)
                .isEqualTo("global error message");

        // field errors
        assertThat(validationErrors.fieldErrors())
                .hasSize(2)
                .containsKeys("name", "age");

        assertThat(validationErrors.fieldErrors().get("name"))
                .hasSize(2)
                .extracting(TemplateString::message)
                .containsExactly(
                        "name is required",
                        "name too short"
                );

        assertThat(validationErrors.fieldErrors().get("age"))
                .singleElement()
                .satisfies(ts -> {
                    assertThat(ts.message()).isEqualTo("age must be >= {0}");
                    assertThat(ts.args()).containsExactly(18);
                });
    }
}
