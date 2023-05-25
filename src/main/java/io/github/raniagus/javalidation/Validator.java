package io.github.raniagus.javalidation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Validator<T> {
  private final List<Validation<T>> validations = new ArrayList<>();
  private final T value;

  public Validator(T value) {
    this.value = value;
  }

  public Validator<T> add(Predicate<T> check, ErrorCode code) {
    validations.add(Validation.create(check, code));
    return this;
  }

  public Validator<T> addAll(List<Validation<T>> validations) {
    this.validations.addAll(validations);
    return this;
  }

  @SafeVarargs
  public final Validator<T> addAll(Validation<T>... validations) {
    return this.addAll(List.of(validations));
  }

  public Result<T> validate() {
    List<Result<T>> results = getResults(value);
    return results.stream().allMatch(Result::isSuccess)
        ? Result.success(value) : Result.failure(Result.collectErrors(results));
  }

  private List<Result<T>> getResults(T value) {
    return validations.stream()
        .map(x -> x.validate(value))
        .toList();
  }
}
