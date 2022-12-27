package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

public class ResultadoTest {
  @Test
  public void sePuedenColectarErroresDeMultiplesTries() {
    Resultado<String> resultado1 = Resultado.fallido(Collections.singletonList("Error 1"));
    Resultado<String> resultado2 = Resultado.fallido(Arrays.asList("Error 2", "Error 3"));
    Resultado<String> resultado3 = Resultado.exitoso("Resultado exitoso");

    assertThat(Resultado.collectErrors(Arrays.asList(resultado1, resultado2, resultado3)))
        .containsExactly("Error 1", "Error 2", "Error 3");
  }

  @Test
  public void sePuedeMapearUnTryDeUnTipoAOtroExitosamente() {
    Resultado<String> resultado = Resultado.exitoso("6");

    Resultado<Integer> resultadoMapeado = resultado.mapCatching(Integer::parseInt, "El valor no es numérico");

    assertThat(resultadoMapeado.getValor()).isEqualTo(6);
  }

  @Test
  public void sePuedeMapearUnTryDeUnTipoAOtroConError() {
    Resultado<String> resultado = Resultado.exitoso("6a");

    Resultado<Integer> resultadoMapeado = resultado.mapCatching(Integer::parseInt, "El valor no es numérico");

    assertThat(resultadoMapeado.getErrores()).containsExactly("El valor no es numérico");
  }

  @Test
  public void unTryFallidoNoEsAfectadoPorElMapeo() {
    Resultado<String> resultado = Resultado.fallido("Error");

    Resultado<Integer> resultadoMapeado = resultado.mapCatching(Integer::parseInt, "El valor no es numérico");

    assertThat(resultadoMapeado.getErrores()).containsExactly("Error");
  }

  @Test
  public void sePuedeParsearUnValorExitosamente() {
    Resultado<Integer> resultado = Resultado.desde(() -> Integer.parseInt("6"), "El valor no es numérico");

    assertThat(resultado.getValor()).isEqualTo(6);
  }

  @Test
  public void sePuedeParsearUnValorConError() {
    Resultado<Integer> resultado = Resultado.desde(() -> Integer.parseInt("6a"), "El valor no es numérico");

    assertThat(resultado.getErrores()).containsExactly("El valor no es numérico");
  }

  @Test
  public void noSePuedeObtenerElValorDeUnTryFallido() {
    Resultado<String> resultado = Resultado.fallido("Error");

    assertThatThrownBy(resultado::getValor).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  public void sePuedeFoldearUnEitherExitoso() {
    Resultado<String> resultado = Resultado.exitoso("6");

    Integer resultadoFoldeado = resultado.fold(errores -> 0, Integer::parseInt);

    assertThat(resultadoFoldeado).isEqualTo(6);
  }

  @Test
  public void sePuedeFoldearUnEitherFallido() {
    Resultado<String> resultado = Resultado.fallido("Error");

    Integer resultadoFoldeado = resultado.fold(errores -> 0, Integer::parseInt);

    assertThat(resultadoFoldeado).isEqualTo(0);
  }

  @Test
  public void sePuedeAplanarUnEitherExitoso() {
    Resultado<String> resultado = Resultado.exitoso("6");

    Resultado<Integer> resultadoAplanado = resultado.flatMap(valor -> Resultado.exitoso(Integer.parseInt(valor)));

    assertThat(resultadoAplanado.getValor()).isEqualTo(6);
  }

  @Test
  public void sePuedeAplanarUnEitherExitosoAFallido() {
    Resultado<String> resultado = Resultado.exitoso("6a");

    Resultado<Integer> resultadoAplanado = resultado.flatMap(valor -> Resultado.fallido("Error"));

    assertThat(resultadoAplanado.getErrores()).containsExactly("Error");
  }

  @Test
  public void sePuedeAplanarUnEitherFallido() {
    Resultado<String> resultado = Resultado.fallido("Error");

    Resultado<Integer> resultadoAplanado = resultado.flatMap(valor -> Resultado.exitoso(Integer.parseInt(valor)));

    assertThat(resultadoAplanado.getErrores()).containsExactly("Error");
  }

  @Test
  public void sePuedeConcatenarResultadosExitosos() {
    Resultado<Integer> resultado1 = Resultado.exitoso(6);
    Resultado<Integer> resultado2 = Resultado.exitoso(7);

    Resultado<Integer> resultadoConcatenado = Resultado.merge(() -> resultado1.getValor() + resultado2.getValor(), resultado1, resultado2);

    assertThat(resultadoConcatenado.getValor()).isEqualTo(13);
  }

  @Test
  public void sePuedeConcatenarResultadosFallidos() {
    Resultado<Integer> resultado1 = Resultado.fallido("Error 1");
    Resultado<Integer> resultado2 = Resultado.fallido("Error 2");

    Resultado<Integer> resultadoConcatenado = Resultado.merge(() -> resultado1.getValor() + resultado2.getValor(), resultado1, resultado2);

    assertThat(resultadoConcatenado.getErrores()).containsExactly("Error 1", "Error 2");
  }

  @Test
  public void sePuedeObtenerUnEitherDesdeUnaFuncionQueDevuelveOptional() {
    Resultado<String> resultado = Resultado.exitoso(Arrays.asList("1", "2", "3"))
        .flatMapCatching(list -> list.stream().findFirst(), "No se encontró el valor");

    assertThat(resultado.getValor()).isEqualTo("1");
  }

  @Test
  public void sePuedeObtenerUnEitherDesdeUnaFuncionQueDevuelveOptionalConError() {
    Resultado<Object> resultado = Resultado.exitoso(Collections.emptyList())
        .flatMapCatching(list -> list.stream().findFirst(), "No se encontró el valor");

    assertThat(resultado.getErrores()).containsExactly("No se encontró el valor");
  }

  @Test
  public void sePuedeFiltrarUnEitherExitoso() {
    Resultado<String> resultado = Resultado.exitoso("6");

    Resultado<String> resultadoFiltrado = resultado.filter(valor -> valor.equals("6"), "El valor no es 6");

    assertThat(resultadoFiltrado.getValor()).isEqualTo("6");
  }

  @Test
  public void sePuedeFiltrarUnEitherExitosoConError() {
    Resultado<String> resultado = Resultado.exitoso("6");

    Resultado<String> resultadoFiltrado = resultado.filter(valor -> valor.equals("7"), "El valor no es 6");

    assertThat(resultadoFiltrado.getErrores()).containsExactly("El valor no es 6");
  }

  @Test
  public void sePuedeFiltrarUnEitherFallido() {
    Resultado<String> resultado = Resultado.fallido("Error");

    Resultado<String> resultadoFiltrado = resultado.filter(valor -> valor.equals("6"), "El valor no es 6");

    assertThat(resultadoFiltrado.getErrores()).containsExactly("Error");
  }
}