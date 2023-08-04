package io.github.raniagus.javalidation;

import io.github.raniagus.javalidation.forcomprehension.BiFor;
import io.github.raniagus.javalidation.forcomprehension.DecaFor;
import io.github.raniagus.javalidation.forcomprehension.HexFor;
import io.github.raniagus.javalidation.forcomprehension.NonaFor;
import io.github.raniagus.javalidation.forcomprehension.OctaFor;
import io.github.raniagus.javalidation.forcomprehension.PentaFor;
import io.github.raniagus.javalidation.forcomprehension.SeptaFor;
import io.github.raniagus.javalidation.forcomprehension.TetraFor;
import io.github.raniagus.javalidation.forcomprehension.TriFor;
import java.util.List;
import java.util.Optional;
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

  static <T> Result<T> of(Supplier<T> supplier, ErrorCode errorCode) {
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
    return failure(Stream.of(errorCodes).map(ValidationException::new).toList());
  }

  static <T> List<ValidationException> collectErrors(List<Result<T>> results) {
    return results.stream().flatMap(t -> t.getErrors().stream()).toList();
  }

  @SafeVarargs
  static <T> List<ValidationException> collectErrors(Result<T>... results) {
    return collectErrors(List.of(results));
  }

  static <A, B> BiFor<A, B> join(Result<A> result1, Result<B> result2) {
    return new BiFor<>(result1, result2);
  }

  static <A, B, C> TriFor<A, B, C> join(Result<A> result1, Result<B> result2, Result<C> result3) {
    return new TriFor<>(result1, result2, result3);
  }

  static <A, B, C, D> TetraFor<A, B, C, D> join(Result<A> result1, Result<B> result2, Result<C> result3, Result<D> result4) {
    return new TetraFor<>(result1, result2, result3, result4);
  }

  static <A, B, C, D, E> PentaFor<A, B, C, D, E> join(Result<A> result1, Result<B> result2, Result<C> result3, Result<D> result4, Result<E> result5) {
    return new PentaFor<>(result1, result2, result3, result4, result5);
  }

  static <A, B, C, D, E, F> HexFor<A, B, C, D, E, F> join(Result<A> result1, Result<B> result2, Result<C> result3, Result<D> result4, Result<E> result5, Result<F> result6) {
    return new HexFor<>(result1, result2, result3, result4, result5, result6);
  }

  static <A, B, C, D, E, F, G> SeptaFor<A, B, C, D, E, F, G> join(Result<A> result1, Result<B> result2, Result<C> result3, Result<D> result4, Result<E> result5, Result<F> result6, Result<G> result7) {
    return new SeptaFor<>(result1, result2, result3, result4, result5, result6, result7);
  }

  static <A, B, C, D, E, F, G, H> OctaFor<A, B, C, D, E, F, G, H> join(Result<A> result1, Result<B> result2, Result<C> result3, Result<D> result4, Result<E> result5, Result<F> result6, Result<G> result7, Result<H> result8) {
    return new OctaFor<>(result1, result2, result3, result4, result5, result6, result7, result8);
  }

  static <A, B, C, D, E, F, G, H, I> NonaFor<A, B, C, D, E, F, G, H, I> join(Result<A> result1, Result<B> result2, Result<C> result3, Result<D> result4, Result<E> result5, Result<F> result6, Result<G> result7, Result<H> result8, Result<I> result9) {
    return new NonaFor<>(result1, result2, result3, result4, result5, result6, result7, result8, result9);
  }

  static <A, B, C, D, E, F, G, H, I, J> DecaFor<A, B, C, D, E, F, G, H, I, J> join(Result<A> result1, Result<B> result2, Result<C> result3, Result<D> result4, Result<E> result5, Result<F> result6, Result<G> result7, Result<H> result8, Result<I> result9, Result<J> result10) {
    return new DecaFor<>(result1, result2, result3, result4, result5, result6, result7, result8, result9, result10);
  }

  static <R> Result<R> merge(Supplier<R> onSuccess, Result<?>... results) {
    List<ValidationException> errors = Stream.of(results).flatMap(t -> t.getErrors().stream()).toList();
    return errors.isEmpty() ? Result.success(onSuccess.get()) : Result.failure(errors);
  }
}
