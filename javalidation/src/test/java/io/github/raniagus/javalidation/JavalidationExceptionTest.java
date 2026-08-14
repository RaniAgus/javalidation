package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JavalidationExceptionTest {

    @Nested
    class GetMessageTests {

        @Test
        void givenSingleFieldError_whenGetMessage_thenReturnsErrorCount() {
            var exception = JavalidationException.at("email", "Invalid email format");

            assertThat(exception.getMessage()).isEqualTo("Validation failed with 1 error(s)");
        }

        @Test
        void givenSingleIndexError_whenGetMessage_thenReturnsErrorCount() {
            var exception = JavalidationException.at(0, "Must not be empty");

            assertThat(exception.getMessage()).isEqualTo("Validation failed with 1 error(s)");
        }

        @Test
        void givenSingleRootError_whenGetMessage_thenReturnsErrorCount() {
            var exception = JavalidationException.of("Something went wrong");

            assertThat(exception.getMessage()).isEqualTo("Validation failed with 1 error(s)");
        }

        @Test
        void givenMultipleErrors_whenGetMessage_thenReturnsTotalCount() {
            var validation = Validation.create();
            validation.addError("Invalid request");
            validation.addErrorAt("name", "Name is required");
            validation.addErrorAt("age", "Must be at least 18");
            validation.addErrorAt("age", "Cannot be negative");

            var exception = JavalidationException.of(validation.finish());

            assertThat(exception.getMessage()).isEqualTo("Validation failed with 4 error(s)");
        }

        @Test
        void givenEmptyErrors_whenGetMessage_thenReturnsZeroCount() {
            var exception = JavalidationException.of(ValidationErrors.empty());

            assertThat(exception.getMessage()).isEqualTo("Validation failed with 0 error(s)");
        }

        @Test
        void givenFieldErrorWithArgs_whenGetMessage_thenReturnsErrorCount() {
            var exception = JavalidationException.at("age", "Must be at least {0}", 18);

            assertThat(exception.getMessage()).isEqualTo("Validation failed with 1 error(s)");
        }

        @Test
        void givenMultipleFieldsWithMultipleErrors_whenGetMessage_thenReturnsTotalCount() {
            var validation = Validation.create();
            validation.addErrorAt("email", "Required");
            validation.addErrorAt("email", "Invalid format");
            validation.addErrorAt("name", "Required");
            validation.addErrorAt("age", "Too young");

            var exception = JavalidationException.of(validation.finish());

            assertThat(exception.getMessage()).isEqualTo("Validation failed with 4 error(s)");
        }
    }

    @Nested
    class GetErrorsTests {

        @Test
        void givenException_whenGetErrors_thenReturnsValidationErrors() {
            var errors = ValidationErrors.at("email", "Invalid format");
            var exception = JavalidationException.of(errors);

            assertThat(exception.getErrors()).isEqualTo(errors);
        }
    }

    @Nested
    class WithPrefixTests {

        @Test
        void givenFieldError_whenWithStringPrefix_thenFieldPathIsPrefixed() {
            var exception = JavalidationException.at("street", "required");

            var prefixed = exception.withPrefix("address");

            assertThat(prefixed.getErrors()).isEqualTo(new ValidationErrors(
                    List.of(),
                    Map.of(FieldKey.of("address", "street"), List.of(TemplateString.of("required")))
            ));
        }

        @Test
        void givenRootError_whenWithStringPrefix_thenRootBecomesFieldErrorAtPrefixKey() {
            var exception = JavalidationException.of("invalid.request");

            var prefixed = exception.withPrefix("order");

            assertThat(prefixed.getErrors()).isEqualTo(new ValidationErrors(
                    List.of(),
                    Map.of(FieldKey.of("order"), List.of(TemplateString.of("invalid.request")))
            ));
        }

        @Test
        void givenFieldError_whenWithNumberPrefix_thenFieldPathIsIndexPrefixed() {
            var exception = JavalidationException.at("name", "required");

            var prefixed = exception.withPrefix(0);

            assertThat(prefixed.getErrors()).isEqualTo(new ValidationErrors(
                    List.of(),
                    Map.of(FieldKey.of(0, "name"), List.of(TemplateString.of("required")))
            ));
        }

        @Test
        void givenFieldError_whenWithObjectPrefix_thenMixedSegmentsArePrepended() {
            var exception = JavalidationException.at("price", "too.low");

            var prefixed = exception.withPrefix("items", 2);

            assertThat(prefixed.getErrors()).isEqualTo(new ValidationErrors(
                    List.of(),
                    Map.of(FieldKey.of("items", 2, "price"), List.of(TemplateString.of("too.low")))
            ));
        }

        @Test
        void givenMultiplePrefixSegments_whenWithPrefix_thenAllSegmentsArePrepended() {
            var exception = JavalidationException.at("street", "required");

            var prefixed = exception.withPrefix("user", "address");

            assertThat(prefixed.getErrors()).isEqualTo(new ValidationErrors(
                    List.of(),
                    Map.of(FieldKey.of("user", "address", "street"), List.of(TemplateString.of("required")))
            ));
        }

        @Test
        void givenWithPrefix_whenGetMessage_thenErrorCountIsPreserved() {
            var exception = JavalidationException.at("street", "required");

            var prefixed = exception.withPrefix("address");

            assertThat(prefixed.getMessage()).isEqualTo("Validation failed with 1 error(s)");
        }

        @Test
        void givenWithPrefix_thenOriginalExceptionIsUnchanged() {
            var exception = JavalidationException.at("email", "invalid");
            var originalErrors = exception.getErrors();

            exception.withPrefix("user");

            assertThat(exception.getErrors()).isEqualTo(originalErrors);
        }

        @Test
        void givenWithPrefix_thenOriginalExceptionIsSetAsCause() {
            var original = JavalidationException.at("street", "required");

            var prefixed = original.withPrefix("address");

            assertThat(prefixed.getCause()).isSameAs(original);
        }

        @Test
        void givenChainedWithPrefix_thenCauseChainIsPreserved() {
            var original = JavalidationException.at("street", "required");

            var prefixed = original.withPrefix("address").withPrefix("user");

            assertThat(prefixed.getCause())
                    .isInstanceOf(JavalidationException.class)
                    .extracting(Throwable::getCause)
                    .isSameAs(original);
        }
    }
}
