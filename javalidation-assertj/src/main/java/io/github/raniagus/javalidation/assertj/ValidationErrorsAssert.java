package io.github.raniagus.javalidation.assertj;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.TemplateString;
import io.github.raniagus.javalidation.ValidationErrors;
import java.util.Arrays;
import java.util.List;
import org.assertj.core.api.AbstractAssert;

/**
 * AssertJ assertions for {@link ValidationErrors}.
 *
 * <p>Obtain an instance via {@link JavalidationAssertions#assertThat(ValidationErrors)} or
 * transitively via {@link ResultAssert#isErr()}.
 *
 * <p>Example:
 * <pre>{@code
 * assertThat(result).isErr()
 *     .hasFieldError("age", "must be at least {0}", 18)
 *     .hasNoRootErrors();
 * }</pre>
 */
public class ValidationErrorsAssert
        extends AbstractAssert<ValidationErrorsAssert, ValidationErrors> {

    ValidationErrorsAssert(ValidationErrors actual) {
        super(actual, ValidationErrorsAssert.class);
    }

    // -- isEmpty / isNotEmpty --

    /**
     * Asserts that there are no errors at all (neither root nor field).
     */
    public ValidationErrorsAssert isEmpty() {
        isNotNull();
        if (actual.isNotEmpty()) {
            failWithMessage("Expected ValidationErrors to be empty but found %d error(s):%n%s",
                    actual.count(), actual);
        }
        return this;
    }

    /**
     * Asserts that there is at least one error.
     */
    public ValidationErrorsAssert isNotEmpty() {
        isNotNull();
        if (actual.isEmpty()) {
            failWithMessage("Expected ValidationErrors to be non-empty but it was empty");
        }
        return this;
    }

    // -- error count --

    /**
     * Asserts the total number of errors (root + all field errors).
     */
    public ValidationErrorsAssert hasErrorCount(int expected) {
        isNotNull();
        int actual = this.actual.count();
        if (actual != expected) {
            failWithMessage("Expected %d total error(s) but found %d:%n%s",
                    expected, actual, this.actual);
        }
        return this;
    }

    /**
     * Asserts the number of root-level errors.
     */
    public ValidationErrorsAssert hasRootErrorCount(int expected) {
        isNotNull();
        int actual = this.actual.rootErrors().size();
        if (actual != expected) {
            failWithMessage("Expected %d root error(s) but found %d:%n%s",
                    expected, actual, this.actual.rootErrors());
        }
        return this;
    }

    /**
     * Asserts the total number of field errors across all field keys.
     */
    public ValidationErrorsAssert hasFieldErrorCount(int expected) {
        isNotNull();
        int actual = this.actual.fieldErrors().values().stream()
                .mapToInt(List::size)
                .sum();
        if (actual != expected) {
            failWithMessage("Expected %d field error(s) but found %d:%n%s",
                    expected, actual, this.actual.fieldErrors());
        }
        return this;
    }

    /**
     * Asserts the number of field errors at the given {@link FieldKey}.
     */
    public ValidationErrorsAssert hasFieldErrorCountAt(FieldKey key, int expected) {
        isNotNull();
        List<TemplateString> errors = actual.fieldErrors().get(key);
        if (errors == null) {
            failWithMessage(
                    "Expected field errors to contain key <%s> but found keys:%n%s",
                    key, actual.fieldErrors().keySet());
            return this;
        }
        if (errors.size() != expected) {
            failWithMessage("Expected %d error(s) at field <%s> but found %d:%n%s",
                    expected, key, errors.size(), errors);
        }
        return this;
    }

    // -- root errors --

    /**
     * Asserts that there are no root-level errors.
     */
    public ValidationErrorsAssert hasNoRootErrors() {
        isNotNull();
        if (!actual.rootErrors().isEmpty()) {
            failWithMessage("Expected no root errors but found %d:%n%s",
                    actual.rootErrors().size(), actual.rootErrors());
        }
        return this;
    }

    /**
     * Asserts that at least one root error matches the given message template and arguments.
     *
     * <p>The {@code message} is matched against {@link TemplateString#message()} and {@code args}
     * against {@link TemplateString#args()} element-wise.
     */
    public ValidationErrorsAssert hasRootError(String message, Object... args) {
        isNotNull();
        TemplateString expected = new TemplateString(message, args);
        List<TemplateString> rootErrors = actual.rootErrors();
        if (rootErrors.stream().noneMatch(expected::equals)) {
            failWithMessage(
                    "Expected root errors to contain TemplateString{message='%s', args=%s} but found:%n%s",
                    message, Arrays.toString(args), rootErrors);
        }
        return this;
    }

    // -- field errors --

    /**
     * Asserts that there are no field errors.
     */
    public ValidationErrorsAssert hasNoFieldErrors() {
        isNotNull();
        if (!actual.fieldErrors().isEmpty()) {
            failWithMessage("Expected no field errors but found %d key(s):%n%s",
                    actual.fieldErrors().size(), actual.fieldErrors().keySet());
        }
        return this;
    }

    /**
     * Asserts that a field error keyed by the single string {@code field} is present and matches
     * the given message template and arguments.
     */
    public ValidationErrorsAssert hasFieldError(String field, String message, Object... args) {
        return hasFieldErrorAt(FieldKey.of(field), message, args);
    }

    /**
     * Asserts that a field error keyed by the single index {@code index} is present and matches
     * the given message template and arguments.
     */
    public ValidationErrorsAssert hasFieldError(int index, String message, Object... args) {
        return hasFieldErrorAt(FieldKey.of(index), message, args);
    }

    /**
     * Asserts that a field error keyed by the given property-path string is present and that at
     * least one error in that list matches the given message template and arguments.
     *
     * <p>The path uses dot-notation for strings and bracket-notation for integers (e.g.
     * {@code "user.address"} or {@code "items[0].price"}).
     */
    public ValidationErrorsAssert hasFieldErrorAt(String path, String message, Object... args) {
        return hasFieldErrorAt(PropertyPathNotationParser.parse(path), message, args);
    }

    /**
     * Asserts that a field error keyed by the given {@link FieldKey} is present and that at least
     * one error in that list matches the given message template and arguments.
     *
     * <p>Use this overload for composite keys (e.g. {@code FieldKey.of("user", "age")}).
     */
    public ValidationErrorsAssert hasFieldErrorAt(FieldKey key, String message, Object... args) {
        isNotNull();
        List<TemplateString> errors = actual.fieldErrors().get(key);
        if (errors == null) {
            failWithMessage(
                    "Expected field errors to contain key <%s> but found keys:%n%s",
                    key, actual.fieldErrors().keySet());
            return this;
        }
        TemplateString expected = new TemplateString(message, args);
        if (errors.stream().noneMatch(expected::equals)) {
            failWithMessage(
                    "Expected field errors at key <%s> to contain TemplateString{message='%s', args=%s} but found:%n%s",
                    key, message, Arrays.toString(args), errors);
        }
        return this;
    }

    /**
     * Asserts that the given field path is present in the field errors, without inspecting the error
     * messages.
     */
    public ValidationErrorsAssert hasFieldKey(Object... path) {
        isNotNull();
        FieldKey key = FieldKey.of(path);
        if (!actual.fieldErrors().containsKey(key)) {
            failWithMessage("Expected field errors to contain key <%s> but found keys:%n%s",
                    key, actual.fieldErrors().keySet());
        }
        return this;
    }

    /**
     * Asserts that the given field path is <em>not</em> present in the field errors.
     */
    public ValidationErrorsAssert doesNotHaveFieldKey(Object... path) {
        isNotNull();
        FieldKey key = FieldKey.of(path);
        if (actual.fieldErrors().containsKey(key)) {
            failWithMessage("Expected field errors NOT to contain key <%s> but it was present", key);
        }
        return this;
    }
}
