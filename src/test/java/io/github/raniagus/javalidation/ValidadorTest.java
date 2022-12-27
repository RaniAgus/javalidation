package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ValidadorTest {
  @Test
  public void sePuedeChequearCuandoEsValido() {
    Validador<String> chequeador = new Validador<>("123456");
    chequeador.agregarValidacion((valor) -> valor.length() > 5, "El valor debe tener más de 5 caracteres");

    Resultado<String> resultado = chequeador.validar();

    assertThat(resultado.getValor()).isEqualTo("123456");
  }

  @Test
  public void sePuedeChequearCuandoEsInvalido() {
    Validador<String> chequeador = new Validador<>("123456")
        .agregarValidacion((valor) -> valor.length() > 7, "El valor debe tener más de 7 caracteres")
        .agregarValidacion((valor) -> valor.length() > 5, "El valor debe tener más de 5 caracteres");

    Resultado<String> resultado = chequeador.validar();

    assertThat(resultado.esExitoso()).isFalse();
    assertThat(resultado.getErrores()).containsExactlyInAnyOrder(
        "El valor debe tener más de 7 caracteres");
  }

  @Test
  public void sePuedeChequearCuandoEsInvalidoPorMasDeUnMotivo() {
    Validador<String> chequeador = new Validador<>("123456")
        .agregarValidacion((valor) -> valor.length() > 8, "El valor debe tener más de 7 caracteres")
        .agregarValidacion((valor) -> valor.length() > 6, "El valor debe tener más de 5 caracteres")
        .agregarValidacion((valor) -> valor.length() > 4, "El valor debe tener más de 3 caracteres");

    Resultado<String> resultado = chequeador.validar();

    assertThat(resultado.esExitoso()).isFalse();
    assertThat(resultado.getErrores()).containsExactlyInAnyOrder(
        "El valor debe tener más de 7 caracteres", "El valor debe tener más de 5 caracteres");
  }

}
