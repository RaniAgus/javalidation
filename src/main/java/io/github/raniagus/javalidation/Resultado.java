package io.github.raniagus.javalidation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Resultado<T> {
  boolean esExitoso();
  T getValor();
  List<String> getErrores();
  Resultado<T> filter(Predicate<T> predicado, String mensajeDeError);
  <R> Resultado<R> map(Function<T, R> function);
  <R> Resultado<R> mapCatching(Function<T, R> function, String error);
  <R> Resultado<R> flatMap(Function<T, Resultado<R>> function);
  <R> Resultado<R> flatMapCatching(Function<T, Optional<R>> function, String error);
  <R> R fold(Function<List<String>, R> error, Function<T, R> exito);

  static <T> Resultado<T> desde(Supplier<T> supplier, String error) {
    try {
      return exitoso(supplier.get());
    } catch (Exception e) {
      return fallido(error);
    }
  }

  static <T> Resultado<T> exitoso(T valor) {
    return new ResultadoExitoso<>(valor);
  }

  static <T> Resultado<T> fallido(List<String> errores) {
    return new ResultadoFallido<>(errores);
  }

  static <T> Resultado<T> fallido(String error) {
    return fallido(Collections.singletonList(error));
  }

  static <T> List<String> collectErrors(List<Resultado<T>> tries) {
    return tries.stream()
        .flatMap(t -> t.getErrores().stream())
        .collect(Collectors.toList());
  }

  static <T> Resultado<T> merge(Supplier<T> valorExitoso, Resultado<?>... resultados) {
    List<String> errores = Stream.of(resultados)
        .flatMap(t -> t.getErrores().stream())
        .collect(Collectors.toList());

    return errores.isEmpty()
        ? Resultado.exitoso(valorExitoso.get())
        : Resultado.fallido(errores);
  }
}
