package io.github.raniagus.javalidation.spring;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.raniagus.javalidation.format.TemplateString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(properties = "io.github.raniagus.javalidation.use-message-source=true")
class MessageSourceTemplateStringFormatterTest {
    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldUseMessageSource() {
        TemplateString ts = new TemplateString("greeting.message", "Alice", 5);
        String json = jsonMapper.writeValueAsString(ts);
        assertThat(json).contains("Hello Alice!");
    }
}
