package io.github.raniagus.javalidation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class Success<T> implements Result<T> {
  private final T value;

  protected Success(T value) {
    this.value = value;
  }

  @Override
  public boolean isSuccess() {
    return true;
  }

  @Override
  public T getValue() {
    return value;
  }

  @Override
  public List<ValidationException> getErrors() {
    return Collections.emptyList();
  }

  @Override
  public Result<T> filter(Predicate<T> predicate, ErrorCode errorCode) {
    return predicate.test(value) ? this : Result.failure(errorCode);
  }

  @Override
  public <R> Result<R> map(Function<T, R> function) {
    return Result.success(function.apply(value));
  }

  @Override
  public <R> Result<R> mapCatching(Function<T, R> function, ErrorCode errorCode) {
    return Result.of(() -> function.apply(value), errorCode);
  }

  @Override
  public <R> Result<R> flatMap(Function<T, Result<R>> function) {
    return function.apply(value);
  }

  @Override
  public <R> Result<R> flatMapCatching(Function<T, Optional<R>> function, ErrorCode errorCode) {
    return mapCatching(function, errorCode)
        .flatMap(o -> o
            .map(Result::success)
            .orElseGet(() -> Result.failure(errorCode))
        );
  }

  @Override
  public <R> R fold(Function<List<ValidationException>, R> onFailure, Function<T, R> onSuccess) {
    return onSuccess.apply(value);
  }
}
