package io.github.raniagus.javalidation;

import static io.github.raniagus.javalidation.ResultCollector.toListOrThrow;
import static io.github.raniagus.javalidation.ResultCollector.toPartitioned;
import static io.github.raniagus.javalidation.ResultCollector.toResultList;
import static io.github.raniagus.javalidation.ResultCollector.withIndex;
import static io.github.raniagus.javalidation.ResultCollector.withPrefix;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.throwable;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ResultCollectorWrapperTest {

    @Nested
    class WithIndexTests {

        @Test
        void givenAllOkResults_whenWithIndexToListOrThrow_thenReturnsListOfValues() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.ok("value2");
            Result<String> result3 = Result.ok("value3");

            var list = Stream.of(result1, result2, result3)
                    .collect(withIndex(toListOrThrow()));

            assertThat(list).containsExactly("value1", "value2", "value3");
        }

        @Test
        void givenFailingResults_whenWithIndexToListOrThrow_thenThrowsExceptionWithIndexedErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.err("field", "error");
            Result<String> result3 = Result.err("root");

            Stream<Result<String>> stream = Stream.of(result1, result2, result3);

            assertThatThrownBy(() -> stream.collect(withIndex(toListOrThrow())))
                    .asInstanceOf(throwable(JavalidationException.class))
                    .extracting(JavalidationException::getErrors)
                    .isEqualTo(new ValidationErrors(
                            List.of(),
                            Map.of(
                                    FieldKey.of(1, "field"), List.of(TemplateString.of("error")),
                                    FieldKey.of(2), List.of(TemplateString.of("root"))
                            )
                    ));
        }

        @Test
        void givenFailingResults_whenWithIndexToResultList_thenReturnsErrWithIndexedErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.err("field", "error");
            Result<String> result3 = Result.err("root");

            var result = Stream.of(result1, result2, result3)
                    .collect(withIndex(toResultList()));

            assertThat(result)
                    .asInstanceOf(InstanceOfAssertFactories.type(Result.Err.class))
                    .extracting(Result.Err::errors)
                    .isEqualTo(new ValidationErrors(
                            List.of(),
                            Map.of(
                                    FieldKey.of(1, "field"), List.of(TemplateString.of("error")),
                                    FieldKey.of(2), List.of(TemplateString.of("root"))
                            )
                    ));
        }

        @Test
        void givenFailingResults_whenWithIndexToPartitioned_thenReturnsPartitionedWithIndexedErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.err("field", "error");
            Result<String> result3 = Result.ok("value3");

            var partitioned = Stream.of(result1, result2, result3)
                    .collect(withIndex(toPartitioned()));

            assertThat(partitioned.value()).containsExactly("value1", "value3");
            assertThat(partitioned.errors()).isEqualTo(new ValidationErrors(
                    List.of(),
                    Map.of(FieldKey.of(1, "field"), List.of(TemplateString.of("error")))
            ));
        }

        @Test
        void givenMultipleFailingResults_whenWithIndex_thenIndexesIncrement() {
            Result<String> result1 = Result.err("field", "error1");
            Result<String> result2 = Result.err("field", "error2");
            Result<String> result3 = Result.err("field", "error3");

            var result = Stream.of(result1, result2, result3)
                    .collect(withIndex(toResultList()));

            assertThat(result)
                    .asInstanceOf(InstanceOfAssertFactories.type(Result.Err.class))
                    .extracting(Result.Err::errors)
                    .isEqualTo(new ValidationErrors(
                            List.of(),
                            Map.of(
                                    FieldKey.of(0, "field"), List.of(TemplateString.of("error1")),
                                    FieldKey.of(1, "field"), List.of(TemplateString.of("error2")),
                                    FieldKey.of(2, "field"), List.of(TemplateString.of("error3"))
                            )
                    ));
        }

        @Test
        void givenResultsWithRootErrors_whenWithIndex_thenConvertsRootErrorsToIndexedFields() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.err("root error 2");
            Result<String> result3 = Result.ok("value3");
            Result<String> result4 = Result.err("root error 4");

            var result = Stream.of(result1, result2, result3, result4)
                    .collect(withIndex(toResultList()));

            assertThat(result)
                    .asInstanceOf(InstanceOfAssertFactories.type(Result.Err.class))
                    .extracting(Result.Err::errors)
                    .isEqualTo(new ValidationErrors(
                            List.of(),
                            Map.of(
                                    FieldKey.of(1), List.of(TemplateString.of("root error 2")),
                                    FieldKey.of(3), List.of(TemplateString.of("root error 4"))
                            )
                    ));
        }
    }

    @Nested
    class WithPrefixTests {

        @Test
        void givenAllOkResults_whenWithPrefixToListOrThrow_thenReturnsListOfValues() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.ok("value2");
            Result<String> result3 = Result.ok("value3");

            var list = Stream.of(result1, result2, result3)
                    .collect(withPrefix("items", toListOrThrow()));

            assertThat(list).containsExactly("value1", "value2", "value3");
        }

        @Test
        void givenFailingResults_whenWithPrefixToListOrThrow_thenThrowsExceptionWithPrefixedErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.err("field", "error");
            Result<String> result3 = Result.err("root");

            Stream<Result<String>> stream = Stream.of(result1, result2, result3);

            assertThatThrownBy(() -> stream.collect(withPrefix("items", toListOrThrow())))
                    .asInstanceOf(throwable(JavalidationException.class))
                    .extracting(JavalidationException::getErrors)
                    .isEqualTo(new ValidationErrors(
                            List.of(),
                            Map.of(
                                    FieldKey.of("items", "field"), List.of(TemplateString.of("error")),
                                    FieldKey.of("items"), List.of(TemplateString.of("root"))
                            )
                    ));
        }

        @Test
        void givenFailingResults_whenWithPrefixToResultList_thenReturnsErrWithPrefixedErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.err("field", "error");
            Result<String> result3 = Result.err("root");

            var result = Stream.of(result1, result2, result3)
                    .collect(withPrefix("order", toResultList()));

            assertThat(result)
                    .asInstanceOf(InstanceOfAssertFactories.type(Result.Err.class))
                    .extracting(Result.Err::errors)
                    .isEqualTo(new ValidationErrors(
                            List.of(),
                            Map.of(
                                    FieldKey.of("order", "field"), List.of(TemplateString.of("error")),
                                    FieldKey.of("order"), List.of(TemplateString.of("root"))
                            )
                    ));
        }

        @Test
        void givenFailingResults_whenWithPrefixToPartitioned_thenReturnsPartitionedWithPrefixedErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.err("price", "must be positive");
            Result<String> result3 = Result.ok("value3");

            var partitioned = Stream.of(result1, result2, result3)
                    .collect(withPrefix("items", toPartitioned()));

            assertThat(partitioned.value()).containsExactly("value1", "value3");
            assertThat(partitioned.errors()).isEqualTo(new ValidationErrors(
                    List.of(),
                    Map.of(FieldKey.of("items", "price"), List.of(TemplateString.of("must be positive")))
            ));
        }

        @Test
        void givenNestedPrefix_whenWithPrefix_thenConcatenatesWithDot() {
            Result<String> result1 = Result.err("field", "error");

            var result = Stream.of(result1)
                    .collect(withPrefix("items", toResultList()));

            assertThat(result)
                    .asInstanceOf(InstanceOfAssertFactories.type(Result.Err.class))
                    .extracting(Result.Err::errors)
                    .isEqualTo(new ValidationErrors(
                            List.of(),
                            Map.of(FieldKey.of("items", "field"), List.of(TemplateString.of("error")))
                    ));
        }
    }

    @Nested
    class CombinedWrapperTests {

        @Test
        void givenWithPrefixAndWithIndex_whenToListOrThrow_thenAppliesBothPrefixAndIndex() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.err("field", "error");
            Result<String> result3 = Result.err("root");

            Stream<Result<String>> stream = Stream.of(result1, result2, result3);

            assertThatThrownBy(() -> stream.collect(withPrefix("items", withIndex(toListOrThrow()))))
                    .asInstanceOf(throwable(JavalidationException.class))
                    .extracting(JavalidationException::getErrors)
                    .isEqualTo(new ValidationErrors(
                            List.of(),
                            Map.of(
                                    FieldKey.of("items", 1, "field"), List.of(TemplateString.of("error")),
                                    FieldKey.of("items", 2), List.of(TemplateString.of("root"))
                            )
                    ));
        }

        @Test
        void givenWithPrefixAndWithIndex_whenToResultList_thenAppliesBothPrefixAndIndex() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.err("price", "must be positive");
            Result<String> result3 = Result.ok("value3");
            Result<String> result4 = Result.err("name", "required");

            var result = Stream.of(result1, result2, result3, result4)
                    .collect(withPrefix("items", withIndex(toResultList())));

            assertThat(result)
                    .asInstanceOf(InstanceOfAssertFactories.type(Result.Err.class))
                    .extracting(Result.Err::errors)
                    .isEqualTo(new ValidationErrors(
                            List.of(),
                            Map.of(
                                    FieldKey.of("items", 1, "price"), List.of(TemplateString.of("must be positive")),
                                    FieldKey.of("items", 3, "name"), List.of(TemplateString.of("required"))
                            )
                    ));
        }

        @Test
        void givenWithPrefixAndWithIndex_whenToPartitioned_thenAppliesBothPrefixAndIndex() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.err("field", "error2");
            Result<String> result3 = Result.ok("value3");
            Result<String> result4 = Result.err("field", "error4");

            var partitioned = Stream.of(result1, result2, result3, result4)
                    .collect(withPrefix("users", withIndex(toPartitioned())));

            assertThat(partitioned.value()).containsExactly("value1", "value3");
            assertThat(partitioned.errors()).isEqualTo(new ValidationErrors(
                    List.of(),
                    Map.of(
                            FieldKey.of("users", 1, "field"), List.of(TemplateString.of("error2")),
                            FieldKey.of("users", 3, "field"), List.of(TemplateString.of("error4"))
                    )
            ));
        }

        @Test
        void givenMultiplePrefixes_whenChained_thenConcatenatesDirectly() {
            Result<String> result1 = Result.err("field", "error");

            // Note: Chaining withPrefix() directly concatenates without dots
            // The dot is added when converting to field errors in Validation.addAll()
            var result = Stream.of(result1)
                    .collect(withPrefix("order", withPrefix("items", toResultList())));

            assertThat(result)
                    .asInstanceOf(InstanceOfAssertFactories.type(Result.Err.class))
                    .extracting(Result.Err::errors)
                    .isEqualTo(new ValidationErrors(
                            List.of(),
                            Map.of(FieldKey.of("order", "items", "field"), List.of(TemplateString.of("error")))
                    ));
        }

        @Test
        void givenNestedPrefixesWithIndex_whenChained_thenAppliesAllCorrectly() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.err("name", "too short");
            Result<String> result3 = Result.err("age", "too young");

            // Note: Chaining withPrefix() directly concatenates without dots
            // Use dot notation in the prefix string if needed: "request.users"
            var result = Stream.of(result1, result2, result3)
                    .collect(withPrefix("request", withPrefix("users", withIndex(toResultList()))));

            assertThat(result)
                    .asInstanceOf(InstanceOfAssertFactories.type(Result.Err.class))
                    .extracting(Result.Err::errors)
                    .isEqualTo(new ValidationErrors(
                            List.of(),
                            Map.of(
                                    FieldKey.of("request", "users", 1, "name"), List.of(TemplateString.of("too short")),
                                    FieldKey.of("request", "users", 2, "age"), List.of(TemplateString.of("too young"))
                            )
                    ));
        }

        @Test
        void givenRootErrors_whenWithPrefixAndWithIndex_thenConvertsToIndexedPrefixedFields() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.err("invalid");
            Result<String> result3 = Result.ok("value3");

            var result = Stream.of(result1, result2, result3)
                    .collect(withPrefix("items", withIndex(toResultList())));

            assertThat(result)
                    .asInstanceOf(InstanceOfAssertFactories.type(Result.Err.class))
                    .extracting(Result.Err::errors)
                    .isEqualTo(new ValidationErrors(
                            List.of(),
                            Map.of(FieldKey.of("items", 1), List.of(TemplateString.of("invalid")))
                    ));
        }
    }
}
