package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;
import static org.assertj.core.api.InstanceOfAssertFactories.throwable;

import io.github.raniagus.javalidation.format.TemplateString;
import io.github.raniagus.javalidation.util.ErrorStrings;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ResultCollectorTest {
    @Test
    void sePuedenColectarErroresDeMultiplesTriesComoListaFallida() {
        Result<String> resultado1 = Result.err(ErrorStrings.ERROR_1);
        Result<String> resultado2 = Result.err(
                Validation.create()
                        .addRootError(ErrorStrings.ERROR_2)
                        .addRootError(ErrorStrings.ERROR_3)
                        .finish()
        );
        Result<String> resultado3 = Result.ok("Resultado exitoso");

        Supplier<List<String>> listSupplier = () -> Stream.of(resultado1, resultado2, resultado3)
                .collect(ResultCollector.toList());

        assertThatThrownBy(listSupplier::get)
                .isInstanceOf(ValidationException.class)
                .asInstanceOf(throwable(ValidationException.class))
                .extracting(ValidationException::getErrors)
                .extracting(ValidationErrors::fieldErrors)
                .asInstanceOf(MAP)
                .containsAllEntriesOf(Map.of(
                        "[0]", List.of(new TemplateString("Error 1")),
                        "[1]", List.of(
                                new TemplateString("Error 2"),
                                new TemplateString("Error 3")
                        )
                ));
    }

    @Test
    void sePuedenColectarErroresDeMultiplesTriesComoResultado() {
        Result<String> resultado1 = Result.err(ErrorStrings.ERROR_1);
        Result<String> resultado2 = Result.err(
                Validation.create()
                        .addRootError(ErrorStrings.ERROR_2)
                        .addRootError(ErrorStrings.ERROR_3)
                        .finish()
        );
        Result<String> resultado3 = Result.ok("Resultado exitoso");

        Result<List<String>> resultadoConcatenado = Stream.of(resultado1, resultado2, resultado3)
                .collect(ResultCollector.toResultList());

        assertThat(resultadoConcatenado.getErrors())
                .extracting(ValidationErrors::fieldErrors)
                .asInstanceOf(MAP)
                .containsAllEntriesOf(Map.of(
                        "[0]", List.of(new TemplateString("Error 1")),
                        "[1]", List.of(
                                new TemplateString("Error 2"),
                                new TemplateString("Error 3")
                        )
                ));
    }

    @Test
    void sePuedenColectarErroresDeMultiplesTriesComoResultadoParticionado() {
        Result<String> resultado1 = Result.err(ErrorStrings.ERROR_1);
        Result<String> resultado2 = Result.err(
                Validation.create()
                        .addRootError(ErrorStrings.ERROR_2)
                        .addRootError(ErrorStrings.ERROR_3)
                        .finish()
        );
        Result<String> resultado3 = Result.ok("Resultado exitoso");

        PartitionedResult<List<String>> resultadoConcatenado = Stream.of(resultado1, resultado2, resultado3)
                .collect(ResultCollector.toPartitioned());

        assertThat(resultadoConcatenado.value())
                .containsExactly("Resultado exitoso");

        assertThat(resultadoConcatenado.errors())
                .extracting(ValidationErrors::fieldErrors)
                .asInstanceOf(MAP)
                .containsAllEntriesOf(Map.of(
                        "[0]", List.of(new TemplateString("Error 1")),
                        "[1]", List.of(
                                new TemplateString("Error 2"),
                                new TemplateString("Error 3")
                        )
                ));
    }
}
