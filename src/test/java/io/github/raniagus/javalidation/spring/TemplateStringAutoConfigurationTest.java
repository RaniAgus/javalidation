package io.github.raniagus.javalidation.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.raniagus.javalidation.format.TemplateString;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(classes = TestApplication.class)
class TemplateStringAutoConfigurationTest {
    @Autowired
    private JsonMapper jsonMapper;

    @Autowired(required = false)
    private TemplateStringFormatter formatter;

    @Test
    void shouldAutoConfigureModule() {
        assertNotNull(formatter);
    }

    @Test
    void shouldSerializeWithAutoConfiguration() {
        TemplateString ts = new TemplateString("Hello {0}", "World");
        String json = jsonMapper.writeValueAsString(ts);
        assertEquals("\"Hello World\"", json);
    }
}