package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.raniagus.javalidation.util.ErrorCodes;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ValidatorTest {
  @Test
  void sePuedeChequearCuandoEsValido() {
    Validator<String> validador = new Validator<>("123456");
    validador.agregarValidacion((valor) -> valor.length() > 5, ErrorCodes.STRING_LENGTH_5_ERROR);

    Result<String> resultado = validador.validar();

    assertThat(resultado.getValue()).isEqualTo("123456");
  }

  @Test
  void sePuedeChequearCuandoEsInvalido() {
    Validator<String> validador = new Validator<>("123456")
        .agregarValidacion((valor) -> valor.length() > 7, ErrorCodes.STRING_LENGTH_7_ERROR)
        .agregarValidacion((valor) -> valor.length() > 5, ErrorCodes.STRING_LENGTH_5_ERROR);

    Result<String> resultado = validador.validar();

    assertThat(resultado.isSuccess()).isFalse();
    assertThat(resultado.getErrors().stream().map(Throwable::getMessage).collect(Collectors.toList()))
        .containsExactlyInAnyOrder("El valor debe tener más de 7 caracteres");
  }

  @Test
  void sePuedeChequearCuandoEsInvalidoPorMasDeUnMotivo() {
    Validator<String> chequeador = new Validator<>("12345")
        .agregarValidacion((valor) -> valor.length() > 7, ErrorCodes.STRING_LENGTH_7_ERROR)
        .agregarValidacion((valor) -> valor.length() > 5, ErrorCodes.STRING_LENGTH_5_ERROR)
        .agregarValidacion((valor) -> valor.length() > 3, ErrorCodes.STRING_LENGTH_3_ERROR);

    Result<String> resultado = chequeador.validar();

    assertThat(resultado.isSuccess()).isFalse();
    assertThat(resultado.getErrors().stream().map(Throwable::getMessage).collect(Collectors.toList()))
        .containsExactlyInAnyOrder("El valor debe tener más de 7 caracteres", "El valor debe tener más de 5 caracteres");
  }

}
