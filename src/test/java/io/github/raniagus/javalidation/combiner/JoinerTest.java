package io.github.raniagus.javalidation.combiner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.util.ErrorStrings;
import io.github.raniagus.javalidation.util.Person;
import org.junit.jupiter.api.Test;

class JoinerTest {
    @Test
    void sePuedeFusionarDosResultadosExitosos() {
        Result<String> resultado1 = Result.ok("Agustin");
        Result<Integer> resultado2 = Result.ok(23);

        Result<Person> resultadoConcatenado = resultado1
                .and(resultado2)
                .combine(Person::new);

        assertThat(resultadoConcatenado.getOrThrow()).isEqualTo(new Person("Agustin", 23));
    }

    @Test
    void sePuedeFusionarDosResultadosFallidos() {
        Result<String> resultado1 = Result.err(ErrorStrings.ERROR_1);
        Result<Integer> resultado2 = Result.err(ErrorStrings.ERROR_2);

        Result<Person> resultadoConcatenado = resultado1
                .and(resultado2)
                .combine(Person::new);

        assertThat(resultadoConcatenado.getErrors())
                .extracting(ValidationErrors::rootErrors)
                .asInstanceOf(LIST)
                .containsExactly("Error 1", "Error 2");
    }

    @Test
    void sePuedeFusionarTresResultadosExitosos() {
        Result<Integer> resultado1 = Result.ok(6);
        Result<Integer> resultado2 = Result.ok(7);
        Result<Integer> resultado3 = Result.ok(8);

        Result<Integer> resultadoConcatenado = resultado1
                .and(resultado2)
                .and(resultado3)
                .combine((a, b, c) -> a + b + c);

        assertThat(resultadoConcatenado.getOrThrow()).isEqualTo(21);
    }

    @Test
    void sePuedeFusionarTresResultadosFallidos() {
        Result<Integer> resultado1 = Result.err(ErrorStrings.ERROR_1);
        Result<Integer> resultado2 = Result.err(ErrorStrings.ERROR_2);
        Result<Integer> resultado3 = Result.err(ErrorStrings.ERROR_3);

        Result<Integer> resultadoConcatenado = resultado1
                .and(resultado2)
                .and(resultado3)
                .combine((a, b, c) -> a + b + c);

        assertThat(resultadoConcatenado.getErrors())
                .extracting(ValidationErrors::rootErrors)
                .asInstanceOf(LIST)
                .containsExactly("Error 1", "Error 2", "Error 3");
    }

    @Test
    void sePuedeFusionarCuatroResultadosExitosos() {
        Result<Integer> resultado1 = Result.ok(6);
        Result<Integer> resultado2 = Result.ok(7);
        Result<Integer> resultado3 = Result.ok(8);
        Result<Integer> resultado4 = Result.ok(9);

        Result<Integer> resultadoConcatenado = resultado1
                .and(resultado2)
                .and(resultado3)
                .and(resultado4)
                .combine((a, b, c, d) -> a + b + c + d);

        assertThat(resultadoConcatenado.getOrThrow()).isEqualTo(30);
    }

    @Test
    void sePuedeFusionarCuatroResultadosFallidos() {
        Result<Integer> resultado1 = Result.err(ErrorStrings.ERROR_1);
        Result<Integer> resultado2 = Result.err(ErrorStrings.ERROR_2);
        Result<Integer> resultado3 = Result.err(ErrorStrings.ERROR_3);
        Result<Integer> resultado4 = Result.err(ErrorStrings.ERROR_4);

        Result<Integer> resultadoConcatenado = resultado1
                .and(resultado2)
                .and(resultado3)
                .and(resultado4)
                .combine((a, b, c, d) -> a + b + c + d);

        assertThat(resultadoConcatenado.getErrors())
                .extracting(ValidationErrors::rootErrors)
                .asInstanceOf(LIST)
                .containsExactly("Error 1", "Error 2", "Error 3", "Error 4");
    }

    @Test
    void sePuedeFusionarCincoResultadosExitosos() {
        Result<Integer> resultado1 = Result.ok(6);
        Result<Integer> resultado2 = Result.ok(7);
        Result<Integer> resultado3 = Result.ok(8);
        Result<Integer> resultado4 = Result.ok(9);
        Result<Integer> resultado5 = Result.ok(10);

        Result<Integer> resultadoConcatenado = resultado1
                .and(resultado2)
                .and(resultado3)
                .and(resultado4)
                .and(resultado5)
                .combine((a, b, c, d, e) -> a + b + c + d + e);

        assertThat(resultadoConcatenado.getOrThrow()).isEqualTo(40);
    }

    @Test
    void sePuedeFusionarCincoResultadosFallidos() {
        Result<Integer> resultado1 = Result.err(ErrorStrings.ERROR_1);
        Result<Integer> resultado2 = Result.err(ErrorStrings.ERROR_2);
        Result<Integer> resultado3 = Result.err(ErrorStrings.ERROR_3);
        Result<Integer> resultado4 = Result.err(ErrorStrings.ERROR_4);
        Result<Integer> resultado5 = Result.err(ErrorStrings.ERROR_5);

        Result<Integer> resultadoConcatenado = resultado1
                .and(resultado2)
                .and(resultado3)
                .and(resultado4)
                .and(resultado5)
                .combine((a, b, c, d, e) -> a + b + c + d + e);

        assertThat(resultadoConcatenado.getErrors())
                .extracting(ValidationErrors::rootErrors)
                .asInstanceOf(LIST)
                .containsExactly("Error 1", "Error 2", "Error 3", "Error 4", "Error 5");
    }

    @Test
    void sePuedeFusionarSeisResultadosExitosos() {
        Result<Integer> resultado1 = Result.ok(6);
        Result<Integer> resultado2 = Result.ok(7);
        Result<Integer> resultado3 = Result.ok(8);
        Result<Integer> resultado4 = Result.ok(9);
        Result<Integer> resultado5 = Result.ok(10);
        Result<Integer> resultado6 = Result.ok(11);

        Result<Integer> resultadoConcatenado = resultado1
                .and(resultado2)
                .and(resultado3)
                .and(resultado4)
                .and(resultado5)
                .and(resultado6)
                .combine((a, b, c, d, e, f) -> a + b + c + d + e + f);

        assertThat(resultadoConcatenado.getOrThrow()).isEqualTo(51);
    }

    @Test
    void sePuedeFusionarSeisResultadosFallidos() {
        Result<Integer> resultado1 = Result.err(ErrorStrings.ERROR_1);
        Result<Integer> resultado2 = Result.err(ErrorStrings.ERROR_2);
        Result<Integer> resultado3 = Result.err(ErrorStrings.ERROR_3);
        Result<Integer> resultado4 = Result.err(ErrorStrings.ERROR_4);
        Result<Integer> resultado5 = Result.err(ErrorStrings.ERROR_5);
        Result<Integer> resultado6 = Result.err(ErrorStrings.ERROR_6);

        Result<Integer> resultadoConcatenado = resultado1
                .and(resultado2)
                .and(resultado3)
                .and(resultado4)
                .and(resultado5)
                .and(resultado6)
                .combine((a, b, c, d, e, f) -> a + b + c + d + e + f);

        assertThat(resultadoConcatenado.getErrors())
                .extracting(ValidationErrors::rootErrors)
                .asInstanceOf(LIST)
                .containsExactly("Error 1", "Error 2", "Error 3", "Error 4", "Error 5", "Error 6");
    }

    @Test
    void sePuedeFusionarSieteResultadosExitosos() {
        Result<Integer> resultado1 = Result.ok(6);
        Result<Integer> resultado2 = Result.ok(7);
        Result<Integer> resultado3 = Result.ok(8);
        Result<Integer> resultado4 = Result.ok(9);
        Result<Integer> resultado5 = Result.ok(10);
        Result<Integer> resultado6 = Result.ok(11);
        Result<Integer> resultado7 = Result.ok(12);

        Result<Integer> resultadoConcatenado = resultado1
                .and(resultado2)
                .and(resultado3)
                .and(resultado4)
                .and(resultado5)
                .and(resultado6)
                .and(resultado7)
                .combine((a, b, c, d, e, f, g) -> a + b + c + d + e + f + g);

        assertThat(resultadoConcatenado.getOrThrow()).isEqualTo(63);
    }

    @Test
    void sePuedeFusionarSieteResultadosFallidos() {
        Result<Integer> resultado1 = Result.err(ErrorStrings.ERROR_1);
        Result<Integer> resultado2 = Result.err(ErrorStrings.ERROR_2);
        Result<Integer> resultado3 = Result.err(ErrorStrings.ERROR_3);
        Result<Integer> resultado4 = Result.err(ErrorStrings.ERROR_4);
        Result<Integer> resultado5 = Result.err(ErrorStrings.ERROR_5);
        Result<Integer> resultado6 = Result.err(ErrorStrings.ERROR_6);
        Result<Integer> resultado7 = Result.err(ErrorStrings.ERROR_7);

        Result<Integer> resultadoConcatenado = resultado1
                .and(resultado2)
                .and(resultado3)
                .and(resultado4)
                .and(resultado5)
                .and(resultado6)
                .and(resultado7)
                .combine((a, b, c, d, e, f, g) -> a + b + c + d + e + f + g);

        assertThat(resultadoConcatenado.getErrors())
                .extracting(ValidationErrors::rootErrors)
                .asInstanceOf(LIST)
                .containsExactly("Error 1", "Error 2", "Error 3", "Error 4", "Error 5", "Error 6", "Error 7");
    }

    @Test
    void sePuedeFusionarOchoResultadosExitosos() {
        Result<Integer> resultado1 = Result.ok(6);
        Result<Integer> resultado2 = Result.ok(7);
        Result<Integer> resultado3 = Result.ok(8);
        Result<Integer> resultado4 = Result.ok(9);
        Result<Integer> resultado5 = Result.ok(10);
        Result<Integer> resultado6 = Result.ok(11);
        Result<Integer> resultado7 = Result.ok(12);
        Result<Integer> resultado8 = Result.ok(13);

        Result<Integer> resultadoConcatenado = resultado1
                .and(resultado2)
                .and(resultado3)
                .and(resultado4)
                .and(resultado5)
                .and(resultado6)
                .and(resultado7)
                .and(resultado8)
                .combine((a, b, c, d, e, f, g, h) -> a + b + c + d + e + f + g + h);

        assertThat(resultadoConcatenado.getOrThrow()).isEqualTo(76);
    }

    @Test
    void sePuedeFusionarOchoResultadosFallidos() {
        Result<Integer> resultado1 = Result.err(ErrorStrings.ERROR_1);
        Result<Integer> resultado2 = Result.err(ErrorStrings.ERROR_2);
        Result<Integer> resultado3 = Result.err(ErrorStrings.ERROR_3);
        Result<Integer> resultado4 = Result.err(ErrorStrings.ERROR_4);
        Result<Integer> resultado5 = Result.err(ErrorStrings.ERROR_5);
        Result<Integer> resultado6 = Result.err(ErrorStrings.ERROR_6);
        Result<Integer> resultado7 = Result.err(ErrorStrings.ERROR_7);
        Result<Integer> resultado8 = Result.err(ErrorStrings.ERROR_8);

        Result<Integer> resultadoConcatenado = resultado1
                .and(resultado2)
                .and(resultado3)
                .and(resultado4)
                .and(resultado5)
                .and(resultado6)
                .and(resultado7)
                .and(resultado8)
                .combine((a, b, c, d, e, f, g, h) -> a + b + c + d + e + f + g + h);

        assertThat(resultadoConcatenado.getErrors())
                .extracting(ValidationErrors::rootErrors)
                .asInstanceOf(LIST)
                .containsExactly("Error 1", "Error 2", "Error 3", "Error 4", "Error 5", "Error 6", "Error 7", "Error 8");
    }

    @Test
    void sePuedeFusionarNueveResultadosExitosos() {
        Result<Integer> resultado1 = Result.ok(6);
        Result<Integer> resultado2 = Result.ok(7);
        Result<Integer> resultado3 = Result.ok(8);
        Result<Integer> resultado4 = Result.ok(9);
        Result<Integer> resultado5 = Result.ok(10);
        Result<Integer> resultado6 = Result.ok(11);
        Result<Integer> resultado7 = Result.ok(12);
        Result<Integer> resultado8 = Result.ok(13);
        Result<Integer> resultado9 = Result.ok(14);

        Result<Integer> resultadoConcatenado = resultado1
                .and(resultado2)
                .and(resultado3)
                .and(resultado4)
                .and(resultado5)
                .and(resultado6)
                .and(resultado7)
                .and(resultado8)
                .and(resultado9)
                .combine((a, b, c, d, e, f, g, h, i) -> a + b + c + d + e + f + g + h + i);

        assertThat(resultadoConcatenado.getOrThrow()).isEqualTo(90);
    }

    @Test
    void sePuedeFusionarNueveResultadosFallidos() {
        Result<Integer> resultado1 = Result.err(ErrorStrings.ERROR_1);
        Result<Integer> resultado2 = Result.err(ErrorStrings.ERROR_2);
        Result<Integer> resultado3 = Result.err(ErrorStrings.ERROR_3);
        Result<Integer> resultado4 = Result.err(ErrorStrings.ERROR_4);
        Result<Integer> resultado5 = Result.err(ErrorStrings.ERROR_5);
        Result<Integer> resultado6 = Result.err(ErrorStrings.ERROR_6);
        Result<Integer> resultado7 = Result.err(ErrorStrings.ERROR_7);
        Result<Integer> resultado8 = Result.err(ErrorStrings.ERROR_8);
        Result<Integer> resultado9 = Result.err(ErrorStrings.ERROR_9);

        Result<Integer> resultadoConcatenado = resultado1
                .and(resultado2)
                .and(resultado3)
                .and(resultado4)
                .and(resultado5)
                .and(resultado6)
                .and(resultado7)
                .and(resultado8)
                .and(resultado9)
                .combine((a, b, c, d, e, f, g, h, i) -> a + b + c + d + e + f + g + h + i);

        assertThat(resultadoConcatenado.getErrors())
                .extracting(ValidationErrors::rootErrors)
                .asInstanceOf(LIST)
                .containsExactly("Error 1", "Error 2", "Error 3", "Error 4", "Error 5", "Error 6", "Error 7", "Error 8", "Error 9");
    }

    @Test
    void sePuedeFusionarDiezResultadosExitosos() {
        Result<Integer> resultado1 = Result.ok(6);
        Result<Integer> resultado2 = Result.ok(7);
        Result<Integer> resultado3 = Result.ok(8);
        Result<Integer> resultado4 = Result.ok(9);
        Result<Integer> resultado5 = Result.ok(10);
        Result<Integer> resultado6 = Result.ok(11);
        Result<Integer> resultado7 = Result.ok(12);
        Result<Integer> resultado8 = Result.ok(13);
        Result<Integer> resultado9 = Result.ok(14);
        Result<Integer> resultado10 = Result.ok(15);

        Result<Integer> resultadoConcatenado = resultado1
                .and(resultado2)
                .and(resultado3)
                .and(resultado4)
                .and(resultado5)
                .and(resultado6)
                .and(resultado7)
                .and(resultado8)
                .and(resultado9)
                .and(resultado10)
                .combine((a, b, c, d, e, f, g, h, i, j) -> a + b + c + d + e + f + g + h + i + j);

        assertThat(resultadoConcatenado.getOrThrow()).isEqualTo(105);
    }

    @Test
    void sePuedeFusionarDiezResultadosFallidos() {
        Result<Integer> resultado1 = Result.err(ErrorStrings.ERROR_1);
        Result<Integer> resultado2 = Result.err(ErrorStrings.ERROR_2);
        Result<Integer> resultado3 = Result.err(ErrorStrings.ERROR_3);
        Result<Integer> resultado4 = Result.err(ErrorStrings.ERROR_4);
        Result<Integer> resultado5 = Result.err(ErrorStrings.ERROR_5);
        Result<Integer> resultado6 = Result.err(ErrorStrings.ERROR_6);
        Result<Integer> resultado7 = Result.err(ErrorStrings.ERROR_7);
        Result<Integer> resultado8 = Result.err(ErrorStrings.ERROR_8);
        Result<Integer> resultado9 = Result.err(ErrorStrings.ERROR_9);
        Result<Integer> resultado10 = Result.err(ErrorStrings.ERROR_10);

        Result<Integer> resultadoConcatenado = resultado1
                .and(resultado2)
                .and(resultado3)
                .and(resultado4)
                .and(resultado5)
                .and(resultado6)
                .and(resultado7)
                .and(resultado8)
                .and(resultado9)
                .and(resultado10)
                .combine((a, b, c, d, e, f, g, h, i, j) -> a + b + c + d + e + f + g + h + i + j);


        assertThat(resultadoConcatenado.getErrors())
                .extracting(ValidationErrors::rootErrors)
                .asInstanceOf(LIST)
                .containsExactly("Error 1", "Error 2", "Error 3", "Error 4", "Error 5", "Error 6", "Error 7", "Error 8", "Error 9", "Error 10");
    }
}
