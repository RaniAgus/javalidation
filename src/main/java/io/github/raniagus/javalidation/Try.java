package io.github.raniagus.javalidation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Try<T> {
  boolean esExitoso();
  T getValor();
  List<String> getErrores();
  Try<T> filter(Predicate<T> predicado, String mensajeDeError);
  <R> Try<R> map(Function<T, R> function);
  <R> Try<R> mapCatching(Function<T, R> function, String error);
  <R> Try<R> flatMap(Function<T, Try<R>> function);
  <R> Try<R> flatMapCatching(Function<T, Optional<R>> function, String error);
  <R> R fold(Function<List<String>, R> error, Function<T, R> exito);

  static <T> Try<T> desde(Supplier<T> supplier, String error) {
    try {
      return exitoso(supplier.get());
    } catch (Exception e) {
      return fallido(error);
    }
  }

  static <T> Try<T> exitoso(T valor) {
    return new TryExitoso<>(valor);
  }

  static <T> Try<T> fallido(List<String> errores) {
    return new TryFallido<>(errores);
  }

  static <T> Try<T> fallido(String error) {
    return fallido(Collections.singletonList(error));
  }

  static <T> List<String> collectErrors(List<Try<T>> tries) {
    return tries.stream()
        .flatMap(t -> t.getErrores().stream())
        .collect(Collectors.toList());
  }

  static <T> Try<T> merge(Supplier<T> valorExitoso, Try<?>... tries) {
    List<String> errores = Stream.of(tries)
        .flatMap(t -> t.getErrores().stream())
        .collect(Collectors.toList());

    return errores.isEmpty()
        ? Try.exitoso(valorExitoso.get())
        : Try.fallido(errores);
  }
}
