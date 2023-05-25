package io.github.raniagus.javalidation.util;

import io.github.raniagus.javalidation.Validation;

public class StringLengthValidation implements Validation<String> {
  private final int length;

  public StringLengthValidation(int length) {
    this.length = length;
  }

  @Override
  public String name() {
    return "STRING_LENGTH_" + length + "_ERROR";
  }

  @Override
  public String getMessage() {
    return "El valor debe tener más de " + length + " caracteres";
  }

  @Override
  public boolean test(String s) {
    return s.length() > length;
  }
}
