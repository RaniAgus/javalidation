package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import io.github.raniagus.javalidation.util.ErrorCodes;
import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ResultTest {
  @Test
  void sePuedenColectarErroresDeMultiplesTries() {
    Result<String> resultado1 = Result.failure(Collections.singletonList(ErrorCodes.ERROR_1.toException()));
    Result<String> resultado2 = Result.failure(Arrays.asList(ErrorCodes.ERROR_2.toException(), ErrorCodes.ERROR_3.toException()));
    Result<String> resultado3 = Result.success("Resultado exitoso");

    assertThat(Result.collectErrors(Arrays.asList(resultado1, resultado2, resultado3)).stream().map(Throwable::getMessage))
        .containsExactly("Error 1", "Error 2", "Error 3");
  }

  @Test
  void sePuedeMapearUnTryDeUnTipoAOtroExitosamente() {
    Result<String> resultado = Result.success("6");

    Result<Integer> resultadoMapeado = resultado.mapCatching(Integer::parseInt, ErrorCodes.NUMBER_FORMAT_ERROR);

    assertThat(resultadoMapeado.getValue()).isEqualTo(6);
  }

  @Test
  void sePuedeMapearUnTryDeUnTipoAOtroConError() {
    Result<String> resultado = Result.success("6a");

    Result<Integer> resultadoMapeado = resultado.mapCatching(Integer::parseInt, ErrorCodes.NUMBER_FORMAT_ERROR);

    assertThat(resultadoMapeado.getErrors().stream().map(Throwable::getMessage).collect(Collectors.toList()))
        .containsExactly("El valor no es numérico");
  }

  @Test
  void unTryFallidoNoEsAfectadoPorElMapeo() {
    Result<String> resultado = Result.failure(ErrorCodes.ERROR_1.toException());

    Result<Integer> resultadoMapeado = resultado.mapCatching(Integer::parseInt, ErrorCodes.NUMBER_FORMAT_ERROR);

    assertThat(resultadoMapeado.getErrors().stream().map(Throwable::getMessage).collect(Collectors.toList()))
        .containsExactly("Error 1");
  }

  @Test
  void sePuedeParsearUnValorExitosamente() {
    Result<Integer> resultado = Result.from(() -> Integer.parseInt("6"), ErrorCodes.NUMBER_FORMAT_ERROR);

    assertThat(resultado.getValue()).isEqualTo(6);
  }

  @Test
  void sePuedeParsearUnValorConError() {
    Result<Integer> resultado = Result.from(() -> Integer.parseInt("6a"), ErrorCodes.NUMBER_FORMAT_ERROR);

    assertThat(resultado.getErrors().stream().map(Throwable::getMessage).collect(Collectors.toList()))
        .containsExactly("El valor no es numérico");
  }

  @Test
  void noSePuedeObtenerElValorDeUnTryFallido() {
    Result<String> resultado = Result.failure(ErrorCodes.ERROR_1.toException());

    assertThatThrownBy(resultado::getValue).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void sePuedeFoldearUnEitherExitoso() {
    Result<String> resultado = Result.success("6");

    Integer resultadoFoldeado = resultado.fold(errores -> 0, Integer::parseInt);

    assertThat(resultadoFoldeado).isEqualTo(6);
  }

  @Test
  void sePuedeFoldearUnEitherFallido() {
    Result<String> resultado = Result.failure(ErrorCodes.ERROR_1.toException());

    Integer resultadoFoldeado = resultado.fold(errores -> 0, Integer::parseInt);

    assertThat(resultadoFoldeado).isZero();
  }

  @Test
  void sePuedeAplanarUnEitherExitoso() {
    Result<String> resultado = Result.success("6");

    Result<Integer> resultadoAplanado = resultado.flatMap(valor -> Result.success(Integer.parseInt(valor)));

    assertThat(resultadoAplanado.getValue()).isEqualTo(6);
  }

  @Test
  void sePuedeAplanarUnEitherExitosoAFallido() {
    Result<String> resultado = Result.success("6a");

    Result<Integer> resultadoAplanado = resultado.flatMap(valor -> Result.failure(ErrorCodes.ERROR_1.toException()));

    assertThat(resultadoAplanado.getErrors().stream().map(Throwable::getMessage).collect(Collectors.toList()))
        .containsExactly("Error 1");
  }

  @Test
  void sePuedeAplanarUnEitherFallido() {
    Result<String> resultado = Result.failure(ErrorCodes.ERROR_1.toException());

    Result<Integer> resultadoAplanado = resultado.flatMap(valor -> Result.success(Integer.parseInt(valor)));

    assertThat(resultadoAplanado.getErrors().stream().map(Throwable::getMessage).collect(Collectors.toList()))
        .containsExactly("Error 1");
  }

  @Test
  void sePuedeConcatenarResultadosExitosos() {
    Result<Integer> resultado1 = Result.success(6);
    Result<Integer> resultado2 = Result.success(7);

    Result<Integer> resultadoConcatenado = Result.merge(() -> resultado1.getValue() + resultado2.getValue(), resultado1, resultado2);

    assertThat(resultadoConcatenado.getValue()).isEqualTo(13);
  }

  @Test
  void sePuedeConcatenarResultadosFallidos() {
    Result<Integer> resultado1 = Result.failure(ErrorCodes.ERROR_1.toException());
    Result<Integer> resultado2 = Result.failure(ErrorCodes.ERROR_2.toException());

    Result<Integer> resultadoConcatenado = Result.merge(() -> resultado1.getValue() + resultado2.getValue(), resultado1, resultado2);

    assertThat(resultadoConcatenado.getErrors().stream().map(Throwable::getMessage).collect(Collectors.toList()))
        .containsExactly("Error 1", "Error 2");
  }

  @Test
  void sePuedeObtenerUnEitherDesdeUnaFuncionQueDevuelveOptional() {
    Result<String> resultado = Result.success(Arrays.asList("1", "2", "3"))
        .flatMapCatching(list -> list.stream().findFirst(), ErrorCodes.VALUE_NOT_FOUND);

    assertThat(resultado.getValue()).isEqualTo("1");
  }

  @Test
  void sePuedeObtenerUnEitherDesdeUnaFuncionQueDevuelveOptionalConError() {
    Result<Object> resultado = Result.success(Collections.emptyList())
        .flatMapCatching(list -> list.stream().findFirst(), ErrorCodes.VALUE_NOT_FOUND);

    assertThat(resultado.getErrors().stream().map(Throwable::getMessage).collect(Collectors.toList()))
        .containsExactly("No se encontró el valor");
  }

  @Test
  void sePuedeFiltrarUnEitherExitoso() {
    Result<String> resultado = Result.success("6");

    Result<String> resultadoFiltrado = resultado.filter(valor -> valor.equals("6"), ErrorCodes.UNEXPECTED_VALUE);

    assertThat(resultadoFiltrado.getValue()).isEqualTo("6");
  }

  @Test
  void sePuedeFiltrarUnEitherExitosoConError() {
    Result<String> resultado = Result.success("6");

    Result<String> resultadoFiltrado = resultado.filter(valor -> valor.equals("7"), ErrorCodes.UNEXPECTED_VALUE);

    assertThat(resultadoFiltrado.getErrors().stream().map(Throwable::getMessage).collect(Collectors.toList()))
        .containsExactly("Valor inesperado");
  }

  @Test
  void sePuedeFiltrarUnEitherFallido() {
    Result<String> resultado = Result.failure(ErrorCodes.ERROR_1.toException());

    Result<String> resultadoFiltrado = resultado.filter(valor -> valor.equals("6"), ErrorCodes.UNEXPECTED_VALUE);

    assertThat(resultadoFiltrado.getErrors().stream().map(Throwable::getMessage).collect(Collectors.toList()))
        .containsExactly("Error 1");
  }
}
