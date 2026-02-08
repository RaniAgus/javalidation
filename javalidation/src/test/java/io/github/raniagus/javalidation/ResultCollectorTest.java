package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;
import static org.assertj.core.api.InstanceOfAssertFactories.throwable;

import io.github.raniagus.javalidation.format.TemplateString;
import io.github.raniagus.javalidation.util.ErrorStrings;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ResultCollectorTest {

    // -- toList --

    @Test
    void givenAllOkResults_whenToList_thenReturnsListOfValues() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.ok("value2");
        Result<String> result3 = Result.ok("value3");

        var list = Stream.of(result1, result2, result3)
                .collect(ResultCollector.toList());

        assertThat(list).containsExactly("value1", "value2", "value3");
    }

    @Test
    void givenSomeErrResults_whenToList_thenThrowsWithIndexedErrors() {
        Result<String> result1 = Result.err(ErrorStrings.ERROR_1);
        Result<String> result2 = Result.err(
                Validation.create()
                        .addRootError(ErrorStrings.ERROR_2)
                        .addRootError(ErrorStrings.ERROR_3)
                        .finish()
        );
        Result<String> result3 = Result.ok("value");

        assertThatThrownBy(() -> Stream.of(result1, result2, result3)
                .collect(ResultCollector.toList()))
                .isInstanceOf(JavalidationException.class)
                .asInstanceOf(throwable(JavalidationException.class))
                .extracting(JavalidationException::getErrors)
                .extracting(ValidationErrors::fieldErrors)
                .asInstanceOf(MAP)
                .containsAllEntriesOf(Map.of(
                        "[0]", List.of(TemplateString.of("Error 1")),
                        "[1]", List.of(
                                TemplateString.of("Error 2"),
                                TemplateString.of("Error 3")
                        )
                ));
    }

    // -- toResultList --

    @Test
    void givenAllOkResults_whenToResultList_thenReturnsOkWithList() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.ok("value2");
        Result<String> result3 = Result.ok("value3");

        var result = Stream.of(result1, result2, result3)
                .collect(ResultCollector.toResultList());

        assertThat(result.getOrThrow()).containsExactly("value1", "value2", "value3");
    }

    @Test
    void givenSomeErrResults_whenToResultList_thenReturnsErrWithIndexedErrors() {
        Result<String> result1 = Result.err(ErrorStrings.ERROR_1);
        Result<String> result2 = Result.err(
                Validation.create()
                        .addRootError(ErrorStrings.ERROR_2)
                        .addRootError(ErrorStrings.ERROR_3)
                        .finish()
        );
        Result<String> result3 = Result.ok("value");

        var result = Stream.of(result1, result2, result3)
                .collect(ResultCollector.toResultList());

        assertThat(result.getErrors())
                .extracting(ValidationErrors::fieldErrors)
                .asInstanceOf(MAP)
                .containsAllEntriesOf(Map.of(
                        "[0]", List.of(TemplateString.of("Error 1")),
                        "[1]", List.of(
                                TemplateString.of("Error 2"),
                                TemplateString.of("Error 3")
                        )
                ));
    }

    // -- toPartitioned --

    @Test
    void givenAllOkResults_whenToPartitioned_thenReturnsValuesWithNoErrors() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.ok("value2");
        Result<String> result3 = Result.ok("value3");

        var partitioned = Stream.of(result1, result2, result3)
                .collect(ResultCollector.toPartitioned());

        assertThat(partitioned.value()).containsExactly("value1", "value2", "value3");
        assertThat(partitioned.errors().isEmpty()).isTrue();
    }

    @Test
    void givenSomeErrResults_whenToPartitioned_thenReturnsOkValuesAndIndexedErrors() {
        Result<String> result1 = Result.err(ErrorStrings.ERROR_1);
        Result<String> result2 = Result.err(
                Validation.create()
                        .addRootError(ErrorStrings.ERROR_2)
                        .addRootError(ErrorStrings.ERROR_3)
                        .finish()
        );
        Result<String> result3 = Result.ok("value");

        var partitioned = Stream.of(result1, result2, result3)
                .collect(ResultCollector.toPartitioned());

        assertThat(partitioned.value()).containsExactly("value");
        assertThat(partitioned.errors())
                .extracting(ValidationErrors::fieldErrors)
                .asInstanceOf(MAP)
                .containsAllEntriesOf(Map.of(
                        "[0]", List.of(TemplateString.of("Error 1")),
                        "[1]", List.of(
                                TemplateString.of("Error 2"),
                                TemplateString.of("Error 3")
                        )
                ));
    }

    // -- edge cases --

    @Test
    void givenEmptyStream_whenToList_thenReturnsEmptyList() {
        var list = Stream.<Result<String>>empty()
                .collect(ResultCollector.toList());

        assertThat(list).isEmpty();
    }

    @Test
    void givenEmptyStream_whenToResultList_thenReturnsOkWithEmptyList() {
        var result = Stream.<Result<String>>empty()
                .collect(ResultCollector.toResultList());

        assertThat(result).isInstanceOf(Result.Ok.class);
        assertThat(result.getOrThrow()).isEmpty();
    }

    @Test
    void givenEmptyStream_whenToPartitioned_thenReturnsEmptyValuesAndNoErrors() {
        var partitioned = Stream.<Result<String>>empty()
                .collect(ResultCollector.toPartitioned());

        assertThat(partitioned.value()).isEmpty();
        assertThat(partitioned.errors().isEmpty()).isTrue();
    }

    @Test
    void givenResultsWithFieldErrors_whenToList_thenThrowsWithIndexedFieldErrors() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.err("field", "Field error");
        Result<String> result3 = Result.ok("value3");

        assertThatThrownBy(() -> Stream.of(result1, result2, result3)
                .collect(ResultCollector.toList()))
                .isInstanceOf(JavalidationException.class)
                .asInstanceOf(throwable(JavalidationException.class))
                .extracting(JavalidationException::getErrors)
                .extracting(ValidationErrors::fieldErrors)
                .asInstanceOf(MAP)
                .containsEntry("[0].field", List.of(TemplateString.of("Field error")));
    }

    @Test
    void givenResultsWithFieldErrors_whenToResultList_thenReturnsErrWithIndexedFieldErrors() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.err("field", "Field error");
        Result<String> result3 = Result.ok("value3");

        var result = Stream.of(result1, result2, result3)
                .collect(ResultCollector.toResultList());

        assertThat(result).isInstanceOf(Result.Err.class);
        assertThat(result.getErrors())
                .extracting(ValidationErrors::fieldErrors)
                .asInstanceOf(MAP)
                .containsEntry("[0].field", List.of(TemplateString.of("Field error")));
    }

    @Test
    void givenResultsWithFieldErrors_whenToPartitioned_thenReturnsOkValuesAndIndexedFieldErrors() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.err("field", "Field error");
        Result<String> result3 = Result.ok("value3");

        var partitioned = Stream.of(result1, result2, result3)
                .collect(ResultCollector.toPartitioned());

        assertThat(partitioned.value()).containsExactly("value1", "value3");
        assertThat(partitioned.errors())
                .extracting(ValidationErrors::fieldErrors)
                .asInstanceOf(MAP)
                .containsEntry("[0].field", List.of(TemplateString.of("Field error")));
    }

    @Test
    void givenLargeStream_whenToResultList_thenAccumulatesAllErrors() {
        var results = Stream.iterate(0, i -> i < 20, i -> i + 1)
                .map(i -> i % 3 == 0
                        ? Result.<Integer>err("error", "Error at " + i)
                        : Result.ok(i))
                .collect(ResultCollector.toResultList());

        assertThat(results).isInstanceOf(Result.Err.class);
        assertThat(results.getErrors().fieldErrors()).hasSize(7);
        assertThat(results.getErrors().fieldErrors())
                .containsKeys("[0].error", "[1].error", "[2].error", "[3].error",
                        "[4].error", "[5].error", "[6].error");
    }

    @Test
    void givenAllErrResults_whenToPartitioned_thenReturnsEmptyValuesWithAllErrors() {
        Result<String> result1 = Result.err(ErrorStrings.ERROR_1);
        Result<String> result2 = Result.err(ErrorStrings.ERROR_2);
        Result<String> result3 = Result.err(ErrorStrings.ERROR_3);

        var partitioned = Stream.of(result1, result2, result3)
                .collect(ResultCollector.toPartitioned());

        assertThat(partitioned.value()).isEmpty();
        assertThat(partitioned.errors().isNotEmpty()).isTrue();
        assertThat(partitioned.errors().fieldErrors()).hasSize(3);
    }

    // -- toList with prefix --

    @Test
    void givenAllOkResults_whenToListWithPrefix_thenReturnsListOfValues() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.ok("value2");
        Result<String> result3 = Result.ok("value3");

        var list = Stream.of(result1, result2, result3)
                .collect(ResultCollector.toList("items"));

        assertThat(list).containsExactly("value1", "value2", "value3");
    }

    @Test
    void givenSomeErrResults_whenToListWithPrefix_thenThrowsWithPrefixedIndexedErrors() {
        Result<String> result1 = Result.err(ErrorStrings.ERROR_1);
        Result<String> result2 = Result.err(
                Validation.create()
                        .addRootError(ErrorStrings.ERROR_2)
                        .addRootError(ErrorStrings.ERROR_3)
                        .finish()
        );
        Result<String> result3 = Result.ok("value");

        assertThatThrownBy(() -> Stream.of(result1, result2, result3)
                .collect(ResultCollector.toList("items")))
                .isInstanceOf(JavalidationException.class)
                .asInstanceOf(throwable(JavalidationException.class))
                .extracting(JavalidationException::getErrors)
                .extracting(ValidationErrors::fieldErrors)
                .asInstanceOf(MAP)
                .containsAllEntriesOf(Map.of(
                        "items[0]", List.of(TemplateString.of("Error 1")),
                        "items[1]", List.of(
                                TemplateString.of("Error 2"),
                                TemplateString.of("Error 3")
                        )
                ));
    }

    @Test
    void givenResultsWithFieldErrors_whenToListWithPrefix_thenThrowsWithPrefixedIndexedFieldErrors() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.err("field", "Field error");
        Result<String> result3 = Result.ok("value3");

        assertThatThrownBy(() -> Stream.of(result1, result2, result3)
                .collect(ResultCollector.toList("order.items")))
                .isInstanceOf(JavalidationException.class)
                .asInstanceOf(throwable(JavalidationException.class))
                .extracting(JavalidationException::getErrors)
                .extracting(ValidationErrors::fieldErrors)
                .asInstanceOf(MAP)
                .containsEntry("order.items[0].field", List.of(TemplateString.of("Field error")));
    }

    // -- toResultList with prefix --

    @Test
    void givenAllOkResults_whenToResultListWithPrefix_thenReturnsOkWithList() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.ok("value2");
        Result<String> result3 = Result.ok("value3");

        var result = Stream.of(result1, result2, result3)
                .collect(ResultCollector.toResultList("items"));

        assertThat(result.getOrThrow()).containsExactly("value1", "value2", "value3");
    }

    @Test
    void givenSomeErrResults_whenToResultListWithPrefix_thenReturnsErrWithPrefixedIndexedErrors() {
        Result<String> result1 = Result.err(ErrorStrings.ERROR_1);
        Result<String> result2 = Result.err(
                Validation.create()
                        .addRootError(ErrorStrings.ERROR_2)
                        .addRootError(ErrorStrings.ERROR_3)
                        .finish()
        );
        Result<String> result3 = Result.ok("value");

        var result = Stream.of(result1, result2, result3)
                .collect(ResultCollector.toResultList("items"));

        assertThat(result.getErrors())
                .extracting(ValidationErrors::fieldErrors)
                .asInstanceOf(MAP)
                .containsAllEntriesOf(Map.of(
                        "items[0]", List.of(TemplateString.of("Error 1")),
                        "items[1]", List.of(
                                TemplateString.of("Error 2"),
                                TemplateString.of("Error 3")
                        )
                ));
    }

    @Test
    void givenResultsWithFieldErrors_whenToResultListWithPrefix_thenReturnsErrWithPrefixedIndexedFieldErrors() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.err("field", "Field error");
        Result<String> result3 = Result.ok("value3");

        var result = Stream.of(result1, result2, result3)
                .collect(ResultCollector.toResultList("order.items"));

        assertThat(result).isInstanceOf(Result.Err.class);
        assertThat(result.getErrors())
                .extracting(ValidationErrors::fieldErrors)
                .asInstanceOf(MAP)
                .containsEntry("order.items[0].field", List.of(TemplateString.of("Field error")));
    }

    // -- toPartitioned with prefix --

    @Test
    void givenAllOkResults_whenToPartitionedWithPrefix_thenReturnsValuesWithNoErrors() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.ok("value2");
        Result<String> result3 = Result.ok("value3");

        var partitioned = Stream.of(result1, result2, result3)
                .collect(ResultCollector.toPartitioned("items"));

        assertThat(partitioned.value()).containsExactly("value1", "value2", "value3");
        assertThat(partitioned.errors().isEmpty()).isTrue();
    }

    @Test
    void givenSomeErrResults_whenToPartitionedWithPrefix_thenReturnsOkValuesAndPrefixedIndexedErrors() {
        Result<String> result1 = Result.err(ErrorStrings.ERROR_1);
        Result<String> result2 = Result.err(
                Validation.create()
                        .addRootError(ErrorStrings.ERROR_2)
                        .addRootError(ErrorStrings.ERROR_3)
                        .finish()
        );
        Result<String> result3 = Result.ok("value");

        var partitioned = Stream.of(result1, result2, result3)
                .collect(ResultCollector.toPartitioned("items"));

        assertThat(partitioned.value()).containsExactly("value");
        assertThat(partitioned.errors())
                .extracting(ValidationErrors::fieldErrors)
                .asInstanceOf(MAP)
                .containsAllEntriesOf(Map.of(
                        "items[0]", List.of(TemplateString.of("Error 1")),
                        "items[1]", List.of(
                                TemplateString.of("Error 2"),
                                TemplateString.of("Error 3")
                        )
                ));
    }

    @Test
    void givenResultsWithFieldErrors_whenToPartitionedWithPrefix_thenReturnsOkValuesAndPrefixedIndexedFieldErrors() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.err("field", "Field error");
        Result<String> result3 = Result.ok("value3");

        var partitioned = Stream.of(result1, result2, result3)
                .collect(ResultCollector.toPartitioned("order.items"));

        assertThat(partitioned.value()).containsExactly("value1", "value3");
        assertThat(partitioned.errors())
                .extracting(ValidationErrors::fieldErrors)
                .asInstanceOf(MAP)
                .containsEntry("order.items[0].field", List.of(TemplateString.of("Field error")));
    }
}
