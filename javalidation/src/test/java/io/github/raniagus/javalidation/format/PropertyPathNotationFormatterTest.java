package io.github.raniagus.javalidation.format;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.raniagus.javalidation.FieldKey;
import org.junit.jupiter.api.Test;

class PropertyPathNotationFormatterTest {
    private final FieldKeyFormatter formatter = new PropertyPathNotationFormatter();

    @Test
    void givenSimpleString_whenFormat_thenReturnSameString() {
        FieldKey key = FieldKey.of("test");

        assertThat(formatter.format(key)).isEqualTo("test");
    }

    @Test
    void givenNumericValue_whenFormat_thenReturnBracketedRepresentation() {
        FieldKey key = FieldKey.of(123);

        assertThat(formatter.format(key)).isEqualTo("[123]");
    }

    @Test
    void givenNestedValue_whenFormat_thenReturnPropertyPathString() {
        FieldKey key = FieldKey.of("test", 42, "nested");

        assertThat(formatter.format(key)).isEqualTo("test[42].nested");
    }
}
