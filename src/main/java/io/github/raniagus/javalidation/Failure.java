package io.github.raniagus.javalidation;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Failure<T> implements Result<T> {
  private final List<ValidationException> errors;

  protected Failure(List<ValidationException> errors) {
    this.errors = errors;
  }

  @Override
  public boolean isSuccess() {
    return false;
  }

  @Override
  public T getValue() {
    throw new NoSuchElementException(errors.stream()
        .map(ValidationException::getMessage)
        .collect(Collectors.joining(", ")), errors.get(0));
  }

  @Override
  public List<ValidationException> getErrors() {
    return errors;
  }

  @Override
  public Result<T> filter(Predicate<T> predicate, ErrorCode errorCode) {
    return this;
  }

  @Override
  public <R> Result<R> flatMap(Function<T, Result<R>> function) {
    return Result.failure(errors);
  }

  @Override
  public <R> Result<R> map(Function<T, R> function) {
    return Result.failure(errors);
  }

  @Override
  public <R> Result<R> mapCatching(Function<T, R> function, ErrorCode error) {
    return Result.failure(errors);
  }

  @Override
  public <R> Result<R> flatMapCatching(Function<T, Optional<R>> function, ErrorCode error) {
    return Result.failure(errors);
  }

  @Override
  public <R> R fold(Function<List<ValidationException>, R> onFailure, Function<T, R> onSuccess) {
    return onFailure.apply(errors);
  }
}
