package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

public class TryTest {
  @Test
  public void sePuedenColectarErroresDeMultiplesTries() {
    Try<String> resultado1 = Try.fallido(Collections.singletonList("Error 1"));
    Try<String> resultado2 = Try.fallido(Arrays.asList("Error 2", "Error 3"));
    Try<String> resultado3 = Try.exitoso("Resultado exitoso");

    assertThat(Try.collectErrors(Arrays.asList(resultado1, resultado2, resultado3)))
        .containsExactly("Error 1", "Error 2", "Error 3");
  }

  @Test
  public void sePuedeMapearUnTryDeUnTipoAOtroExitosamente() {
    Try<String> resultado = Try.exitoso("6");

    Try<Integer> resultadoMapeado = resultado.mapCatching(Integer::parseInt, "El valor no es numérico");

    assertThat(resultadoMapeado.getValor()).isEqualTo(6);
  }

  @Test
  public void sePuedeMapearUnTryDeUnTipoAOtroConError() {
    Try<String> resultado = Try.exitoso("6a");

    Try<Integer> resultadoMapeado = resultado.mapCatching(Integer::parseInt, "El valor no es numérico");

    assertThat(resultadoMapeado.getErrores()).containsExactly("El valor no es numérico");
  }

  @Test
  public void unTryFallidoNoEsAfectadoPorElMapeo() {
    Try<String> resultado = Try.fallido("Error");

    Try<Integer> resultadoMapeado = resultado.mapCatching(Integer::parseInt, "El valor no es numérico");

    assertThat(resultadoMapeado.getErrores()).containsExactly("Error");
  }

  @Test
  public void sePuedeParsearUnValorExitosamente() {
    Try<Integer> resultado = Try.desde(() -> Integer.parseInt("6"), "El valor no es numérico");

    assertThat(resultado.getValor()).isEqualTo(6);
  }

  @Test
  public void sePuedeParsearUnValorConError() {
    Try<Integer> resultado = Try.desde(() -> Integer.parseInt("6a"), "El valor no es numérico");

    assertThat(resultado.getErrores()).containsExactly("El valor no es numérico");
  }

  @Test
  public void noSePuedeObtenerElValorDeUnTryFallido() {
    Try<String> resultado = Try.fallido("Error");

    assertThatThrownBy(resultado::getValor).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  public void sePuedeFoldearUnEitherExitoso() {
    Try<String> resultado = Try.exitoso("6");

    Integer resultadoFoldeado = resultado.fold(errores -> 0, Integer::parseInt);

    assertThat(resultadoFoldeado).isEqualTo(6);
  }

  @Test
  public void sePuedeFoldearUnEitherFallido() {
    Try<String> resultado = Try.fallido("Error");

    Integer resultadoFoldeado = resultado.fold(errores -> 0, Integer::parseInt);

    assertThat(resultadoFoldeado).isEqualTo(0);
  }

  @Test
  public void sePuedeAplanarUnEitherExitoso() {
    Try<String> resultado = Try.exitoso("6");

    Try<Integer> resultadoAplanado = resultado.flatMap(valor -> Try.exitoso(Integer.parseInt(valor)));

    assertThat(resultadoAplanado.getValor()).isEqualTo(6);
  }

  @Test
  public void sePuedeAplanarUnEitherExitosoAFallido() {
    Try<String> resultado = Try.exitoso("6a");

    Try<Integer> resultadoAplanado = resultado.flatMap(valor -> Try.fallido("Error"));

    assertThat(resultadoAplanado.getErrores()).containsExactly("Error");
  }

  @Test
  public void sePuedeAplanarUnEitherFallido() {
    Try<String> resultado = Try.fallido("Error");

    Try<Integer> resultadoAplanado = resultado.flatMap(valor -> Try.exitoso(Integer.parseInt(valor)));

    assertThat(resultadoAplanado.getErrores()).containsExactly("Error");
  }

  @Test
  public void sePuedeConcatenarResultadosExitosos() {
    Try<Integer> resultado1 = Try.exitoso(6);
    Try<Integer> resultado2 = Try.exitoso(7);

    Try<Integer> resultadoConcatenado = Try.merge(() -> resultado1.getValor() + resultado2.getValor(), resultado1, resultado2);

    assertThat(resultadoConcatenado.getValor()).isEqualTo(13);
  }

  @Test
  public void sePuedeConcatenarResultadosFallidos() {
    Try<Integer> resultado1 = Try.fallido("Error 1");
    Try<Integer> resultado2 = Try.fallido("Error 2");

    Try<Integer> resultadoConcatenado = Try.merge(() -> resultado1.getValor() + resultado2.getValor(), resultado1, resultado2);

    assertThat(resultadoConcatenado.getErrores()).containsExactly("Error 1", "Error 2");
  }

  @Test
  public void sePuedeObtenerUnEitherDesdeUnaFuncionQueDevuelveOptional() {
    Try<String> resultado = Try.exitoso(Arrays.asList("1", "2", "3"))
        .flatMapCatching(list -> list.stream().findFirst(), "No se encontró el valor");

    assertThat(resultado.getValor()).isEqualTo("1");
  }

  @Test
  public void sePuedeObtenerUnEitherDesdeUnaFuncionQueDevuelveOptionalConError() {
    Try<Object> resultado = Try.exitoso(Collections.emptyList())
        .flatMapCatching(list -> list.stream().findFirst(), "No se encontró el valor");

    assertThat(resultado.getErrores()).containsExactly("No se encontró el valor");
  }

  @Test
  public void sePuedeFiltrarUnEitherExitoso() {
    Try<String> resultado = Try.exitoso("6");

    Try<String> resultadoFiltrado = resultado.filter(valor -> valor.equals("6"), "El valor no es 6");

    assertThat(resultadoFiltrado.getValor()).isEqualTo("6");
  }

  @Test
  public void sePuedeFiltrarUnEitherExitosoConError() {
    Try<String> resultado = Try.exitoso("6");

    Try<String> resultadoFiltrado = resultado.filter(valor -> valor.equals("7"), "El valor no es 6");

    assertThat(resultadoFiltrado.getErrores()).containsExactly("El valor no es 6");
  }

  @Test
  public void sePuedeFiltrarUnEitherFallido() {
    Try<String> resultado = Try.fallido("Error");

    Try<String> resultadoFiltrado = resultado.filter(valor -> valor.equals("6"), "El valor no es 6");

    assertThat(resultadoFiltrado.getErrores()).containsExactly("Error");
  }
}