package io.github.raniagus.javalidation.spring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.validation.Validator;

class JakartaValidationAutoConfigurationTest extends AutoConfigurationTest {
    @SpringBootTest(classes = TestApplication.class)
    static class ValidatorUnsetTest {
        @Autowired(required = false)
        private Validator validator;

        @Test
        void givenAutoConfiguration_whenStartup_thenConfiguresOtherValidator() {
            assertThat(validator).isNotExactlyInstanceOf(JavalidationSpringValidator.class);
        }
    }

    @TestPropertySource(properties = "io.github.raniagus.javalidation.use-static-validators=false")
    static class ValidatorFalseTest extends ValidatorUnsetTest {
    }

    @SpringBootTest(classes = TestApplication.class)
    @TestPropertySource(properties = "io.github.raniagus.javalidation.use-static-validators=true")
    static class ValidatorTrueTest {
        @Autowired(required = false)
        private Validator validator;

        @Test
        void givenAutoConfiguration_whenStartup_thenConfiguresJavalidationAdapter() {
            assertThat(validator).isNotNull()
                    .isExactlyInstanceOf(JavalidationSpringValidator.class);
        }
    }
}
