package io.github.raniagus.javalidation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class ResultadoExitoso<T> implements Resultado<T> {
  private T valor;

  protected ResultadoExitoso(T valor) {
    this.valor = valor;
  }

  @Override
  public boolean esExitoso() {
    return true;
  }

  @Override
  public T getValor() {
    return valor;
  }

  @Override
  public List<String> getErrores() {
    return Collections.emptyList();
  }

  @Override
  public Resultado<T> filter(Predicate<T> predicado, String mensajeDeError) {
    return predicado.test(valor) ? this : Resultado.fallido(mensajeDeError);
  }

  @Override
  public <R> Resultado<R> map(Function<T, R> function) {
    return Resultado.exitoso(function.apply(valor));
  }

  @Override
  public <R> Resultado<R> mapCatching(Function<T, R> function, String error) {
    return Resultado.desde(() -> function.apply(valor), error);
  }

  @Override
  public <R> Resultado<R> flatMap(Function<T, Resultado<R>> function) {
    return function.apply(valor);
  }

  @Override
  public <R> Resultado<R> flatMapCatching(Function<T, Optional<R>> function, String error) {
    return mapCatching(function, error)
        .flatMap(o -> o.map(Resultado::exitoso).orElseGet(() -> Resultado.fallido(error)));
  }

  @Override
  public <R> R fold(Function<List<String>, R> error, Function<T, R> exito) {
    return exito.apply(valor);
  }
}
