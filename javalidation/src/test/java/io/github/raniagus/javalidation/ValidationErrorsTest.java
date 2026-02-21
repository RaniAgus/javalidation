package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ValidationErrorsTest {

    // -- withPrefix(String) --

    @Test
    void givenRootErrors_whenWithPrefix_thenConvertsToFieldErrors() {
        var errors = ValidationErrors.of("root error");

        var prefixed = errors.withPrefix("field");

        assertThat(prefixed.rootErrors()).isEmpty();
        assertThat(prefixed.fieldErrors()).containsOnlyKeys(FieldKey.of("field"));
        assertThat(prefixed.fieldErrors().get(FieldKey.of("field"))).containsExactly(TemplateString.of("root error"));
    }

    @Test
    void givenFieldErrors_whenWithPrefix_thenPrefixesFieldNames() {
        var errors = ValidationErrors.at("email", "invalid");

        var prefixed = errors.withPrefix("form");

        assertThat(prefixed.fieldErrors()).containsOnlyKeys(FieldKey.of("form", "email"));
        assertThat(prefixed.fieldErrors().get(FieldKey.of("form","email"))).containsExactly(TemplateString.of("invalid"));
    }

    // -- withPrefix(Object, Object...) --

    @Test
    void givenFieldErrors_whenWithPrefixVarargs_thenBuildsPrefix() {
        var errors = ValidationErrors.at("field", "error");

        var prefixed = errors.withPrefix("parent", "child");

        assertThat(prefixed.fieldErrors()).containsOnlyKeys(FieldKey.of("parent", "child", "field"));
    }
}
