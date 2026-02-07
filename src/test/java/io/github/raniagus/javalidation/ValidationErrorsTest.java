package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ValidationErrorsTest {

    @Test
    void withPrefix_string_convertRootErrorsToFieldErrors() {
        var errors = ValidationErrors.of("root error");

        var prefixed = errors.withPrefix("user");

        assertThat(prefixed.rootErrors()).isEmpty();
        assertThat(prefixed.fieldErrors()).containsOnlyKeys("user");
        assertThat(prefixed.fieldErrors().get("user")).containsExactly("root error");
    }

    @Test
    void withPrefix_string_prefixFieldErrors() {
        var errors = ValidationErrors.of("email", "invalid");

        var prefixed = errors.withPrefix("form");

        assertThat(prefixed.fieldErrors()).containsOnlyKeys("formemail");
        assertThat(prefixed.fieldErrors().get("formemail")).containsExactly("invalid");
    }

    @Test
    void withPrefix_string_preservesMultipleErrors() {
        var errors = new ValidationErrors(
                java.util.List.of(),
                java.util.Map.of("field", java.util.List.of("error1", "error2"))
        );

        var prefixed = errors.withPrefix("prefix");

        assertThat(prefixed.fieldErrors().get("prefixfield"))
                .containsExactly("error1", "error2");
    }

    @Test
    void withPrefix_varargs_buildsPrefix() {
        var errors = ValidationErrors.of("field", "error");

        var prefixed = errors.withPrefix("parent", ".", "child");

        assertThat(prefixed.fieldErrors()).containsOnlyKeys("parent.childfield");
    }

    @Test
    void withPrefix_varargs_acceptsMixedTypes() {
        var errors = ValidationErrors.of("field", "error");

        var prefixed = errors.withPrefix("user", "[", "0", "]");

        assertThat(prefixed.fieldErrors()).containsOnlyKeys("user[0]field");
    }

    @Test
    void withPrefix_returnsNewInstance() {
        var errors = ValidationErrors.of("field", "error");
        var prefixed = errors.withPrefix("prefix");

        assertThat(errors).isNotSameAs(prefixed);
    }
}
