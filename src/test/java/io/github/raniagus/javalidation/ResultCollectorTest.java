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
}
