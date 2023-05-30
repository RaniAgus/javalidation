package io.github.raniagus.javalidation;

import java.util.function.Predicate;

public interface Validation<T> extends Predicate<T>, ErrorCode {
  default Result<T> validate(T valor) {
    return test(valor) ? Result.success(valor) : Result.failure(this);
  }

  static <T> Validation<T> create(Predicate<T> predicate, ErrorCode errorCode) {
    return new Validation<>() {
      @Override
      public boolean test(T valor) {
        return predicate.test(valor);
      }

      @Override
      public String name() {
        return errorCode.name();
      }

      @Override
      public String getMessage() {
        return errorCode.getMessage();
      }
    };
  }
}
