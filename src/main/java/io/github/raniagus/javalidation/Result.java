package io.github.raniagus.javalidation;

import io.github.raniagus.javalidation.function.DecaFunction;
import io.github.raniagus.javalidation.function.SeptaFunction;
import io.github.raniagus.javalidation.function.HexFunction;
import io.github.raniagus.javalidation.function.NonaFunction;
import io.github.raniagus.javalidation.function.OctaFunction;
import io.github.raniagus.javalidation.function.PentaFunction;
import io.github.raniagus.javalidation.function.TetraFunction;
import io.github.raniagus.javalidation.function.TriFunction;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface Result<T> {
  boolean isSuccess();
  T getValue();
  List<ValidationException> getErrors();
  Result<T> filter(Predicate<T> predicate, ErrorCode errorCode);
  <R> Result<R> map(Function<T, R> function);
  <R> Result<R> mapCatching(Function<T, R> function, ErrorCode errorCode);
  <R> Result<R> flatMap(Function<T, Result<R>> function);
  <R> Result<R> flatMapCatching(Function<T, Optional<R>> function, ErrorCode errorCode);
  <R> R fold(Function<List<ValidationException>, R> onFailure, Function<T, R> onSuccess);

  static <T> Result<T> from(Supplier<T> supplier, ErrorCode errorCode) {
    try {
      return success(supplier.get());
    } catch (Exception e) {
      return failure(new ValidationException(errorCode, e));
    }
  }

  static <T> Result<T> success(T valor) {
    return new Success<>(valor);
  }

  static <T> Result<T> failure(List<ValidationException> errors) {
    return new Failure<>(errors);
  }

  static <T> Result<T> failure(ValidationException... errors) {
    return failure(List.of(errors));
  }

  static <T> Result<T> failure(ErrorCode... errorCodes) {
    return new Failure<>(Stream.of(errorCodes)
        .map(ValidationException::new)
        .toList()
    );
  }

  static <T> List<ValidationException> collectErrors(List<Result<T>> results) {
    return results.stream()
        .flatMap(t -> t.getErrors().stream())
        .toList();
  }

  @SafeVarargs
  static <T> List<ValidationException> collectErrors(Result<T>... results) {
    return collectErrors(List.of(results));
  }

  static <A, B, R> Result<R> merge(Result<A> result1, Result<B> result2, BiFunction<A, B, R> onSuccess) {
    return merge(() -> onSuccess.apply(result1.getValue(), result2.getValue()), result1, result2);
  }

  static <A, B, C, R> Result<R> merge(Result<A> result1, Result<B> result2, Result<C> result3, TriFunction<A, B, C, R> onSuccess) {
    return merge(() -> onSuccess.apply(result1.getValue(), result2.getValue(), result3.getValue()), result1, result2, result3);
  }

  static <A, B, C, D, R> Result<R> merge(Result<A> result1, Result<B> result2, Result<C> result3, Result<D> result4, TetraFunction<A, B, C, D, R> onSuccess) {
    return merge(() -> onSuccess.apply(result1.getValue(), result2.getValue(), result3.getValue(), result4.getValue()), result1, result2, result3, result4);
  }

  static <A, B, C, D, E, R> Result<R> merge(Result<A> result1, Result<B> result2, Result<C> result3, Result<D> result4, Result<E> result5, PentaFunction<A, B, C, D, E, R> onSuccess) {
    return merge(() -> onSuccess.apply(result1.getValue(), result2.getValue(), result3.getValue(), result4.getValue(), result5.getValue()), result1, result2, result3, result4, result5);
  }

  static <A, B, C, D, E, F, R> Result<R> merge(Result<A> result1, Result<B> result2, Result<C> result3, Result<D> result4, Result<E> result5, Result<F> result6, HexFunction<A, B, C, D, E, F, R> onSuccess) {
    return merge(() -> onSuccess.apply(result1.getValue(), result2.getValue(), result3.getValue(), result4.getValue(), result5.getValue(), result6.getValue()), result1, result2, result3, result4, result5, result6);
  }

  static <A, B, C, D, E, F, G, R> Result<R> merge(Result<A> result1, Result<B> result2, Result<C> result3, Result<D> result4, Result<E> result5, Result<F> result6, Result<G> result7, SeptaFunction<A, B, C, D, E, F, G, R> onSuccess) {
    return merge(() -> onSuccess.apply(result1.getValue(), result2.getValue(), result3.getValue(), result4.getValue(), result5.getValue(), result6.getValue(), result7.getValue()), result1, result2, result3, result4, result5, result6, result7);
  }

  static <A, B, C, D, E, F, G, H, R> Result<R> merge(Result<A> result1, Result<B> result2, Result<C> result3, Result<D> result4, Result<E> result5, Result<F> result6, Result<G> result7, Result<H> result8, OctaFunction<A, B, C, D, E, F, G, H, R> onSuccess) {
    return merge(() -> onSuccess.apply(result1.getValue(), result2.getValue(), result3.getValue(), result4.getValue(), result5.getValue(), result6.getValue(), result7.getValue(), result8.getValue()), result1, result2, result3, result4, result5, result6, result7, result8);
  }

  static <A, B, C, D, E, F, G, H, I, R> Result<R> merge(Result<A> result1, Result<B> result2, Result<C> result3, Result<D> result4, Result<E> result5, Result<F> result6, Result<G> result7, Result<H> result8, Result<I> result9, NonaFunction<A, B, C, D, E, F, G, H, I, R> onSuccess) {
    return merge(() -> onSuccess.apply(result1.getValue(), result2.getValue(), result3.getValue(), result4.getValue(), result5.getValue(), result6.getValue(), result7.getValue(), result8.getValue(), result9.getValue()), result1, result2, result3, result4, result5, result6, result7, result8, result9);
  }

  static <A, B, C, D, E, F, G, H, I, J, R> Result<R> merge(Result<A> result1, Result<B> result2, Result<C> result3, Result<D> result4, Result<E> result5, Result<F> result6, Result<G> result7, Result<H> result8, Result<I> result9, Result<J> result10, DecaFunction<A, B, C, D, E, F, G, H, I, J, R> onSuccess) {
    return merge(() -> onSuccess.apply(result1.getValue(), result2.getValue(), result3.getValue(), result4.getValue(), result5.getValue(), result6.getValue(), result7.getValue(), result8.getValue(), result9.getValue(), result10.getValue()), result1, result2, result3, result4, result5, result6, result7, result8, result9, result10);
  }

  private static <R> Result<R> merge(Supplier<R> onSuccess, Result<?>... results) {
    List<ValidationException> errors = Stream.of(results).flatMap(t -> t.getErrors().stream()).toList();
    return errors.isEmpty() ? Result.success(onSuccess.get()) : Result.failure(errors);
  }
}
