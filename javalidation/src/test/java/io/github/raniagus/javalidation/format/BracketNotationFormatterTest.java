package io.github.raniagus.javalidation.format;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.raniagus.javalidation.FieldKey;
import org.junit.jupiter.api.Test;

public class BracketNotationFormatterTest {
    FieldKeyFormatter formatter = new BracketNotationFormatter();

    @Test
    void givenSimpleString_whenFormat_thenReturnSameString() {
        FieldKey key = FieldKey.of("test");

        String result = formatter.format(key);

        assertThat(result).isEqualTo("test");
    }

    @Test
    void givenNumericValue_whenFormat_thenReturnStringRepresentation() {
        FieldKey key = FieldKey.of(123);

        String result = formatter.format(key);

        assertThat(result).isEqualTo("[123]");
    }

    @Test
    void givenNestedValue_whenFormat_thenReturnDotSeparatedString() {
        FieldKey key = FieldKey.of("test", 42, "nested");

        String result = formatter.format(key);

        assertThat(result).isEqualTo("test[42][nested]");
    }
}
