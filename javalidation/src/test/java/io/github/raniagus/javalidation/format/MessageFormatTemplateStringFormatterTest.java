package io.github.raniagus.javalidation.format;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.raniagus.javalidation.TemplateString;
import org.junit.jupiter.api.Test;

class MessageFormatTemplateStringFormatterTest {
    private final MessageFormatTemplateStringFormatter formatter = new MessageFormatTemplateStringFormatter();

    // -- format --

    @Test
    void givenNoArguments_whenFormat_thenReturnsMessage() {
        TemplateString template = TemplateString.of("Simple message");

        String result = formatter.format(template);

        assertThat(result).isEqualTo("Simple message");
    }

    @Test
    void givenSingleArgument_whenFormat_thenSubstitutesArgument() {
        TemplateString template = TemplateString.of("Hello {0}", "World");

        String result = formatter.format(template);

        assertThat(result).isEqualTo("Hello World");
    }

    @Test
    void givenMultipleArguments_whenFormat_thenSubstitutesAll() {
        TemplateString template = TemplateString.of("Hello {0}, you have {1} messages", "Alice", 5);

        String result = formatter.format(template);

        assertThat(result).isEqualTo("Hello Alice, you have 5 messages");
    }

    @Test
    void givenNumberFormatPattern_whenFormat_thenFormatsNumber() {
        TemplateString template = TemplateString.of("Price: {0,number,currency}", 42.50);

        String result = formatter.format(template);

        assertThat(result).startsWith("Price: ");
        assertThat(result).contains("42");
    }

    @Test
    void givenDateFormatPattern_whenFormat_thenFormatsDate() {
        TemplateString template = TemplateString.of("Date: {0,date,short}", new java.util.Date(0));

        String result = formatter.format(template);

        assertThat(result).startsWith("Date: ");
    }
}
