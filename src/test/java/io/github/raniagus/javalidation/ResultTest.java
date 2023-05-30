package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import io.github.raniagus.javalidation.util.ErrorCodes;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class ResultTest {
  @Test
  void sePuedenColectarErroresDeMultiplesTries() {
    Result<String> resultado1 = Result.failure(ErrorCodes.ERROR_1);
    Result<String> resultado2 = Result.failure(ErrorCodes.ERROR_2, ErrorCodes.ERROR_3);
    Result<String> resultado3 = Result.success("Resultado exitoso");

    assertThat(Result.collectErrors(resultado1, resultado2, resultado3))
        .extracting(ValidationException::getCode)
        .containsExactly("ERROR_1", "ERROR_2", "ERROR_3");
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

    assertThat(resultadoMapeado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactly("NUMBER_FORMAT_ERROR");
  }

  @Test
  void unTryFallidoNoEsAfectadoPorElMapeo() {
    Result<String> resultado = Result.failure(ErrorCodes.ERROR_1);

    Result<Integer> resultadoMapeado = resultado.mapCatching(Integer::parseInt, ErrorCodes.NUMBER_FORMAT_ERROR);

    assertThat(resultadoMapeado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactly("ERROR_1");
  }

  @Test
  void sePuedeParsearUnValorExitosamente() {
    Result<Integer> resultado = Result.of(() -> Integer.parseInt("6"), ErrorCodes.NUMBER_FORMAT_ERROR);

    assertThat(resultado.getValue()).isEqualTo(6);
  }

  @Test
  void sePuedeParsearUnValorConError() {
    Result<Integer> resultado = Result.of(() -> Integer.parseInt("6a"), ErrorCodes.NUMBER_FORMAT_ERROR);

    assertThat(resultado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactly("NUMBER_FORMAT_ERROR");
  }

  @Test
  void noSePuedeObtenerElValorDeUnTryFallido() {
    Result<String> resultado = Result.failure(ErrorCodes.ERROR_1);

    assertThatThrownBy(resultado::getValue)
        .isInstanceOf(NoSuchElementException.class)
        .extracting(Throwable::getMessage)
        .isEqualTo("Error 1");
  }

  @Test
  void sePuedeFoldearUnEitherExitoso() {
    Result<String> resultado = Result.success("6");

    Integer resultadoFoldeado = resultado.fold(errores -> 0, Integer::parseInt);

    assertThat(resultadoFoldeado).isEqualTo(6);
  }

  @Test
  void sePuedeFoldearUnEitherFallido() {
    Result<String> resultado = Result.failure(ErrorCodes.ERROR_1);

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

    Result<Integer> resultadoAplanado = resultado.flatMap(valor -> Result.failure(ErrorCodes.ERROR_1));

    assertThat(resultadoAplanado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactly("ERROR_1");
  }

  @Test
  void sePuedeAplanarUnEitherFallido() {
    Result<String> resultado = Result.failure(ErrorCodes.ERROR_1);

    Result<Integer> resultadoAplanado = resultado.flatMap(valor -> Result.success(Integer.parseInt(valor)));

    assertThat(resultadoAplanado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactly("ERROR_1");
  }

  @Test
  void sePuedeTransformarUnEitherExitoso() {
    Result<Integer> resultado = Result.success(6);

    Result<Integer> resultadoTransformado = resultado.map(valor -> valor * 2);

    assertThat(resultadoTransformado.getValue()).isEqualTo(12);
  }

  @Test
  void sePuedeTransformarUnEitherFallido() {
    Result<Integer> resultado = Result.failure(ErrorCodes.ERROR_1);

    Result<Integer> resultadoTransformado = resultado.map(valor -> valor * 2);

    assertThat(resultadoTransformado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactly("ERROR_1");
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

    assertThat(resultado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactly("VALUE_NOT_FOUND");
  }

  @Test
  void sePuedeObtenerUnEitherFallidoDesdeUnaFuncionQueDevuelveOptional() {
    Result<String> resultado = Result.<List<String>>failure(ErrorCodes.ERROR_1)
        .flatMapCatching(list -> list.stream().findFirst(), ErrorCodes.VALUE_NOT_FOUND);

    assertThat(resultado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactly("ERROR_1");
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

    assertThat(resultadoFiltrado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactly("UNEXPECTED_VALUE");
  }

  @Test
  void sePuedeFiltrarUnEitherFallido() {
    Result<String> resultado = Result.failure(ErrorCodes.ERROR_1);

    Result<String> resultadoFiltrado = resultado.filter(valor -> valor.equals("6"), ErrorCodes.UNEXPECTED_VALUE);

    assertThat(resultadoFiltrado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactly("ERROR_1");
  }
}
