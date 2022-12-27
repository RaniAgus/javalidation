package io.github.raniagus.javalidation;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class ResultadoFallido<T> implements Resultado<T> {
  private List<String> errores;

  protected ResultadoFallido(List<String> errores) {
    this.errores = errores;
  }

  @Override
  public boolean esExitoso() {
    return false;
  }

  @Override
  public T getValor() {
    throw new NoSuchElementException(String.join(", ", errores));
  }

  @Override
  public List<String> getErrores() {
    return errores;
  }

  @Override
  public Resultado<T> filter(Predicate<T> predicado, String mensajeDeError) {
    return this;
  }

  @Override
  public <R> Resultado<R> flatMap(Function<T, Resultado<R>> function) {
    return Resultado.fallido(errores);
  }

  @Override
  public <R> Resultado<R> map(Function<T, R> function) {
    return Resultado.fallido(errores);
  }

  @Override
  public <R> Resultado<R> mapCatching(Function<T, R> function, String error) {
    return Resultado.fallido(errores);
  }

  @Override
  public <R> Resultado<R> flatMapCatching(Function<T, Optional<R>> function, String error) {
    return Resultado.fallido(errores);
  }

  @Override
  public <R> R fold(Function<List<String>, R> error, Function<T, R> exito) {
    return error.apply(errores);
  }
}
