package io.github.raniagus.javalidation;

import static io.github.raniagus.javalidation.ResultCollector.into;
import static io.github.raniagus.javalidation.ResultCollector.withPrefix;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.raniagus.javalidation.format.TemplateString;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ValidationCollectorTest {

    @Nested
    class IntoTests {

        @Test
        void givenAllOkResults_whenInto_thenValidationHasNoErrors() {
            Validation validation = Validation.create();
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.ok("value2");
            Result<String> result3 = Result.ok("value3");

            validation = Stream.of(result1, result2, result3)
                    .collect(into(validation));

            assertThat(validation.finish().isEmpty()).isTrue();
        }

        @Test
        void givenFailingResults_whenInto_thenValidationContainsAllErrors() {
            Validation validation = Validation.create();
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.err("field", "error");
            Result<String> result3 = Result.err("root");

            validation = Stream.of(result1, result2, result3)
                    .collect(into(validation));

            assertThat(validation.finish()).isEqualTo(new ValidationErrors(
                    List.of(TemplateString.of("root")),
                    Map.of("field", List.of(TemplateString.of("error")))
            ));
        }
    }

    @Nested
    class IntoWithWrapperTests {

        @Test
        void givenAllOkResults_whenIntoWithPrefix_thenValidationHasNoErrors() {
            Validation validation = Validation.create();
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.ok("value2");
            Result<String> result3 = Result.ok("value3");

            validation = Stream.of(result1, result2, result3)
                    .collect(withPrefix("items", into(validation)));

            assertThat(validation.finish().isEmpty()).isTrue();
        }

        @Test
        void givenFailingResults_whenIntoWithPrefix_thenValidationContainsAllErrors() {
            Validation validation = Validation.create();
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.err("field", "error");
            Result<String> result3 = Result.err("root");

            validation = Stream.of(result1, result2, result3)
                    .collect(withPrefix("items", into(validation)));

            assertThat(validation.finish()).isEqualTo(new ValidationErrors(
                    List.of(),
                    Map.of("items", List.of(TemplateString.of("root")),
                            "items.field", List.of(TemplateString.of("error")))
            ));
        }
    }
}
