package io.github.raniagus.javalidation.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.format.FieldKeyFormatter;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.json.JsonMapper;

class FieldKeyFormatterAutoConfigurationTest extends AutoConfigurationTest {
    @SpringBootTest(classes = TestApplication.class)
    static class ContextTest {
        @Autowired(required = false)
        private FieldKeyFormatter formatter;

        @Test
        void givenAutoConfiguration_whenStartup_thenConfiguresFormatter() {
            assertNotNull(formatter);
        }
    }

    static class KeyNotationUnsetTest extends ContextTest {
        @Autowired
        private JsonMapper jsonMapper;

        @Test
        void givenNotationUnset_whenSerialize_thenUsesDefaultNotation() {
            Map<FieldKey, String> map = Map.of(FieldKey.of("greetings", 1, "message"), "Hello Alice!");
            String json = jsonMapper.writeValueAsString(map);
            assertThat(json).isEqualTo("""
                    {"greetings[1].message":"Hello Alice!"}\
                    """);
        }
    }

    @TestPropertySource(properties = "io.github.raniagus.javalidation.key-notation=property_path")
    static class KeyNotationDefaultSetTest extends KeyNotationUnsetTest {
    }

    @TestPropertySource(properties = "io.github.raniagus.javalidation.key-notation=dots")
    static class KeyNotationDotSetTest extends ContextTest {
        @Autowired
        private JsonMapper jsonMapper;

        @Test
        void givenNotationSetAsDot_whenSerialize_thenUsesDotNotation() {
            Map<FieldKey, String> map = Map.of(FieldKey.of("greetings", 1, "message"), "Hello Alice!");
            String json = jsonMapper.writeValueAsString(map);
            assertThat(json).isEqualTo("""
                    {"greetings.1.message":"Hello Alice!"}\
                    """);
        }
    }

    @TestPropertySource(properties = "io.github.raniagus.javalidation.key-notation=brackets")
    static class KeyNotationBracketSetTest extends ContextTest {
        @Autowired
        private JsonMapper jsonMapper;

        @Test
        void givenNotationSetAsBracket_whenSerialize_thenUsesBracketNotation() {
            Map<FieldKey, String> map = Map.of(FieldKey.of("greetings", 1, "message"), "Hello Alice!");
            String json = jsonMapper.writeValueAsString(map);
            assertThat(json).isEqualTo("""
                    {"greetings[1][message]":"Hello Alice!"}\
                    """);
        }
    }
}
