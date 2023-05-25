package io.github.raniagus.javalidation.util;

import io.github.raniagus.javalidation.ErrorCode;

public enum ErrorCodes implements ErrorCode {
  ERROR_1("Error 1"),
  ERROR_2("Error 2"),
  ERROR_3("Error 3"),
  NUMBER_FORMAT_ERROR("El valor no es numérico"),
  VALUE_NOT_FOUND("No se encontró el valor"),
  UNEXPECTED_VALUE("Valor inesperado"),
  STRING_LENGTH_3_ERROR("El valor debe tener más de 3 caracteres"),
  STRING_LENGTH_5_ERROR("El valor debe tener más de 5 caracteres"),
  STRING_LENGTH_7_ERROR("El valor debe tener más de 7 caracteres"),
  ;

  private final String message;

  ErrorCodes(String message) {
    this.message = message;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
