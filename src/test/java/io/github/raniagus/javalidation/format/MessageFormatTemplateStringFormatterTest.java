package io.github.raniagus.javalidation.format;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MessageFormatTemplateStringFormatterTest {
    private final MessageFormatTemplateStringFormatter formatter = new MessageFormatTemplateStringFormatter();

    @Test
    void shouldFormatSimpleMessage() {
        TemplateString template = TemplateString.of("Hello {0}", "World");

        String result = formatter.format(template);

        assertThat(result).isEqualTo("Hello World");
    }

    @Test
    void shouldFormatMultipleArguments() {
        TemplateString template = TemplateString.of("Hello {0}, you have {1} messages", "Alice", 5);

        String result = formatter.format(template);

        assertThat(result).isEqualTo("Hello Alice, you have 5 messages");
    }

    @Test
    void shouldFormatWithNoArguments() {
        TemplateString template = TemplateString.of("Simple message");

        String result = formatter.format(template);

        assertThat(result).isEqualTo("Simple message");
    }

    @Test
    void shouldFormatWithNumberFormatting() {
        TemplateString template = TemplateString.of("Price: {0,number,currency}", 42.50);

        String result = formatter.format(template);

        assertThat(result).startsWith("Price: ");
        assertThat(result).contains("42");
    }

    @Test
    void shouldFormatWithDateFormatting() {
        TemplateString template = TemplateString.of("Date: {0,date,short}", new java.util.Date(0));

        String result = formatter.format(template);

        assertThat(result).startsWith("Date: ");
    }

    @Test
    void shouldFormatWithMixedTypes() {
        TemplateString template = TemplateString.of(
                "User {0} has {1} points and balance {2,number,currency}",
                "Bob",
                150,
                99.99
        );

        String result = formatter.format(template);

        assertThat(result)
                .contains("User Bob")
                .contains("150 points")
                .contains("99");
    }

    @Test
    void shouldHandleSpecialCharacters() {
        TemplateString template = TemplateString.of("Message: {0}", "It's a test!");

        String result = formatter.format(template);

        assertThat(result).isEqualTo("Message: It's a test!");
    }

    @Test
    void shouldFormatEmptyString() {
        TemplateString template = TemplateString.of("{0}", "");

        String result = formatter.format(template);

        assertThat(result).isEmpty();
    }
}