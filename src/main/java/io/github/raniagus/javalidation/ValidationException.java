package io.github.raniagus.javalidation;

public class ValidationException extends RuntimeException {
  protected final String code;

  public ValidationException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.code = errorCode.getCode();
  }

  public ValidationException(ErrorCode errorCode, Throwable cause) {
    super(errorCode.getMessage(), cause);
    this.code = errorCode.getCode();
  }

  public String getCode() {
    return code;
  }
}
