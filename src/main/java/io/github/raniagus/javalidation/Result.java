package io.github.raniagus.javalidation;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
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

  static <T> Result<T> failure(ValidationException... errors) {
    return failure(List.of(errors));
  }

  static <T> Result<T> failure(ErrorCode... errorCodes) {
    return new Failure<>(Stream.of(errorCodes)
        .map(ValidationException::new)
        .toList()
    );
  }

  static <T> List<ValidationException> collectErrors(List<Result<T>> results) {
    return results.stream()
        .flatMap(t -> t.getErrors().stream())
        .toList();
  }

  @SafeVarargs
  static <T> List<ValidationException> collectErrors(Result<T>... results) {
    return collectErrors(List.of(results));
  }

  static <T> Result<T> merge(Supplier<T> valorExitoso, Result<?>... results) {
    List<ValidationException> errores = Stream.of(results)
        .flatMap(t -> t.getErrors().stream())
        .toList();

    return errores.isEmpty()
        ? Result.success(valorExitoso.get())
        : Result.failure(errores);
  }
}
