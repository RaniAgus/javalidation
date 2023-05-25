package io.github.raniagus.javalidation.util;

import io.github.raniagus.javalidation.ErrorCode;

public enum ErrorCodes implements ErrorCode {
  ERROR_1("Error 1"),
  ERROR_2("Error 2"),
  ERROR_3("Error 3"),
  ERROR_4("Error 4"),
  ERROR_5("Error 5"),
  ERROR_6("Error 6"),
  ERROR_7("Error 7"),
  ERROR_8("Error 8"),
  ERROR_9("Error 9"),
  ERROR_10("Error 10"),
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
