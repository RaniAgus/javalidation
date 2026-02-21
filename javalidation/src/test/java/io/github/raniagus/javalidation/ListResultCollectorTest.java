package io.github.raniagus.javalidation;

import static io.github.raniagus.javalidation.ResultCollector.toListOrThrow;
import static io.github.raniagus.javalidation.ResultCollector.toPartitioned;
import static io.github.raniagus.javalidation.ResultCollector.toResultList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.throwable;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ListResultCollectorTest {

    @Nested
    class ToListOrThrowTests {

        @Test
        void givenAllOkResults_whenToListOrThrow_thenReturnsListOfValues() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.ok("value2");
            Result<String> result3 = Result.ok("value3");

            var list = Stream.of(result1, result2, result3)
                    .collect(toListOrThrow());

            assertThat(list).containsExactly("value1", "value2", "value3");
        }

        @Test
        void givenAllOkResults_whenToListOrThrowWithCapacity_thenReturnsListOfValues() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.ok("value2");
            Result<String> result3 = Result.ok("value3");

            var list = Stream.of(result1, result2, result3)
                    .collect(toListOrThrow(3));

            assertThat(list).containsExactly("value1", "value2", "value3");
        }

        @Test
        void givenFailingResults_whenToListOrThrow_thenThrowsExceptionWithAllErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.errorAt("field", "error");
            Result<String> result3 = Result.error("root");

            Stream<Result<String>> stream = Stream.of(result1, result2, result3);

            assertThatThrownBy(() -> stream.collect(toListOrThrow()))
                    .asInstanceOf(throwable(JavalidationException.class))
                    .extracting(JavalidationException::getErrors)
                    .isEqualTo(new ValidationErrors(
                            List.of(TemplateString.of("root")),
                            Map.of(FieldKey.of("field"), List.of(TemplateString.of("error")))
                    ));
        }

        @Test
        void givenFailingResults_whenToListOrThrowWithCapacity_thenThrowsExceptionWithAllErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.errorAt("field", "error");
            Result<String> result3 = Result.error("root");

            Stream<Result<String>> stream = Stream.of(result1, result2, result3);

            assertThatThrownBy(() -> stream.collect(toListOrThrow(3)))
                    .asInstanceOf(throwable(JavalidationException.class))
                    .extracting(JavalidationException::getErrors)
                    .isEqualTo(new ValidationErrors(
                            List.of(TemplateString.of("root")),
                            Map.of(FieldKey.of("field"), List.of(TemplateString.of("error")))
                    ));
        }

        @Test
        void givenOnlyFailingResults_whenToListOrThrow_thenThrowsException() {
            Result<String> result1 = Result.errorAt("field1", "error1");
            Result<String> result2 = Result.errorAt("field2", "error2");

            Stream<Result<String>> stream = Stream.of(result1, result2);

            assertThatThrownBy(() -> stream.collect(toListOrThrow()))
                    .asInstanceOf(throwable(JavalidationException.class))
                    .extracting(JavalidationException::getErrors)
                    .isEqualTo(new ValidationErrors(
                            List.of(),
                            Map.of(
                                    FieldKey.of("field1"), List.of(TemplateString.of("error1")),
                                    FieldKey.of("field2"), List.of(TemplateString.of("error2"))
                            )
                    ));
        }
    }

    @Nested
    class ToResultListTests {

        @Test
        void givenAllOkResults_whenToResultList_thenReturnsOkWithList() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.ok("value2");
            Result<String> result3 = Result.ok("value3");

            var result = Stream.of(result1, result2, result3)
                    .collect(toResultList());

            assertThat(result.getOrThrow()).containsExactly("value1", "value2", "value3");
        }

        @Test
        void givenAllOkResults_whenToResultListWithCapacity_thenReturnsOkWithList() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.ok("value2");
            Result<String> result3 = Result.ok("value3");

            var result = Stream.of(result1, result2, result3)
                    .collect(toResultList(3));

            assertThat(result.getOrThrow()).containsExactly("value1", "value2", "value3");
        }

        @Test
        void givenFailingResults_whenToResultList_thenReturnsErrWithAllErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.errorAt("field", "error");
            Result<String> result3 = Result.error("root");

            var result = Stream.of(result1, result2, result3)
                    .collect(toResultList());

            assertThat(result)
                    .asInstanceOf(InstanceOfAssertFactories.type(Result.Err.class))
                    .extracting(Result.Err::errors)
                    .isEqualTo(new ValidationErrors(
                            List.of(TemplateString.of("root")),
                            Map.of(FieldKey.of("field"), List.of(TemplateString.of("error")))
                    ));
        }

        @Test
        void givenFailingResults_whenToResultListWithCapacity_thenReturnsErrWithAllErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.errorAt("field", "error");
            Result<String> result3 = Result.error("root");

            var result = Stream.of(result1, result2, result3)
                    .collect(toResultList(3));

            assertThat(result)
                    .asInstanceOf(InstanceOfAssertFactories.type(Result.Err.class))
                    .extracting(Result.Err::errors)
                    .isEqualTo(new ValidationErrors(
                            List.of(TemplateString.of("root")),
                            Map.of(FieldKey.of("field"), List.of(TemplateString.of("error")))
                    ));
        }

        @Test
        void givenOnlyFailingResults_whenToResultList_thenReturnsErrResult() {
            Result<String> result1 = Result.errorAt("field1", "error1");
            Result<String> result2 = Result.errorAt("field2", "error2");

            var result = Stream.of(result1, result2)
                    .collect(toResultList());

            assertThat(result)
                    .asInstanceOf(InstanceOfAssertFactories.type(Result.Err.class))
                    .extracting(Result.Err::errors)
                    .isEqualTo(new ValidationErrors(
                            List.of(),
                            Map.of(
                                    FieldKey.of("field1"), List.of(TemplateString.of("error1")),
                                    FieldKey.of("field2"), List.of(TemplateString.of("error2"))
                            )
                    ));
        }
    }

    @Nested
    class ToPartitionedTests {

        @Test
        void givenAllOkResults_whenToPartitioned_thenReturnsAllValuesWithNoErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.ok("value2");
            Result<String> result3 = Result.ok("value3");

            var partitioned = Stream.of(result1, result2, result3)
                    .collect(toPartitioned());

            assertThat(partitioned.value()).containsExactly("value1", "value2", "value3");
            assertThat(partitioned.errors().isEmpty()).isTrue();
        }

        @Test
        void givenAllOkResults_whenToPartitionedWithCapacity_thenReturnsAllValuesWithNoErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.ok("value2");
            Result<String> result3 = Result.ok("value3");

            var partitioned = Stream.of(result1, result2, result3)
                    .collect(toPartitioned(3));

            assertThat(partitioned.value()).containsExactly("value1", "value2", "value3");
            assertThat(partitioned.errors().isEmpty()).isTrue();
        }

        @Test
        void givenFailingResults_whenToPartitioned_thenReturnsValidValuesAndErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.errorAt("field", "error");
            Result<String> result3 = Result.error("root");

            var partitioned = Stream.of(result1, result2, result3)
                    .collect(toPartitioned());

            assertThat(partitioned.value()).containsExactly("value1");
            assertThat(partitioned.errors()).isEqualTo(new ValidationErrors(
                    List.of(TemplateString.of("root")),
                    Map.of(FieldKey.of("field"), List.of(TemplateString.of("error")))
            ));
        }

        @Test
        void givenFailingResults_whenToPartitionedWithCapacity_thenReturnsValidValuesAndErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.errorAt("field", "error");
            Result<String> result3 = Result.error("root");

            var partitioned = Stream.of(result1, result2, result3)
                    .collect(toPartitioned(3));

            assertThat(partitioned.value()).containsExactly("value1");
            assertThat(partitioned.errors()).isEqualTo(new ValidationErrors(
                    List.of(TemplateString.of("root")),
                    Map.of(FieldKey.of("field"), List.of(TemplateString.of("error")))
            ));
        }

        @Test
        void givenOnlyFailingResults_whenToPartitioned_thenReturnsEmptyListAndErrors() {
            Result<String> result1 = Result.errorAt("field1", "error1");
            Result<String> result2 = Result.errorAt("field2", "error2");

            var partitioned = Stream.of(result1, result2)
                    .collect(toPartitioned());

            assertThat(partitioned.value()).isEmpty();
            assertThat(partitioned.errors()).isEqualTo(new ValidationErrors(
                    List.of(),
                    Map.of(
                            FieldKey.of("field1"), List.of(TemplateString.of("error1")),
                            FieldKey.of("field2"), List.of(TemplateString.of("error2"))
                    )
            ));
        }

        @Test
        void givenMixedResults_whenToPartitioned_thenReturnsBothValidValuesAndErrors() {
            Result<String> result1 = Result.ok("value1");
            Result<String> result2 = Result.errorAt("field2", "error2");
            Result<String> result3 = Result.ok("value3");
            Result<String> result4 = Result.errorAt("field4", "error4");

            var partitioned = Stream.of(result1, result2, result3, result4)
                    .collect(toPartitioned());

            assertThat(partitioned.value()).containsExactly("value1", "value3");
            assertThat(partitioned.errors()).isEqualTo(new ValidationErrors(
                    List.of(),
                    Map.of(
                            FieldKey.of("field2"), List.of(TemplateString.of("error2")),
                            FieldKey.of("field4"), List.of(TemplateString.of("error4"))
                    )
            ));
        }
    }
}
