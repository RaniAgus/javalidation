package io.github.raniagus.javalidation;

import java.util.function.Predicate;

public interface Validation<T> extends Predicate<T>, ErrorCode {
  default Result<T> validate(T valor) {
    return test(valor) ? Result.success(valor) : Result.failure(new ValidationException(this));
  }

  static <T> Validation<T> create(Predicate<T> chequeo, ErrorCode errorCode) {
    return new Validation<T>() {
      @Override
      public boolean test(T valor) {
        return chequeo.test(valor);
      }

      @Override
      public String getCode() {
        return errorCode.getCode();
      }

      @Override
      public String getMessage() {
        return errorCode.getMessage();
      }
    };
  }
}
