package io.github.raniagus.javalidation.assertj;

import static io.github.raniagus.javalidation.assertj.JavalidationAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.INTEGER;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.PartialResult;
import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JavalidationAssertionsTest {

    // -------------------------------------------------------------------------
    // ResultAssert
    // -------------------------------------------------------------------------

    @Nested
    class ResultAssertTest {

        @Test
        void givenOkResult_whenIsOk_thenSucceeds() {
            var result = Result.ok(42);

            assertThat(result).isOk().isEqualTo(42);
        }

        @Test
        void givenOkResult_whenIsOkHasValue_thenSucceeds() {
            var result = Result.ok("hello");

            assertThat(result).isOk().hasValue("hello");
        }

        @Test
        void givenErrResult_whenIsOk_thenFails() {
            var result = Result.<Integer>error("bad input");

            assertThatThrownBy(() -> assertThat(result).isOk())
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("Ok");
        }

        @Test
        void givenErrResult_whenIsErr_thenReturnsValidationErrorsAssert() {
            var result = Result.<Integer>error("bad input");

            assertThat(result).isErr().hasRootError("bad input");
        }

        @Test
        void givenOkResult_whenIsErr_thenFails() {
            var result = Result.ok(42);

            assertThatThrownBy(() -> assertThat(result).isErr())
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("Err");
        }

        @Test
        void givenErrWithFieldError_whenIsErrChained_thenAssertsProperly() {
            var result = Result.<String>errorAt("email", "invalid format");

            assertThat(result).isErr()
                    .hasFieldError("email", "invalid format")
                    .hasNoRootErrors();
        }

        @Test
        void givenOkResult_whenIsOkHasValue_withWrongValue_thenFails() {
            var result = Result.ok("hello");

            assertThatThrownBy(() -> assertThat(result).isOk().hasValue("world"))
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void givenErrResult_whenIsOk_failureMessageMentionsErrors() {
            var result = Result.<Integer>error("some error");

            assertThatThrownBy(() -> assertThat(result).isOk())
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("some error");
        }

        @Test
        void givenOkResult_whenIsErr_failureMessageMentionsValue() {
            var result = Result.ok(99);

            assertThatThrownBy(() -> assertThat(result).isErr())
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("99");
        }

        @Test
        void givenOkStringResult_whenGet_thenReturnsObjectAssertOnValue() {
            var result = Result.ok("hello world");

            assertThat(result).isOk().get().asString().startsWith("hello");
        }

        @Test
        void givenOkIntResult_whenGetWithFactory_thenReturnsNarrowedAssert() {
            var result = Result.ok(42);

            assertThat(result).isOk().get(INTEGER).isGreaterThan(10);
        }

        @Test
        void givenOkStringResult_whenGetWithStringFactory_thenReturnsStringAssert() {
            var result = Result.ok("hello");

            assertThat(result).isOk().get(STRING).startsWith("hel").endsWith("lo");
        }

        @Test
        void givenOkListResult_whenGetWithListFactory_thenReturnsListAssert() {
            var result = Result.ok(java.util.List.of("a", "b", "c"));

            assertThat(result).isOk().get(LIST).hasSize(3).contains("b");
        }
    }

    // -------------------------------------------------------------------------
    // ValidationErrorsAssert
    // -------------------------------------------------------------------------

    @Nested
    class ValidationErrorsAssertTest {

        @Test
        void givenEmpty_whenIsEmpty_thenSucceeds() {
            assertThat(ValidationErrors.empty()).isEmpty();
        }

        @Test
        void givenErrors_whenIsEmpty_thenFails() {
            var errors = ValidationErrors.of("some error");

            assertThatThrownBy(() -> assertThat(errors).isEmpty())
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void givenErrors_whenIsNotEmpty_thenSucceeds() {
            assertThat(ValidationErrors.of("some error")).isNotEmpty();
        }

        @Test
        void givenEmpty_whenIsNotEmpty_thenFails() {
            assertThatThrownBy(() -> assertThat(ValidationErrors.empty()).isNotEmpty())
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void givenTwoErrors_whenHasErrorCount_thenSucceeds() {
            var errors = ValidationErrors.of("root")
                    .mergeWith(ValidationErrors.at("field", "bad"));

            assertThat(errors).hasErrorCount(2);
        }

        @Test
        void givenRootError_whenHasRootError_thenSucceeds() {
            var errors = ValidationErrors.of("must not be null");

            assertThat(errors).hasRootError("must not be null");
        }

        @Test
        void givenRootErrorWithArgs_whenHasRootError_thenSucceeds() {
            var errors = ValidationErrors.of("must be at least {0}", 18);

            assertThat(errors).hasRootError("must be at least {0}", 18);
        }

        @Test
        void givenNoMatchingRootError_whenHasRootError_thenFails() {
            var errors = ValidationErrors.of("other error");

            assertThatThrownBy(() -> assertThat(errors).hasRootError("expected error"))
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void givenRootError_whenHasNoRootErrors_thenFails() {
            var errors = ValidationErrors.of("some error");

            assertThatThrownBy(() -> assertThat(errors).hasNoRootErrors())
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void givenFieldError_whenHasNoRootErrors_thenSucceeds() {
            var errors = ValidationErrors.at("field", "error");

            assertThat(errors).hasNoRootErrors();
        }

        @Test
        void givenRootErrorCount_whenHasRootErrorCount_thenSucceeds() {
            var errors = ValidationErrors.of("e1").mergeWith(ValidationErrors.of("e2"));

            assertThat(errors).hasRootErrorCount(2);
        }

        @Test
        void givenFieldError_whenHasFieldError_thenSucceeds() {
            var errors = ValidationErrors.at("email", "invalid format");

            assertThat(errors).hasFieldError("email", "invalid format");
        }

        @Test
        void givenIndexedFieldError_whenHasFieldErrorInt_thenSucceeds() {
            var errors = ValidationErrors.at(0, "must not be null");

            assertThat(errors).hasFieldError(0, "must not be null");
        }

        @Test
        void givenNestedFieldError_whenHasFieldErrorAt_thenSucceeds() {
            var errors = ValidationErrors.at("age", "too young")
                    .withPrefix("user");

            assertThat(errors).hasFieldErrorAt(FieldKey.of("user", "age"), "too young");
        }

        @Test
        void givenNestedFieldError_whenHasFieldErrorAtStringPath_thenSucceeds() {
            var errors = ValidationErrors.at("age", "too young")
                    .withPrefix("user");

            assertThat(errors).hasFieldErrorAt("user.age", "too young");
        }

        @Test
        void givenIndexedNestedFieldError_whenHasFieldErrorAtStringPath_thenSucceeds() {
            var errors = ValidationErrors.at("price", "must be positive")
                    .withPrefix(0)
                    .withPrefix("items");

            assertThat(errors).hasFieldErrorAt("items[0].price", "must be positive");
        }

        @Test
        void givenWrongMessage_whenHasFieldErrorAtStringPath_thenFails() {
            var errors = ValidationErrors.at("age", "too young")
                    .withPrefix("user");

            assertThatThrownBy(() -> assertThat(errors).hasFieldErrorAt("user.age", "too old"))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("too old");
        }

        @Test
        void givenMissingKey_whenHasFieldErrorAtStringPath_thenFails() {
            var errors = ValidationErrors.at("age", "too young")
                    .withPrefix("user");

            assertThatThrownBy(() -> assertThat(errors).hasFieldErrorAt("user.name", "too young"))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining(FieldKey.of("user", "name").toString());
        }

        @Test
        void givenMissingFieldKey_whenHasFieldError_thenFails() {
            var errors = ValidationErrors.at("name", "required");

            assertThatThrownBy(() -> assertThat(errors).hasFieldError("email", "required"))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("email");
        }

        @Test
        void givenFieldKey_whenHasFieldKey_thenSucceeds() {
            var errors = ValidationErrors.at("name", "required");

            assertThat(errors).hasFieldKey("name");
        }

        @Test
        void givenMissingKey_whenHasFieldKey_thenFails() {
            var errors = ValidationErrors.at("name", "required");

            assertThatThrownBy(() -> assertThat(errors).hasFieldKey("email"))
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void givenNoFieldForKey_whenDoesNotHaveFieldKey_thenSucceeds() {
            var errors = ValidationErrors.of("root error");

            assertThat(errors).doesNotHaveFieldKey("email");
        }

        @Test
        void givenExistingKey_whenDoesNotHaveFieldKey_thenFails() {
            var errors = ValidationErrors.at("email", "invalid");

            assertThatThrownBy(() -> assertThat(errors).doesNotHaveFieldKey("email"))
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void givenNoFieldErrors_whenHasNoFieldErrors_thenSucceeds() {
            var errors = ValidationErrors.of("root error");

            assertThat(errors).hasNoFieldErrors();
        }

        @Test
        void givenFieldErrors_whenHasNoFieldErrors_thenFails() {
            var errors = ValidationErrors.at("field", "error");

            assertThatThrownBy(() -> assertThat(errors).hasNoFieldErrors())
                    .isInstanceOf(AssertionError.class);
        }

        // -- hasErrorCount failure --

        @Test
        void givenTwoErrors_whenHasErrorCountWrong_thenFails() {
            var errors = ValidationErrors.of("root")
                    .mergeWith(ValidationErrors.at("field", "bad"));

            assertThatThrownBy(() -> assertThat(errors).hasErrorCount(5))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("5")
                    .hasMessageContaining("2");
        }

        // -- hasFieldErrorCount --

        @Test
        void givenTwoFieldErrors_whenHasFieldErrorCount_thenSucceeds() {
            var errors = ValidationErrors.at("name", "required")
                    .mergeWith(ValidationErrors.at("email", "invalid"));

            assertThat(errors).hasFieldErrorCount(2);
        }

        @Test
        void givenFieldErrorsWithMultipleOnSameKey_whenHasFieldErrorCount_thenCountsAll() {
            var errors = ValidationErrors.at("email", "too short")
                    .mergeWith(ValidationErrors.at("email", "invalid format"));

            assertThat(errors).hasFieldErrorCount(2);
        }

        @Test
        void givenFieldErrors_whenHasFieldErrorCountWrong_thenFails() {
            var errors = ValidationErrors.at("name", "required")
                    .mergeWith(ValidationErrors.at("email", "invalid"));

            assertThatThrownBy(() -> assertThat(errors).hasFieldErrorCount(5))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("5")
                    .hasMessageContaining("2");
        }

        // -- hasFieldErrorCountAt --

        @Test
        void givenFieldErrorsAtKey_whenHasFieldErrorCountAt_thenSucceeds() {
            var errors = ValidationErrors.at("email", "too short")
                    .mergeWith(ValidationErrors.at("email", "invalid format"));

            assertThat(errors).hasFieldErrorCountAt(FieldKey.of("email"), 2);
        }

        @Test
        void givenFieldErrorsAtKey_whenHasFieldErrorCountAtWrong_thenFails() {
            var errors = ValidationErrors.at("email", "invalid");

            assertThatThrownBy(() -> assertThat(errors).hasFieldErrorCountAt(FieldKey.of("email"), 3))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("3")
                    .hasMessageContaining("1");
        }

        @Test
        void givenMissingKey_whenHasFieldErrorCountAt_thenFails() {
            var errors = ValidationErrors.at("name", "required");

            assertThatThrownBy(() -> assertThat(errors).hasFieldErrorCountAt(FieldKey.of("email"), 1))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("email");
        }

        // -- hasRootErrorCount failure --

        @Test
        void givenOneRootError_whenHasRootErrorCountWrong_thenFails() {
            var errors = ValidationErrors.of("e1");

            assertThatThrownBy(() -> assertThat(errors).hasRootErrorCount(3))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("3")
                    .hasMessageContaining("1");
        }

        // -- hasRootError with wrong args --

        @Test
        void givenRootErrorWithArgs_whenHasRootErrorWrongArgs_thenFails() {
            var errors = ValidationErrors.of("must be at least {0}", 18);

            assertThatThrownBy(() -> assertThat(errors).hasRootError("must be at least {0}", 21))
                    .isInstanceOf(AssertionError.class);
        }

        // -- hasRootError matches any, not only first --

        @Test
        void givenMultipleRootErrors_whenHasRootErrorMatchesSecond_thenSucceeds() {
            var errors = ValidationErrors.of("first").mergeWith(ValidationErrors.of("second"));

            assertThat(errors).hasRootError("second");
        }

        // -- hasFieldError(String) wrong message --

        @Test
        void givenFieldError_whenHasFieldErrorWrongMessage_thenFails() {
            var errors = ValidationErrors.at("email", "invalid format");

            assertThatThrownBy(() -> assertThat(errors).hasFieldError("email", "other message"))
                    .isInstanceOf(AssertionError.class);
        }

        // -- hasFieldError(String) with args --

        @Test
        void givenFieldErrorWithArgs_whenHasFieldErrorWithArgs_thenSucceeds() {
            var errors = ValidationErrors.at("age", "must be at least {0}", 18);

            assertThat(errors).hasFieldError("age", "must be at least {0}", 18);
        }

        @Test
        void givenFieldErrorWithArgs_whenHasFieldErrorWrongArgs_thenFails() {
            var errors = ValidationErrors.at("age", "must be at least {0}", 18);

            assertThatThrownBy(() -> assertThat(errors).hasFieldError("age", "must be at least {0}", 21))
                    .isInstanceOf(AssertionError.class);
        }

        // -- hasFieldError(int) --

        @Test
        void givenIndexedFieldError_whenHasFieldErrorWrongIndex_thenFails() {
            var errors = ValidationErrors.at(0, "must not be null");

            assertThatThrownBy(() -> assertThat(errors).hasFieldError(1, "must not be null"))
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void givenIndexedFieldError_whenHasFieldErrorWrongMessage_thenFails() {
            var errors = ValidationErrors.at(0, "must not be null");

            assertThatThrownBy(() -> assertThat(errors).hasFieldError(0, "other message"))
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void givenIndexedFieldErrorWithArgs_whenHasFieldErrorWithArgs_thenSucceeds() {
            var errors = ValidationErrors.at(2, "size must be {0}", 10);

            assertThat(errors).hasFieldError(2, "size must be {0}", 10);
        }

        @Test
        void givenIndexedFieldErrorWithArgs_whenHasFieldErrorWrongArgs_thenFails() {
            var errors = ValidationErrors.at(2, "size must be {0}", 10);

            assertThatThrownBy(() -> assertThat(errors).hasFieldError(2, "size must be {0}", 99))
                    .isInstanceOf(AssertionError.class);
        }

        // -- hasFieldErrorAt --

        @Test
        void givenNestedFieldError_whenHasFieldErrorAtMissingKey_thenFails() {
            var errors = ValidationErrors.at("age", "too young").withPrefix("user");

            assertThatThrownBy(() -> assertThat(errors).hasFieldErrorAt(FieldKey.of("user", "name"), "too young"))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("user");
        }

        @Test
        void givenNestedFieldError_whenHasFieldErrorAtWrongMessage_thenFails() {
            var errors = ValidationErrors.at("age", "too young").withPrefix("user");

            assertThatThrownBy(() -> assertThat(errors).hasFieldErrorAt(FieldKey.of("user", "age"), "other message"))
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void givenNestedFieldErrorWithArgs_whenHasFieldErrorAtWithArgs_thenSucceeds() {
            var errors = ValidationErrors.at("age", "must be at least {0}", 18).withPrefix("user");

            assertThat(errors).hasFieldErrorAt(FieldKey.of("user", "age"), "must be at least {0}", 18);
        }

        @Test
        void givenNestedFieldErrorWithArgs_whenHasFieldErrorAtWrongArgs_thenFails() {
            var errors = ValidationErrors.at("age", "must be at least {0}", 18).withPrefix("user");

            assertThatThrownBy(() -> assertThat(errors).hasFieldErrorAt(FieldKey.of("user", "age"), "must be at least {0}", 21))
                    .isInstanceOf(AssertionError.class);
        }

        // -- hasFieldKey(int) --

        @Test
        void givenIndexedFieldError_whenHasFieldKeyInt_thenSucceeds() {
            var errors = ValidationErrors.at(3, "error");

            assertThat(errors).hasFieldKey(3);
        }

        @Test
        void givenIndexedFieldError_whenHasFieldKeyWrongInt_thenFails() {
            var errors = ValidationErrors.at(3, "error");

            assertThatThrownBy(() -> assertThat(errors).hasFieldKey(5))
                    .isInstanceOf(AssertionError.class);
        }

        // -- doesNotHaveFieldKey(int) --

        @Test
        void givenNoIndexedFieldError_whenDoesNotHaveFieldKeyInt_thenSucceeds() {
            var errors = ValidationErrors.of("root error");

            assertThat(errors).doesNotHaveFieldKey(0);
        }

        @Test
        void givenIndexedFieldError_whenDoesNotHaveFieldKeyInt_thenFails() {
            var errors = ValidationErrors.at(0, "error");

            assertThatThrownBy(() -> assertThat(errors).doesNotHaveFieldKey(0))
                    .isInstanceOf(AssertionError.class);
        }

        // -- null subject guard --

        @Test
        void givenNullSubject_whenIsEmpty_thenFails() {
            assertThatThrownBy(() -> assertThat((ValidationErrors) null).isEmpty())
                    .isInstanceOf(AssertionError.class);
        }

        // -- chaining --

        @Test
        void givenMultipleFieldErrors_whenChainedHasFieldError_thenSucceeds() {
            var errors = ValidationErrors.at("name", "required")
                    .mergeWith(ValidationErrors.at("email", "invalid"));

            assertThat(errors)
                    .hasFieldError("name", "required")
                    .hasFieldError("email", "invalid");
        }
    }

    // -------------------------------------------------------------------------
    // assertThat(Validation)
    // -------------------------------------------------------------------------

    @Nested
    class ValidationAssertTest {

        @Test
        void givenValidationWithRootError_whenAssertThat_thenReturnsValidationErrorsAssert() {
            var validation = Validation.create().addError("must not be null");

            assertThat(validation).hasRootError("must not be null");
        }

        @Test
        void givenValidationWithFieldError_whenAssertThat_thenCanAssertFieldErrors() {
            var validation = Validation.create().addErrorAt("email", "invalid format");

            assertThat(validation).hasFieldError("email", "invalid format").hasNoRootErrors();
        }

        @Test
        void givenValidationWithArgs_whenAssertThat_thenMatchesArgs() {
            var validation = Validation.create().addErrorAt("age", "must be at least {0}", 18);

            assertThat(validation).hasFieldError("age", "must be at least {0}", 18);
        }

        @Test
        void givenEmptyValidation_whenAssertThat_thenIsEmpty() {
            assertThat(Validation.create()).isEmpty();
        }

        @Test
        void givenValidationWithErrors_whenAssertThat_thenIsNotEmpty() {
            var validation = Validation.create().addError("error");

            assertThat(validation).isNotEmpty();
        }

        @Test
        void givenValidationWithMultipleErrors_whenAssertThat_thenHasErrorCount() {
            var validation = Validation.create()
                    .addError("root error")
                    .addErrorAt("field", "field error");

            assertThat(validation).hasErrorCount(2);
        }
    }

    // -------------------------------------------------------------------------
    // PartialResultAssert
    // -------------------------------------------------------------------------

    @Nested
    class PartialResultAssertTest {

        @Test
        void givenPartialWithErrors_whenHasErrors_thenReturnsValidationErrorsAssert() {
            var partial = new PartialResult<>("value", ValidationErrors.of("root error"));

            assertThat(partial).hasErrors().hasRootError("root error");
        }

        @Test
        void givenPartialWithNoErrors_whenHasErrors_thenFails() {
            var partial = new PartialResult<>("value", ValidationErrors.empty());

            assertThatThrownBy(() -> assertThat(partial).hasErrors())
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void givenPartialWithNoErrors_whenHasNoErrors_thenSucceeds() {
            var partial = new PartialResult<>("value", ValidationErrors.empty());

            assertThat(partial).hasNoErrors();
        }

        @Test
        void givenPartialWithErrors_whenHasNoErrors_thenFails() {
            var partial = new PartialResult<>("value", ValidationErrors.of("error"));

            assertThatThrownBy(() -> assertThat(partial).hasNoErrors())
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("1");
        }

        @Test
        void givenPartialWithErrors_whenErrors_thenReturnsValidationErrorsAssertUnconditionally() {
            var partial = new PartialResult<>(42, ValidationErrors.at("field", "bad"));

            assertThat(partial).errors()
                    .hasFieldError("field", "bad")
                    .hasNoRootErrors();
        }

        @Test
        void givenPartialWithNoErrors_whenErrors_thenReturnsEmptyValidationErrorsAssert() {
            var partial = new PartialResult<>("ok", ValidationErrors.empty());

            assertThat(partial).errors().isEmpty();
        }

        @Test
        void givenPartialWithSuccessValue_whenSuccess_thenReturnsObjectAssert() {
            var partial = new PartialResult<>("hello", ValidationErrors.empty());

            assertThat(partial).success().isEqualTo("hello");
        }

        @Test
        void givenPartialWithSuccessValueAndErrors_whenSuccess_thenSuccessIsStillAccessible() {
            var partial = new PartialResult<>(42, ValidationErrors.of("error"));

            assertThat(partial).success().isEqualTo(42);
        }

        @Test
        void givenPartialWithStringValue_whenSuccessWithFactory_thenNarrowsToStringAssert() {
            var partial = new PartialResult<>("hello world", ValidationErrors.empty());

            assertThat(partial).success(STRING).startsWith("hello");
        }

        @Test
        void givenPartialWithListValue_whenSuccessWithFactory_thenNarrowsToListAssert() {
            var partial = new PartialResult<>(java.util.List.of("a", "b"), ValidationErrors.empty());

            assertThat(partial).success(LIST).hasSize(2).contains("a");
        }

        @Test
        void givenPartialWithNoErrors_whenToResult_thenResultIsOk() {
            var partial = new PartialResult<>("success", ValidationErrors.empty());

            assertThat(partial).toResult().isOk().hasValue("success");
        }

        @Test
        void givenPartialWithErrors_whenToResult_thenResultIsErr() {
            var partial = new PartialResult<>("partial", ValidationErrors.of("failed"));

            assertThat(partial).toResult().isErr().hasRootError("failed");
        }

        @Test
        void givenPartialWithFieldErrors_whenHasErrors_thenChainedFieldAssertionsWork() {
            var partial = new PartialResult<>(
                    "partial",
                    ValidationErrors.at("name", "required").mergeWith(ValidationErrors.at("email", "invalid")));

            assertThat(partial).hasErrors()
                    .hasFieldError("name", "required")
                    .hasFieldError("email", "invalid")
                    .hasNoRootErrors();
        }

        @Test
        void givenNullSubject_whenHasErrors_thenFails() {
            assertThatThrownBy(() -> assertThat((PartialResult<?>) null).hasErrors())
                    .isInstanceOf(AssertionError.class);
        }
    }
}
