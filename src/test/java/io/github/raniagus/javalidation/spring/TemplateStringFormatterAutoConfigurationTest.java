package io.github.raniagus.javalidation.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.raniagus.javalidation.format.TemplateString;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.json.JsonMapper;

class TemplateStringFormatterAutoConfigurationTest extends AutoConfigurationTest {
    @SpringBootTest(classes = TestApplication.class)
    static class ContextTest {
        @Autowired(required = false)
        private TemplateStringFormatter formatter;

        @Test
        void givenAutoConfiguration_whenStartup_thenConfiguresFormatter() {
            assertNotNull(formatter);
        }
    }

    static class UseMessageSourceUnsetTest extends ContextTest {
        @Autowired
        private JsonMapper jsonMapper;

        @Test
        void givenMessageSourceUnset_whenSerialize_thenUsesMessageSource() {
            TemplateString ts = TemplateString.of("greeting.message", "Alice", 5);
            String json = jsonMapper.writeValueAsString(ts);
            assertThat(json).isEqualTo("\"Hello Alice!\"");
        }

        @Test
        void givenMessageSourceUnset_whenSerializeUnknownKey_thenFallsBackToDefaultFormatter() {
            TemplateString ts = TemplateString.of("Hello {0}! {1}", "World", 42);
            String json = jsonMapper.writeValueAsString(ts);
            assertThat(json).isEqualTo("\"Hello World! 42\"");
        }
    }

    @TestPropertySource(properties = "io.github.raniagus.javalidation.use-message-source=true")
    static class UseMessageSourceEnabledTest extends UseMessageSourceUnsetTest {
    }

    @TestPropertySource(properties = "io.github.raniagus.javalidation.use-message-source=false")
    static class UseMessageSourceDisabledTest extends ContextTest {
        @Autowired
        private JsonMapper jsonMapper;

        @Test
        void givenMessageSourceDisabled_whenSerialize_thenUsesDefaultFormatter() {
            TemplateString ts = TemplateString.of("greeting.message", "Maroon", 5);
            String json = jsonMapper.writeValueAsString(ts);
            assertThat(json).isEqualTo("\"greeting.message\"");
        }

        @Test
        void givenMessageSourceDisabled_whenSerializeWithPlaceholders_thenFormatsWithMessageFormat() {
            TemplateString ts = TemplateString.of("Hello {0}! {1}", "World", 42);
            String json = jsonMapper.writeValueAsString(ts);
            assertThat(json).isEqualTo("\"Hello World! 42\"");
        }
    }
}
