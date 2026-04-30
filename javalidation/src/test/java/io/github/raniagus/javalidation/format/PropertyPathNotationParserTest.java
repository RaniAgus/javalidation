package io.github.raniagus.javalidation.format;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.raniagus.javalidation.FieldKey;
import org.junit.jupiter.api.Test;

public class PropertyPathNotationParserTest {
    FieldKeyParser parser = new PropertyPathNotationParser();

    @Test
    void givenSimpleString_whenParse_thenReturnSingleStringKey() {
        assertThat(parser.parse("test")).isEqualTo(FieldKey.of("test"));
    }

    @Test
    void givenLeadingBracketedInt_whenParse_thenReturnSingleIntKey() {
        assertThat(parser.parse("[123]")).isEqualTo(FieldKey.of(123));
    }

    @Test
    void givenMixedPath_whenParse_thenReturnStringAndIntKeys() {
        assertThat(parser.parse("items[0].price")).isEqualTo(FieldKey.of("items", 0, "price"));
    }

    @Test
    void givenMultipleIndexes_whenParse_thenReturnAllIntKeys() {
        assertThat(parser.parse("matrix[0][1]")).isEqualTo(FieldKey.of("matrix", 0, 1));
    }

    @Test
    void givenRoundTrip_whenFormatThenParse_thenReturnOriginalKey() {
        FieldKey original = FieldKey.of("user", 3, "email");
        FieldKeyFormatter formatter = new PropertyPathNotationFormatter();

        assertThat(parser.parse(formatter.format(original))).isEqualTo(original);
    }

    @Test
    void givenUnclosedBracket_whenParse_thenThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> parser.parse("path[0.price"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenUnexpectedClosingBracket_whenParse_thenThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> parser.parse("path]0"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenMultiSegmentStringPath_whenParse_thenReturnStringKeys() {
        assertThat(parser.parse("user.address")).isEqualTo(FieldKey.of("user", "address"));
    }

    @Test
    void givenLeadingBracketedIntThenDotString_whenParse_thenReturnIntAndStringKey() {
        assertThat(parser.parse("[0].name")).isEqualTo(FieldKey.of(0, "name"));
    }

    @Test
    void givenTrailingDot_whenParse_thenThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> parser.parse("a."))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenNonIntegerInBrackets_whenParse_thenThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> parser.parse("items[foo]"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
