package io.github.raniagus.javalidation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Validador<T> {
  private List<Validacion<T>> validaciones = new ArrayList<>();
  private T valor;

  public Validador(T valor) {
    this.valor = valor;
  }

  public Validador<T> agregarValidacion(Predicate<T> chequeo, String mensajeDeError) {
    validaciones.add(Validacion.create(chequeo, mensajeDeError));
    return this;
  }

  public Validador<T> agregarValidaciones(List<Validacion<T>> validaciones) {
    this.validaciones.addAll(validaciones);
    return this;
  }

  public Resultado<T> validar() {
    List<Resultado<T>> resultados = getResultados(valor);
    return resultados.stream().allMatch(Resultado::esExitoso)
        ? Resultado.exitoso(valor) : Resultado.fallido(Resultado.collectErrors(resultados));
  }

  private List<Resultado<T>> getResultados(T valor) {
    return validaciones.stream()
        .map(x -> x.validar(valor))
        .collect(Collectors.toList());
  }
}
