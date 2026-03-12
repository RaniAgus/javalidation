package io.github.raniagus.javalidation;

import static io.github.raniagus.javalidation.ResultCollector.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ValidationCollectorTest {

    @Nested
    class AddErrorsToTests {

        @Test
        void givenAllOkResults_whenAddErrorsTo_thenValidationHasNoErrors() {
            Validation validation = Validation.create();
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.ok("value2");
            Result<String> result3 = Result.ok("value3");

            validation = Stream.of(result1, result2, result3)
                    .collect(addErrorsTo(validation));

            assertThat(validation.finish().isEmpty()).isTrue();
        }

        @Test
        void givenFailingResults_whenAddErrorsTo_thenValidationContainsAllErrors() {
            Validation validation = Validation.create();
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.errorAt("field", "error");
            Result<String> result3 = Result.error("root");

            validation = Stream.of(result1, result2, result3)
                    .collect(addErrorsTo(validation));

            assertThat(validation.finish()).isEqualTo(new ValidationErrors(
                    List.of(TemplateString.of("root")),
                    Map.of(FieldKey.of("field"), List.of(TemplateString.of("error")))
            ));
        }
    }

    @Nested
    class AddErrorsToWithWrapperTests {

        @Test
        void givenAllOkResults_whenAddErrorsToWithPrefix_thenValidationHasNoErrors() {
            Validation validation = Validation.create();
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.ok("value2");
            Result<String> result3 = Result.ok("value3");

            validation = Stream.of(result1, result2, result3)
                    .collect(withPrefix("items", addErrorsTo(validation)));

            assertThat(validation.finish().isEmpty()).isTrue();
        }

        @Test
        void givenFailingResults_whenAddErrorsToWithPrefix_thenValidationContainsAllErrors() {
            Validation validation = Validation.create();
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.errorAt("field", "error");
            Result<String> result3 = Result.error("root");

            validation = Stream.of(result1, result2, result3)
                    .collect(withPrefix("items", addErrorsTo(validation)));

            assertThat(validation.finish()).isEqualTo(new ValidationErrors(
                    List.of(),
                    Map.of(FieldKey.of("items"), List.of(TemplateString.of("root")),
                            FieldKey.of("items", "field"), List.of(TemplateString.of("error")))
            ));
        }

        @Test
        void givenFailingResults_whenAddErrorsToWithIndexPrefix_thenValidationContainsAllErrors() {
            Validation validation = Validation.create();
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.errorAt("field", "error");
            Result<String> result3 = Result.error("root");

            validation = Stream.of(result1, result2, result3)
                    .collect(withPrefix(0, addErrorsTo(validation)));

            assertThat(validation.finish()).isEqualTo(new ValidationErrors(
                    List.of(),
                    Map.of(FieldKey.of(0), List.of(TemplateString.of("root")),
                            FieldKey.of(0, "field"), List.of(TemplateString.of("error")))
            ));
        }
    }

    @Nested
    class ToValidationTests {

        @Test
        void givenAllOkResults_whenToValidation_thenValidationHasNoErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.ok("value2");
            Result<String> result3 = Result.ok("value3");

            Validation validation = Stream.of(result1, result2, result3)
                    .collect(toValidation());

            assertThat(validation.finish().isEmpty()).isTrue();
        }

        @Test
        void givenFailingResults_whenToValidation_thenValidationContainsAllErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.errorAt("field", "error");
            Result<String> result3 = Result.error("root");

            Validation validation = Stream.of(result1, result2, result3)
                    .collect(toValidation());

            assertThat(validation.finish()).isEqualTo(new ValidationErrors(
                    List.of(TemplateString.of("root")),
                    Map.of(FieldKey.of("field"), List.of(TemplateString.of("error")))
            ));
        }
    }

    @Nested
    class ToValidationWithWrapperTests {

        @Test
        void givenAllOkResults_whenToValidationWithPrefix_thenValidationHasNoErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.ok("value2");
            Result<String> result3 = Result.ok("value3");

            Validation validation = Stream.of(result1, result2, result3)
                    .collect(toValidation());

            assertThat(validation.finish().isEmpty()).isTrue();
        }

        @Test
        void givenFailingResults_whenToValidationWithPrefix_thenValidationContainsAllErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.errorAt("field", "error");
            Result<String> result3 = Result.error("root");

            Validation validation = Stream.of(result1, result2, result3)
                    .collect(withPrefix("items", toValidation()));

            assertThat(validation.finish()).isEqualTo(new ValidationErrors(
                    List.of(),
                    Map.of(FieldKey.of("items"), List.of(TemplateString.of("root")),
                            FieldKey.of("items", "field"), List.of(TemplateString.of("error")))
            ));
        }

        @Test
        void givenFailingResults_whenToValidationWithIndexPrefix_thenValidationContainsAllErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.errorAt("field", "error");
            Result<String> result3 = Result.error("root");

            Validation validation = Stream.of(result1, result2, result3)
                    .collect(withPrefix(0, toValidation()));

            assertThat(validation.finish()).isEqualTo(new ValidationErrors(
                    List.of(),
                    Map.of(FieldKey.of(0), List.of(TemplateString.of("root")),
                            FieldKey.of(0, "field"), List.of(TemplateString.of("error")))
            ));
        }
    }
}
