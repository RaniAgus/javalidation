package io.github.raniagus.javalidation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Validator<T> {
  private final List<Validation<T>> validaciones = new ArrayList<>();
  private final T valor;

  public Validator(T valor) {
    this.valor = valor;
  }

  public Validator<T> agregarValidacion(Predicate<T> chequeo, ErrorCode code) {
    validaciones.add(Validation.create(chequeo, code));
    return this;
  }

  public Validator<T> agregarValidaciones(List<Validation<T>> validaciones) {
    this.validaciones.addAll(validaciones);
    return this;
  }

  public Result<T> validar() {
    List<Result<T>> resultados = getResultados(valor);
    return resultados.stream().allMatch(Result::isSuccess)
        ? Result.success(valor) : Result.failure(Result.collectErrors(resultados));
  }

  private List<Result<T>> getResultados(T valor) {
    return validaciones.stream()
        .map(x -> x.validate(valor))
        .collect(Collectors.toList());
  }
}
