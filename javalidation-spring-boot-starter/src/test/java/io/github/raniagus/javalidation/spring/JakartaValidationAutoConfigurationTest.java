package io.github.raniagus.javalidation.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.format.FieldKeyFormatter;
import io.github.raniagus.javalidation.validator.JakartaValidatorAdapter;
import jakarta.validation.Validator;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.json.JsonMapper;

class JakartaValidationAutoConfigurationTest extends AutoConfigurationTest {
    @SpringBootTest(classes = TestApplication.class)
    static class ContextTest {
        @Autowired(required = false)
        private Validator validator;

        @Test
        void givenAutoConfiguration_whenStartup_thenConfiguresValidator() {
            assertNotNull(validator);
        }
    }

    static class ValidatorUnsetTest extends ContextTest {
        @Autowired
        private Validator validator;

        @Test
        void givenNotationUnset_whenSerialize_thenUsesDefault() {
            assertThat(validator).isNotExactlyInstanceOf(JakartaValidatorAdapter.class);
        }
    }

    @TestPropertySource(properties = "io.github.raniagus.javalidation.use-static-validators=false")
    static class ValidatorFalseTest extends ValidatorUnsetTest {
    }

    @TestPropertySource(properties = "io.github.raniagus.javalidation.use-static-validators=true")
    static class ValidatorTrueTest extends ContextTest {
        @Autowired
        private Validator validator;

        @Test
        void givenNotationUnset_whenSerialize_thenUsesJavalidationAdapter() {
            assertThat(validator).isExactlyInstanceOf(JakartaValidatorAdapter.class);
        }
    }
}
