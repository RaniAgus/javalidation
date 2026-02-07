package io.github.raniagus.javalidation.spring;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.raniagus.javalidation.format.TemplateString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(properties = "javalidation.use-message-source=false")
class DefaultTemplateStringFormatterTest {
    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldUseMessageSource() {
        TemplateString ts = new TemplateString("Hello {0}{1}!", "Maroon", 5);
        String json = jsonMapper.writeValueAsString(ts);
        assertThat(json).contains("Hello Maroon5!");
    }
}
