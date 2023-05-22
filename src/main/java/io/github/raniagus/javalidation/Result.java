package io.github.raniagus.javalidation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Result<T> {
  boolean isSuccess();
  T getValue();
  List<ValidationException> getErrors();
  Result<T> filter(Predicate<T> predicate, ErrorCode errorCode);
  <R> Result<R> map(Function<T, R> function);
  <R> Result<R> mapCatching(Function<T, R> function, ErrorCode errorCode);
  <R> Result<R> flatMap(Function<T, Result<R>> function);
  <R> Result<R> flatMapCatching(Function<T, Optional<R>> function, ErrorCode errorCode);
  <R> R fold(Function<List<ValidationException>, R> onError, Function<T, R> onSuccess);

  static <T> Result<T> from(Supplier<T> supplier, ErrorCode errorCode) {
    try {
      return success(supplier.get());
    } catch (Exception e) {
      return failure(new ValidationException(errorCode, e));
    }
  }

  static <T> Result<T> success(T valor) {
    return new Success<>(valor);
  }

  static <T> Result<T> failure(List<ValidationException> errors) {
    return new Failure<>(errors);
  }

  static <T> Result<T> failure(ValidationException error) {
    return failure(Collections.singletonList(error));
  }

  static <T> List<ValidationException> collectErrors(List<Result<T>> results) {
    return results.stream()
        .flatMap(t -> t.getErrors().stream())
        .collect(Collectors.toList());
  }

  static <T> Result<T> merge(Supplier<T> valorExitoso, Result<?>... results) {
    List<ValidationException> errores = Stream.of(results)
        .flatMap(t -> t.getErrors().stream())
        .collect(Collectors.toList());

    return errores.isEmpty()
        ? Result.success(valorExitoso.get())
        : Result.failure(errores);
  }
}
