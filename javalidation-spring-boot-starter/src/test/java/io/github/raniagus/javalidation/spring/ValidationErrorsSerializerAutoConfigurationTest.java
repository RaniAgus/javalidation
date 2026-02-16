package io.github.raniagus.javalidation.spring;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.TemplateString;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.json.JsonMapper;

class ValidationErrorsSerializerAutoConfigurationTest extends AutoConfigurationTest {
    static ValidationErrors ERRORS = new ValidationErrors(
            List.of(TemplateString.of("global error")),
            Map.of(
                    FieldKey.of("email"), List.of(TemplateString.of("invalid format"))
            )
    );

    @SpringBootTest(classes = TestApplication.class)
    static class FlattenErrorsUnsetTest {
        @Autowired
        private JsonMapper jsonMapper;

        @Test
        void givenFlattenErrorsUnset_whenSerialize_thenUsesStructuredFormat() {
            String json = jsonMapper.writeValueAsString(ERRORS);
            assertThat(json).isEqualTo("""
                    {"rootErrors":["global error"],"fieldErrors":{"email":["invalid format"]}}\
                    """);
        }
    }

    @SpringBootTest(classes = TestApplication.class)
    @TestPropertySource(properties = "io.github.raniagus.javalidation.flatten-errors=true")
    static class FlattenErrorsEnabledTest {
        @Autowired
        private JsonMapper jsonMapper;

        @Test
        void givenFlattenErrorsEnabled_whenSerialize_thenUsesFlattenedFormat() {
            String json = jsonMapper.writeValueAsString(ERRORS);
            AssertionsForClassTypes.assertThat(json).isEqualTo("""
                    {"":["global error"],"email":["invalid format"]}\
                    """);
        }
    }

    @TestPropertySource(properties = "io.github.raniagus.javalidation.flatten-errors=false")
    static class FlattenErrorsDisabledTest extends FlattenErrorsUnsetTest {
    }
}
