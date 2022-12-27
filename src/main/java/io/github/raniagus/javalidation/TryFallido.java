package io.github.raniagus.javalidation;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class TryFallido<T> implements Try<T> {
  private List<String> errores;

  protected TryFallido(List<String> errores) {
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
  public Try<T> filter(Predicate<T> predicado, String mensajeDeError) {
    return this;
  }

  @Override
  public <R> Try<R> flatMap(Function<T, Try<R>> function) {
    return Try.fallido(errores);
  }

  @Override
  public <R> Try<R> map(Function<T, R> function) {
    return Try.fallido(errores);
  }

  @Override
  public <R> Try<R> mapCatching(Function<T, R> function, String error) {
    return Try.fallido(errores);
  }

  @Override
  public <R> Try<R> flatMapCatching(Function<T, Optional<R>> function, String error) {
    return Try.fallido(errores);
  }

  @Override
  public <R> R fold(Function<List<String>, R> error, Function<T, R> exito) {
    return error.apply(errores);
  }
}
