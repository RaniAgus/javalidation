package io.github.raniagus.javalidation;

public interface ErrorCode {
  String getCode();
  String getMessage();

  default ValidationException toException() {
    return new ValidationException(this);
  }
}
