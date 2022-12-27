package io.github.raniagus.javalidation;

import java.util.function.Predicate;

public interface Validacion<T> extends Predicate<T> {
  String getMensajeDeError();

  default Try<T> validar(T valor) {
    return test(valor) ? Try.exitoso(valor) : Try.fallido(getMensajeDeError());
  }

  static <T> Validacion<T> create(Predicate<T> chequeo, String mensajeDeError) {
    return new Validacion<T>() {
      @Override
      public boolean test(T valor) {
        return chequeo.test(valor);
      }

      @Override
      public String getMensajeDeError() {
        return mensajeDeError;
      }
    };
  }
}
