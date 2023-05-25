package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.raniagus.javalidation.util.ErrorCodes;
import io.github.raniagus.javalidation.util.StringLengthValidation;
import org.junit.jupiter.api.Test;

class ValidatorTest {
  @Test
  void sePuedeChequearCuandoEsValido() {
    Validator<String> validador = new Validator<>("123456");
    validador.add((valor) -> valor.length() > 5, ErrorCodes.STRING_LENGTH_5_ERROR);

    Result<String> resultado = validador.validate();

    assertThat(resultado.getValue()).isEqualTo("123456");
  }

  @Test
  void sePuedeChequearCuandoEsInvalido() {
    Validator<String> validador = new Validator<>("123456")
        .add((valor) -> valor.length() > 7, ErrorCodes.STRING_LENGTH_7_ERROR)
        .add((valor) -> valor.length() > 5, ErrorCodes.STRING_LENGTH_5_ERROR);

    Result<String> resultado = validador.validate();

    assertThat(resultado.isSuccess()).isFalse();
    assertThat(resultado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactlyInAnyOrder("STRING_LENGTH_7_ERROR");
  }

  @Test
  void sePuedeChequearCuandoEsInvalidoPorMasDeUnMotivo() {
    Validator<String> validador = new Validator<>("12345")
        .addAll(new StringLengthValidation(7), new StringLengthValidation(5), new StringLengthValidation(3));

    Result<String> resultado = validador.validate();

    assertThat(resultado.isSuccess()).isFalse();
    assertThat(resultado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactlyInAnyOrder("STRING_LENGTH_7_ERROR", "STRING_LENGTH_5_ERROR");
  }

}
