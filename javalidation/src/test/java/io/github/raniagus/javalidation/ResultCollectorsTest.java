package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.throwable;

import io.github.raniagus.javalidation.format.TemplateString;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

class ResultCollectorsTest {

    // -- toList --

    @Test
    void givenAllOkResults_whenToList_thenReturnsListOfValues() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.ok("value2");
        Result<String> result3 = Result.ok("value3");

        var list = Stream.of(result1, result2, result3)
                .collect(ResultCollectors.toList());

        assertThat(list).containsExactly("value1", "value2", "value3");
    }

    @Test
    void givenAllOkResults_whenToListWithCapacity_thenReturnsListOfValues() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.ok("value2");
        Result<String> result3 = Result.ok("value3");

        var list = Stream.of(result1, result2, result3)
                .collect(ResultCollectors.toList(3));

        assertThat(list).containsExactly("value1", "value2", "value3");
    }

    @Test
    void givenFailingResult_whenToList_thenThrowsException() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.err("field", "error");
        Result<String> result3 = Result.err("root");

        Stream<Result<String>> stream = Stream.of(result1, result2, result3);

        assertThatThrownBy(() -> stream.collect(ResultCollectors.toList()))
                .asInstanceOf(throwable(JavalidationException.class))
                .extracting(JavalidationException::getErrors)
                .isEqualTo(new ValidationErrors(
                        List.of(
                                TemplateString.of("root")
                        ),
                        Map.of("field", List.of(
                                TemplateString.of("error")
                        ))
                ));
    }

    @Test
    void givenAllOkResult_whenIndexedToList_thenReturnsListOfValues() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.ok("value2");
        Result<String> result3 = Result.ok("value3");

        var list = Stream.of(result1, result2, result3)
                .collect(ResultCollectors.indexed(ResultCollectors.toList(3)));

        assertThat(list).containsExactly("value1", "value2", "value3");
    }

    @Test
    void givenFailingResult_whenToListIndexed_thenThrowsException() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.err("field", "error");
        Result<String> result3 = Result.err("root");

        Stream<Result<String>> stream = Stream.of(result1, result2, result3);

        assertThatThrownBy(() -> stream.collect(ResultCollectors.indexed(ResultCollectors.toList(3))))
                .asInstanceOf(throwable(JavalidationException.class))
                .extracting(JavalidationException::getErrors)
                .isEqualTo(new ValidationErrors(
                        List.of(),
                        Map.of(
                                "[1].field", List.of(TemplateString.of("error")),
                                "[2]", List.of(TemplateString.of("root"))
                        )
                ));
    }

    @Test
    void givenFailingResult_whenToListIndexedWithPrefix_thenThrowsException() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.err("field", "error");
        Result<String> result3 = Result.err("root");

        Stream<Result<String>> stream = Stream.of(result1, result2, result3);

        assertThatThrownBy(() -> stream.collect(ResultCollectors.indexed(ResultCollectors.toList(3), "prefix")))
                .asInstanceOf(throwable(JavalidationException.class))
                .extracting(JavalidationException::getErrors)
                .isEqualTo(new ValidationErrors(
                        List.of(),
                        Map.of(
                                "prefix[1].field", List.of(TemplateString.of("error")),
                                "prefix[2]", List.of(TemplateString.of("root"))
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
                .collect(ResultCollectors.toResultList());

        assertThat(result.getOrThrow()).containsExactly("value1", "value2", "value3");
    }

    @Test
    void givenAllOkResults_whenToResultListWithCapacity_thenReturnsOkWithList() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.ok("value2");
        Result<String> result3 = Result.ok("value3");

        var result = Stream.of(result1, result2, result3)
                .collect(ResultCollectors.toResultList(3));

        assertThat(result.getOrThrow()).containsExactly("value1", "value2", "value3");
    }

    @Test
    void givenFailingResult_whenToResultList_thenReturnsErrResult() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.err("field", "error");
        Result<String> result3 = Result.err("root");

        var result = Stream.of(result1, result2, result3)
                .collect(ResultCollectors.toResultList());

        assertThat(result)
                .asInstanceOf(InstanceOfAssertFactories.type(Result.Err.class))
                .extracting(err -> err.getErrors())
                .isEqualTo(new ValidationErrors(
                        List.of(
                                TemplateString.of("root")
                        ),
                        Map.of("field", List.of(
                                TemplateString.of("error")
                        ))
                ));
    }

    @Test
    void givenFailingResult_whenToResultListWithCapacity_thenReturnsErrResult() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.err("field", "error");
        Result<String> result3 = Result.err("root");

        var result = Stream.of(result1, result2, result3)
                .collect(ResultCollectors.toResultList(3));

        assertThat(result)
                .asInstanceOf(InstanceOfAssertFactories.type(Result.Err.class))
                .extracting(err -> err.getErrors())
                .isEqualTo(new ValidationErrors(
                        List.of(
                                TemplateString.of("root")
                        ),
                        Map.of("field", List.of(
                                TemplateString.of("error")
                        ))
                ));
    }

    // -- toPartitioned --

    @Test
    void givenAllOkResults_whenToPartitioned_thenReturnsValuesWithNoErrors() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.ok("value2");
        Result<String> result3 = Result.ok("value3");

        var partitioned = Stream.of(result1, result2, result3)
                .collect(ResultCollectors.toPartitioned());

        assertThat(partitioned.value()).containsExactly("value1", "value2", "value3");
        assertThat(partitioned.errors().isEmpty()).isTrue();
    }

    @Test
    void givenAllOkResults_whenToPartitionedWithCapacity_thenReturnsValuesWithNoErrors() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.ok("value2");
        Result<String> result3 = Result.ok("value3");

        var partitioned = Stream.of(result1, result2, result3)
                .collect(ResultCollectors.toPartitioned(3));

        assertThat(partitioned.value()).containsExactly("value1", "value2", "value3");
        assertThat(partitioned.errors().isEmpty()).isTrue();
    }

    @Test
    void givenFailingResult_whenToPartitioned_thenReturnsValuesAndErrors() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.err("field", "error");
        Result<String> result3 = Result.err("root");

        var partitioned = Stream.of(result1, result2, result3)
                .collect(ResultCollectors.toPartitioned());

        assertThat(partitioned.value()).containsExactly("value1");
        assertThat(partitioned.errors()).isEqualTo(new ValidationErrors(
                List.of(TemplateString.of("root")),
                Map.of("field", List.of(TemplateString.of("error")))
        ));
    }

    @Test
    void givenFailingResult_whenToPartitionedWithCapacity_thenReturnsValuesAndErrors() {
        Result<String> result1 = Result.ok("value1");
        Result<String> result2 = Result.err("field", "error");
        Result<String> result3 = Result.err("root");

        var partitioned = Stream.of(result1, result2, result3)
                .collect(ResultCollectors.toPartitioned(3));

        assertThat(partitioned.value()).containsExactly("value1");
        assertThat(partitioned.errors()).isEqualTo(new ValidationErrors(
                List.of(TemplateString.of("root")),
                Map.of("field", List.of(TemplateString.of("error")))
        ));
    }

}
