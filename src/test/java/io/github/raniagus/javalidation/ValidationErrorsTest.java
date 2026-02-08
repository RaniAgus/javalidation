package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.raniagus.javalidation.format.TemplateString;
import org.junit.jupiter.api.Test;

class ValidationErrorsTest {

    // -- withPrefix(String) --

    @Test
    void givenRootErrors_whenWithPrefix_thenConvertsToFieldErrors() {
        var errors = ValidationErrors.of("root error");

        var prefixed = errors.withPrefix("user");

        assertThat(prefixed.rootErrors()).isEmpty();
        assertThat(prefixed.fieldErrors()).containsOnlyKeys("user");
        assertThat(prefixed.fieldErrors().get("user")).containsExactly(TemplateString.of("root error"));
    }

    @Test
    void givenFieldErrors_whenWithPrefix_thenPrefixesFieldNames() {
        var errors = ValidationErrors.of("email", "invalid");

        var prefixed = errors.withPrefix("form");

        assertThat(prefixed.fieldErrors()).containsOnlyKeys("form.email");
        assertThat(prefixed.fieldErrors().get("form.email")).containsExactly(TemplateString.of("invalid"));
    }

    // -- withPrefix(Object, Object...) --

    @Test
    void givenFieldErrors_whenWithPrefixVarargs_thenBuildsPrefix() {
        var errors = ValidationErrors.of("field", "error");

        var prefixed = errors.withPrefix("parent", ".", "child");

        assertThat(prefixed.fieldErrors()).containsOnlyKeys("parent.child.field");
    }
}
