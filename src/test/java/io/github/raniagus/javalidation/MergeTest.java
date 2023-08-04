package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.raniagus.javalidation.util.ErrorCodes;
import io.github.raniagus.javalidation.util.Person;
import org.junit.jupiter.api.Test;

class MergeTest {
  @Test
  void sePuedeFusionarDosResultadosExitosos() {
    Result<String> resultado1 = Result.success("Agustin");
    Result<Integer> resultado2 = Result.success(23);

    Result<Person> resultadoConcatenado = Result.join(
        resultado1,
        resultado2
    ).with(Person::new);

    assertThat(resultadoConcatenado.getValue()).isEqualTo(new Person("Agustin", 23));
  }

  @Test
  void sePuedeFusionarDosResultadosFallidos() {
    Result<String> resultado1 = Result.failure(ErrorCodes.ERROR_1);
    Result<Integer> resultado2 = Result.failure(ErrorCodes.ERROR_2);

    Result<Person> resultadoConcatenado = Result.join(
        resultado1,
        resultado2
    ).with(Person::new);

    assertThat(resultadoConcatenado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactly("ERROR_1", "ERROR_2");
  }

  @Test
  void sePuedeFusionarTresResultadosExitosos() {
    Result<Integer> resultado1 = Result.success(6);
    Result<Integer> resultado2 = Result.success(7);
    Result<Integer> resultado3 = Result.success(8);

    Result<Integer> resultadoConcatenado = Result.join(
        resultado1,
        resultado2,
        resultado3
    ).with((a, b, c) -> a + b + c);

    assertThat(resultadoConcatenado.getValue()).isEqualTo(21);
  }

  @Test
  void sePuedeFusionarTresResultadosFallidos() {
    Result<Integer> resultado1 = Result.failure(ErrorCodes.ERROR_1);
    Result<Integer> resultado2 = Result.failure(ErrorCodes.ERROR_2);
    Result<Integer> resultado3 = Result.failure(ErrorCodes.ERROR_3);

    Result<Integer> resultadoConcatenado = Result.join(
        resultado1,
        resultado2,
        resultado3
    ).with((a, b, c) -> a + b + c);

    assertThat(resultadoConcatenado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactly("ERROR_1", "ERROR_2", "ERROR_3");
  }

  @Test
  void sePuedeFusionarCuatroResultadosExitosos() {
    Result<Integer> resultado1 = Result.success(6);
    Result<Integer> resultado2 = Result.success(7);
    Result<Integer> resultado3 = Result.success(8);
    Result<Integer> resultado4 = Result.success(9);

    Result<Integer> resultadoConcatenado = Result.join(
        resultado1,
        resultado2,
        resultado3,
        resultado4
    ).with((a, b, c, d) -> a + b + c + d);

    assertThat(resultadoConcatenado.getValue()).isEqualTo(30);
  }

  @Test
  void sePuedeFusionarCuatroResultadosFallidos() {
    Result<Integer> resultado1 = Result.failure(ErrorCodes.ERROR_1);
    Result<Integer> resultado2 = Result.failure(ErrorCodes.ERROR_2);
    Result<Integer> resultado3 = Result.failure(ErrorCodes.ERROR_3);
    Result<Integer> resultado4 = Result.failure(ErrorCodes.ERROR_4);

    Result<Integer> resultadoConcatenado = Result.join(
        resultado1,
        resultado2,
        resultado3,
        resultado4
    ).with((a, b, c, d) -> a + b + c + d);

    assertThat(resultadoConcatenado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactly("ERROR_1", "ERROR_2", "ERROR_3", "ERROR_4");
  }

  @Test
  void sePuedeFusionarCincoResultadosExitosos() {
    Result<Integer> resultado1 = Result.success(6);
    Result<Integer> resultado2 = Result.success(7);
    Result<Integer> resultado3 = Result.success(8);
    Result<Integer> resultado4 = Result.success(9);
    Result<Integer> resultado5 = Result.success(10);

    Result<Integer> resultadoConcatenado = Result.join(
        resultado1,
        resultado2,
        resultado3,
        resultado4,
        resultado5
    ).with((a, b, c, d, e) -> a + b + c + d + e);

    assertThat(resultadoConcatenado.getValue()).isEqualTo(40);
  }

  @Test
  void sePuedeFusionarCincoResultadosFallidos() {
    Result<Integer> resultado1 = Result.failure(ErrorCodes.ERROR_1);
    Result<Integer> resultado2 = Result.failure(ErrorCodes.ERROR_2);
    Result<Integer> resultado3 = Result.failure(ErrorCodes.ERROR_3);
    Result<Integer> resultado4 = Result.failure(ErrorCodes.ERROR_4);
    Result<Integer> resultado5 = Result.failure(ErrorCodes.ERROR_5);

    Result<Integer> resultadoConcatenado = Result.join(
        resultado1,
        resultado2,
        resultado3,
        resultado4,
        resultado5
    ).with((a, b, c, d, e) -> a + b + c + d + e);

    assertThat(resultadoConcatenado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactly("ERROR_1", "ERROR_2", "ERROR_3", "ERROR_4", "ERROR_5");
  }

  @Test
  void sePuedeFusionarSeisResultadosExitosos() {
    Result<Integer> resultado1 = Result.success(6);
    Result<Integer> resultado2 = Result.success(7);
    Result<Integer> resultado3 = Result.success(8);
    Result<Integer> resultado4 = Result.success(9);
    Result<Integer> resultado5 = Result.success(10);
    Result<Integer> resultado6 = Result.success(11);

    Result<Integer> resultadoConcatenado = Result.join(
        resultado1,
        resultado2,
        resultado3,
        resultado4,
        resultado5,
        resultado6
    ).with((a, b, c, d, e, f) -> a + b + c + d + e + f);

    assertThat(resultadoConcatenado.getValue()).isEqualTo(51);
  }

  @Test
  void sePuedeFusionarSeisResultadosFallidos() {
    Result<Integer> resultado1 = Result.failure(ErrorCodes.ERROR_1);
    Result<Integer> resultado2 = Result.failure(ErrorCodes.ERROR_2);
    Result<Integer> resultado3 = Result.failure(ErrorCodes.ERROR_3);
    Result<Integer> resultado4 = Result.failure(ErrorCodes.ERROR_4);
    Result<Integer> resultado5 = Result.failure(ErrorCodes.ERROR_5);
    Result<Integer> resultado6 = Result.failure(ErrorCodes.ERROR_6);

    Result<Integer> resultadoConcatenado = Result.join(
        resultado1,
        resultado2,
        resultado3,
        resultado4,
        resultado5,
        resultado6
    ).with((a, b, c, d, e, f) -> a + b + c + d + e + f);

    assertThat(resultadoConcatenado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactly("ERROR_1", "ERROR_2", "ERROR_3", "ERROR_4", "ERROR_5", "ERROR_6");
  }

  @Test
  void sePuedeFusionarSieteResultadosExitosos() {
    Result<Integer> resultado1 = Result.success(6);
    Result<Integer> resultado2 = Result.success(7);
    Result<Integer> resultado3 = Result.success(8);
    Result<Integer> resultado4 = Result.success(9);
    Result<Integer> resultado5 = Result.success(10);
    Result<Integer> resultado6 = Result.success(11);
    Result<Integer> resultado7 = Result.success(12);

    Result<Integer> resultadoConcatenado = Result.join(
        resultado1,
        resultado2,
        resultado3,
        resultado4,
        resultado5,
        resultado6,
        resultado7
    ).with((a, b, c, d, e, f, g) -> a + b + c + d + e + f + g);

    assertThat(resultadoConcatenado.getValue()).isEqualTo(63);
  }

  @Test
  void sePuedeFusionarSieteResultadosFallidos() {
    Result<Integer> resultado1 = Result.failure(ErrorCodes.ERROR_1);
    Result<Integer> resultado2 = Result.failure(ErrorCodes.ERROR_2);
    Result<Integer> resultado3 = Result.failure(ErrorCodes.ERROR_3);
    Result<Integer> resultado4 = Result.failure(ErrorCodes.ERROR_4);
    Result<Integer> resultado5 = Result.failure(ErrorCodes.ERROR_5);
    Result<Integer> resultado6 = Result.failure(ErrorCodes.ERROR_6);
    Result<Integer> resultado7 = Result.failure(ErrorCodes.ERROR_7);

    Result<Integer> resultadoConcatenado = Result.join(
        resultado1,
        resultado2,
        resultado3,
        resultado4,
        resultado5,
        resultado6,
        resultado7
    ).with((a, b, c, d, e, f, g) -> a + b + c + d + e + f + g);

    assertThat(resultadoConcatenado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactly("ERROR_1", "ERROR_2", "ERROR_3", "ERROR_4", "ERROR_5", "ERROR_6", "ERROR_7");
  }

  @Test
  void sePuedeFusionarOchoResultadosExitosos() {
    Result<Integer> resultado1 = Result.success(6);
    Result<Integer> resultado2 = Result.success(7);
    Result<Integer> resultado3 = Result.success(8);
    Result<Integer> resultado4 = Result.success(9);
    Result<Integer> resultado5 = Result.success(10);
    Result<Integer> resultado6 = Result.success(11);
    Result<Integer> resultado7 = Result.success(12);
    Result<Integer> resultado8 = Result.success(13);

    Result<Integer> resultadoConcatenado = Result.join(
        resultado1,
        resultado2,
        resultado3,
        resultado4,
        resultado5,
        resultado6,
        resultado7,
        resultado8
    ).with((a, b, c, d, e, f, g, h) -> a + b + c + d + e + f + g + h);

    assertThat(resultadoConcatenado.getValue()).isEqualTo(76);
  }

  @Test
  void sePuedeFusionarOchoResultadosFallidos() {
    Result<Integer> resultado1 = Result.failure(ErrorCodes.ERROR_1);
    Result<Integer> resultado2 = Result.failure(ErrorCodes.ERROR_2);
    Result<Integer> resultado3 = Result.failure(ErrorCodes.ERROR_3);
    Result<Integer> resultado4 = Result.failure(ErrorCodes.ERROR_4);
    Result<Integer> resultado5 = Result.failure(ErrorCodes.ERROR_5);
    Result<Integer> resultado6 = Result.failure(ErrorCodes.ERROR_6);
    Result<Integer> resultado7 = Result.failure(ErrorCodes.ERROR_7);
    Result<Integer> resultado8 = Result.failure(ErrorCodes.ERROR_8);

    Result<Integer> resultadoConcatenado = Result.join(
        resultado1,
        resultado2,
        resultado3,
        resultado4,
        resultado5,
        resultado6,
        resultado7,
        resultado8
    ).with((a, b, c, d, e, f, g, h) -> a + b + c + d + e + f + g + h);

    assertThat(resultadoConcatenado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactly("ERROR_1", "ERROR_2", "ERROR_3", "ERROR_4", "ERROR_5", "ERROR_6", "ERROR_7", "ERROR_8");
  }

  @Test
  void sePuedeFusionarNueveResultadosExitosos() {
    Result<Integer> resultado1 = Result.success(6);
    Result<Integer> resultado2 = Result.success(7);
    Result<Integer> resultado3 = Result.success(8);
    Result<Integer> resultado4 = Result.success(9);
    Result<Integer> resultado5 = Result.success(10);
    Result<Integer> resultado6 = Result.success(11);
    Result<Integer> resultado7 = Result.success(12);
    Result<Integer> resultado8 = Result.success(13);
    Result<Integer> resultado9 = Result.success(14);

    Result<Integer> resultadoConcatenado = Result.join(
        resultado1,
        resultado2,
        resultado3,
        resultado4,
        resultado5,
        resultado6,
        resultado7,
        resultado8,
        resultado9
    ).with((a, b, c, d, e, f, g, h, i) -> a + b + c + d + e + f + g + h + i);

    assertThat(resultadoConcatenado.getValue()).isEqualTo(90);
  }

  @Test
  void sePuedeFusionarNueveResultadosFallidos() {
    Result<Integer> resultado1 = Result.failure(ErrorCodes.ERROR_1);
    Result<Integer> resultado2 = Result.failure(ErrorCodes.ERROR_2);
    Result<Integer> resultado3 = Result.failure(ErrorCodes.ERROR_3);
    Result<Integer> resultado4 = Result.failure(ErrorCodes.ERROR_4);
    Result<Integer> resultado5 = Result.failure(ErrorCodes.ERROR_5);
    Result<Integer> resultado6 = Result.failure(ErrorCodes.ERROR_6);
    Result<Integer> resultado7 = Result.failure(ErrorCodes.ERROR_7);
    Result<Integer> resultado8 = Result.failure(ErrorCodes.ERROR_8);
    Result<Integer> resultado9 = Result.failure(ErrorCodes.ERROR_9);

    Result<Integer> resultadoConcatenado = Result.join(
        resultado1,
        resultado2,
        resultado3,
        resultado4,
        resultado5,
        resultado6,
        resultado7,
        resultado8,
        resultado9
    ).with((a, b, c, d, e, f, g, h, i) -> a + b + c + d + e + f + g + h + i);

    assertThat(resultadoConcatenado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactly("ERROR_1", "ERROR_2", "ERROR_3", "ERROR_4", "ERROR_5", "ERROR_6", "ERROR_7", "ERROR_8", "ERROR_9");
  }

  @Test
  void sePuedeFusionarDiezResultadosExitosos() {
    Result<Integer> resultado1 = Result.success(6);
    Result<Integer> resultado2 = Result.success(7);
    Result<Integer> resultado3 = Result.success(8);
    Result<Integer> resultado4 = Result.success(9);
    Result<Integer> resultado5 = Result.success(10);
    Result<Integer> resultado6 = Result.success(11);
    Result<Integer> resultado7 = Result.success(12);
    Result<Integer> resultado8 = Result.success(13);
    Result<Integer> resultado9 = Result.success(14);
    Result<Integer> resultado10 = Result.success(15);

    Result<Integer> resultadoConcatenado = Result.join(
        resultado1,
        resultado2,
        resultado3,
        resultado4,
        resultado5,
        resultado6,
        resultado7,
        resultado8,
        resultado9,
        resultado10
    ).with((a, b, c, d, e, f, g, h, i, j) -> a + b + c + d + e + f + g + h + i + j);

    assertThat(resultadoConcatenado.getValue()).isEqualTo(105);
  }

  @Test
  void sePuedeFusionarDiezResultadosFallidos() {
    Result<Integer> resultado1 = Result.failure(ErrorCodes.ERROR_1);
    Result<Integer> resultado2 = Result.failure(ErrorCodes.ERROR_2);
    Result<Integer> resultado3 = Result.failure(ErrorCodes.ERROR_3);
    Result<Integer> resultado4 = Result.failure(ErrorCodes.ERROR_4);
    Result<Integer> resultado5 = Result.failure(ErrorCodes.ERROR_5);
    Result<Integer> resultado6 = Result.failure(ErrorCodes.ERROR_6);
    Result<Integer> resultado7 = Result.failure(ErrorCodes.ERROR_7);
    Result<Integer> resultado8 = Result.failure(ErrorCodes.ERROR_8);
    Result<Integer> resultado9 = Result.failure(ErrorCodes.ERROR_9);
    Result<Integer> resultado10 = Result.failure(ErrorCodes.ERROR_10);

    Result<Integer> resultadoConcatenado = Result.join(
        resultado1,
        resultado2,
        resultado3,
        resultado4,
        resultado5,
        resultado6,
        resultado7,
        resultado8,
        resultado9,
        resultado10
    ).with((a, b, c, d, e, f, g, h, i, j) -> a + b + c + d + e + f + g + h + i + j);

    assertThat(resultadoConcatenado.getErrors())
        .extracting(ValidationException::getCode)
        .containsExactly("ERROR_1", "ERROR_2", "ERROR_3", "ERROR_4", "ERROR_5", "ERROR_6", "ERROR_7", "ERROR_8", "ERROR_9", "ERROR_10");
  }
}
