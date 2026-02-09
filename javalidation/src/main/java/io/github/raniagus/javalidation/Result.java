package io.github.raniagus.javalidation;

import io.github.raniagus.javalidation.combiner.ResultCombiner2;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * Represents the result of a validation operation that may succeed with a value or fail with accumulated errors.
 * <p>
 * This is a sealed interface implementing the Railway-Oriented Programming pattern with two possible states:
 * <ul>
 *   <li>{@link Ok} - represents a successful result containing a value of type {@code T}</li>
 *   <li>{@link Err} - represents a failed result containing accumulated {@link ValidationErrors}</li>
 * </ul>
 * <p>
 * Unlike traditional exception-based error handling, {@code Result} enables:
 * <ul>
 *   <li><b>Error accumulation</b>: Multiple validation errors can be collected rather than failing fast</li>
 *   <li><b>Type-safe error handling</b>: Compiler ensures both success and failure cases are handled</li>
 *   <li><b>Functional composition</b>: Results can be chained, mapped, and combined using monadic operations</li>
 * </ul>
 * <p>
 * <b>Error Channel Design:</b>
 * <p>
 * This library implements a dedicated error channel for well-known validation failures, similar to Effect.TS.
 * Methods like {@link #map(Function)} and {@link #flatMap(Function)} automatically catch
 * {@link JavalidationException} and convert it to {@link Err}, enabling seamless error handling:
 * <pre>{@code
 * Result<User> user = Result.ok(userId)
 *     .map(id -> findUserOrThrow(id))  // JavalidationException caught → Err
 *     .flatMap(u -> validateUser(u));  // Continues on error track
 * }</pre>
 * <p>
 * This creates two distinct error handling paths:
 * <ul>
 *   <li><b>Error Channel (JavalidationException)</b>: Well-known validation errors from this library's API.
 *       These are caught, converted to {@link Err}, and safe to return to API consumers as structured
 *       validation feedback.</li>
 *   <li><b>Exception Propagation (all other exceptions)</b>: Unexpected errors like {@link NullPointerException},
 *       {@link IllegalStateException}, etc. These propagate normally, should be logged at application boundaries,
 *       and typically result in generic 500 error responses to external clients.</li>
 * </ul>
 * <p>
 * This design provides the best of both worlds: functional error accumulation for expected validation failures,
 * while preserving fail-fast behavior for programming errors and bugs.
 * <p>
 * <b>Basic usage:</b>
 * <pre>{@code
 * Result<Integer> result = Result.ok(42);
 * int value = result.getOrThrow();  // 42
 *
 * Result<String> error = Result.err("Invalid input");
 * error.getOrThrow();  // throws JavalidationException
 * }</pre>
 * <p>
 * <b>Functional operations:</b>
 * <pre>{@code
 * Result.ok(10)
 *     .map(x -> x * 2)                    // Ok(20)
 *     .filter(x -> x > 15, "Too small")   // Ok(20)
 *     .flatMap(x -> validateAge(x))       // chains validation
 *     .fold(
 *         success -> "Valid: " + success,
 *         errors -> "Errors: " + errors
 *     );
 * }</pre>
 * <p>
 * <b>Combining multiple results (applicative style):</b>
 * <pre>{@code
 * Result<Person> person = Result.ok("Alice")
 *     .and(Result.ok(30))
 *     .and(Result.ok("alice@example.com"))
 *     .combine((name, age, email) -> new Person(name, age, email));
 * }</pre>
 * <p>
 * This sealed interface ensures exhaustive pattern matching when using Java's switch expressions:
 * <pre>{@code
 * String message = switch (result) {
 *     case Result.Ok(var value) -> "Success: " + value;
 *     case Result.Err(var errors) -> "Errors: " + errors;
 * };
 * }</pre>
 *
 * @param <T> the type of the success value, may be {@link Nullable}
 * @see Validation
 * @see ValidationErrors
 * @see ResultCombiner2
 * @see JavalidationException
 */
public sealed interface Result<T extends @Nullable Object> {
    /**
     * Represents a successful validation result containing a value.
     * <p>
     * Example:
     * <pre>{@code
     * Result<String> result = Result.ok("success");
     * String value = result.getOrThrow();  // "success"
     * }</pre>
     *
     * @param value the success value, may be {@code null}
     * @param <T> the type of the success value
     */
    record Ok<T extends @Nullable Object>(T value) implements Result<T> {
    }

    /**
     * Represents a failed validation result containing accumulated errors.
     * <p>
     * Example:
     * <pre>{@code
     * Result<String> result = Result.err("Invalid input");
     * result.getOrThrow();  // throws JavalidationException
     * ValidationErrors errors = result.getErrors();  // get accumulated errors
     * }</pre>
     *
     * @param errors the accumulated validation errors
     * @param <T> the type parameter (phantom type, as no value exists)
     */
    record Err<T extends @Nullable Object>(ValidationErrors errors) implements Result<T> {
    }

    /**
     * Extracts the success value or throws an exception if this result is an error.
     * <p>
     * This is the primary way to unwrap a {@code Result} when you're confident it's successful
     * or want to propagate validation errors as exceptions.
     * <p>
     * Example:
     * <pre>{@code
     * Result<Integer> ok = Result.ok(42);
     * int value = ok.getOrThrow();  // 42
     *
     * Result<Integer> err = Result.err("Invalid");
     * err.getOrThrow();  // throws JavalidationException
     * }</pre>
     *
     * @return the success value if this is {@link Ok}
     * @throws JavalidationException if this is {@link Err}
     */
    default T getOrThrow() {
        return switch (this) {
            case Ok<T>(T value) -> value;
            case Err<T>(ValidationErrors errors) -> throw new JavalidationException(errors);
        };
    }

    /**
     * Returns the validation errors if this result is an error, or an empty {@link ValidationErrors} if successful.
     * <p>
     * This method is safe to call on both {@link Ok} and {@link Err} variants.
     * <p>
     * Example:
     * <pre>{@code
     * Result<String> result = validateInput(input);
     * ValidationErrors errors = result.getErrors();
     * if (errors.isNotEmpty()) {
     *     // handle validation errors
     * }
     * }</pre>
     *
     * @return the accumulated validation errors, or empty if this is {@link Ok}
     */
    default ValidationErrors getErrors() {
        return switch (this) {
            case Ok<T>(T ignored) -> ValidationErrors.empty();
            case Err<T>(ValidationErrors errors) -> errors;
        };
    }

    /**
     * Adds a prefix to all error field paths in this result, useful for validating nested objects.
     * <p>
     * Root errors are converted to field errors with the given prefix. Field errors have the
     * prefix prepended to their field names with a dot separator.
     * <p>
     * This enables composing validators for nested structures:
     * <pre>{@code
     * // Validate nested address
     * Result<Address> addressResult = validateAddress(person.getAddress())
     *     .withPrefix("address");
     * // Errors will be: "address.street", "address.zipCode", etc.
     *
     * // Validate list items
     * for (int i = 0; i < items.size(); i++) {
     *     Result<Item> itemResult = validateItem(items.get(i))
     *         .withPrefix("items[", i, "]");
     * }
     * }</pre>
     *
     * @param prefix the prefix to add to all field paths
     * @return a new result with prefixed errors if this is {@link Err}, or the same {@link Ok} instance
     * @see #withPrefix(Object, Object...)
     */
    default Result<T> withPrefix(String prefix) {
        return switch (this) {
            case Ok<T> self -> self;
            case Err<T>(ValidationErrors errors) -> new Err<>(errors.withPrefix(prefix));
        };
    }

    /**
     * Adds a prefix to all error field paths by concatenating the given objects into a string.
     * <p>
     * This is a convenience method for building prefixes from multiple parts without manual string concatenation.
     * <p>
     * Example:
     * <pre>{@code
     * // Validate list items
     * for (int i = 0; i < items.size(); i++) {
     *     validateItem(items.get(i))
     *         .withPrefix("items[", i, "]");  // produces "items[0]", "items[1]", etc.
     * }
     * }</pre>
     *
     * @param first the first part of the prefix
     * @param remaining additional parts to concatenate
     * @return a new result with prefixed errors if this is {@link Err}, or the same {@link Ok} instance
     * @see #withPrefix(String)
     */
    default Result<T> withPrefix(Object first, Object... remaining) {
        if (this instanceof Ok) {
            return this;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(first);
        for (Object o : remaining) {
            sb.append(o);
        }
        return withPrefix(sb.toString());
    }

    /**
     * Combines this result with another to enable applicative validation.
     * <p>
     * This is the entry point for combining multiple validation results using the applicative functor pattern.
     * Chain multiple {@code and()} calls and terminate with {@code combine()} to aggregate all results.
     * <p>
     * All errors from all results are accumulated, enabling validation of multiple fields simultaneously.
     * <p>
     * Example:
     * <pre>{@code
     * Result<Person> person = validateName(name)
     *     .and(validateAge(age))
     *     .and(validateEmail(email))
     *     .combine((n, a, e) -> new Person(n, a, e));
     * }</pre>
     *
     * @param result the result to combine with this one
     * @param <U> the type of the other result's success value
     * @return a {@link ResultCombiner2} that can be chained with more results or terminated with {@code combine()}
     * @see ResultCombiner2
     */
    default <U extends @Nullable Object> ResultCombiner2<T, U> and(Result<U> result) {
        return new ResultCombiner2<>(this, result);
    }

    /**
     * Provides a fallback result if this result is an error (lazy evaluation).
     * <p>
     * This implements short-circuit evaluation: if this result is {@link Ok}, the supplier is not called.
     * If this result is {@link Err}, the fallback result's errors are merged with this result's errors.
     * <p>
     * Example:
     * <pre>{@code
     * Result<User> user = findUserInCache(id)
     *     .or(() -> findUserInDatabase(id))
     *     .or(() -> Result.err("User not found"));
     * }</pre>
     *
     * @param supplier supplies the fallback result (only called if this is {@link Err})
     * @return this result if {@link Ok}, otherwise the fallback result with merged errors
     */
    default Result<T> or(Supplier<Result<T>> supplier) {
        return switch (this) {
            case Ok<T> self -> self;
            case Err<T>(ValidationErrors errors) -> supplier.get().mapErr(errors::mergeWith);
        };
    }

    /**
     * Provides a fallback result if this result is an error (eager evaluation).
     * <p>
     * If this result is {@link Err}, the fallback result's errors are merged with this result's errors.
     * <p>
     * Example:
     * <pre>{@code
     * Result<Config> config = loadUserConfig()
     *     .or(loadDefaultConfig());
     * }</pre>
     *
     * @param other the fallback result
     * @return this result if {@link Ok}, otherwise the fallback result with merged errors
     */
    default Result<T> or(Result<T> other) {
        return switch (this) {
            case Ok<T> self -> self;
            case Err<T>(ValidationErrors errors) -> other.mapErr(errors::mergeWith);
        };
    }

    /**
     * Transforms the success value using the provided function, preserving errors.
     * <p>
     * This is the functor map operation. If this result is {@link Ok}, applies the function to the value.
     * If this result is {@link Err}, returns a new {@link Err} with the same errors.
     * <p>
     * <b>Exception handling (Error Channel):</b> If the mapper function throws {@link JavalidationException},
     * it is caught and converted to {@link Err}.
     * <p>
     * Example:
     * <pre>{@code
     * Result<Integer> age = Result.ok("25")
     *     .map(Integer::parseInt);  // Ok(25)
     *
     * Result<User> user = Result.ok(userId)
     *     .map(id -> findUserOrThrow(id));  // JavalidationException caught → Err
     *
     * Result<Integer> error = Result.err("Invalid");
     * Result<String> stillError = error.map(a -> "Age: " + a);  // Err(...)
     * }</pre>
     *
     * @param mapper the function to transform the success value
     * @param <U> the type of the transformed value
     * @return a new result with the transformed value, or the same errors
     * <p>
     * <b>Implementation Note:</b> Only {@link JavalidationException} is caught and converted to {@link Err}. All other
     * exceptions (such as {@link NullPointerException}, {@link IllegalStateException},
     * {@link java.io.IOException}, etc.) propagate normally through the call stack. This
     * design distinguishes expected validation failures from unexpected programming errors,
     * enabling fail-fast behavior for bugs while maintaining functional error accumulation
     * for domain validation failures. See the "Error Channel" section in the class documentation
     * for the complete rationale.
     */
    default <U extends @Nullable Object> Result<U> map(Function<T, U> mapper) {
        try {
            return switch (this) {
                case Ok<T>(T value) -> new Ok<>(mapper.apply(value));
                case Err<T>(ValidationErrors errors) -> new Err<>(errors);
            };
        } catch (JavalidationException e) {
            return new Err<>(e.getErrors());
        }
    }

    /**
     * Transforms the validation errors using the provided function, preserving the success value.
     * <p>
     * If this result is {@link Err}, applies the function to the errors. If this result is {@link Ok},
     * returns the same result unchanged.
     * <p>
     * This is useful for transforming error messages or adding context to existing errors.
     * <p>
     * Example:
     * <pre>{@code
     * Result<String> result = validateInput(input)
     *     .mapErr(errors -> errors.withPrefix("request"));
     * }</pre>
     *
     * @param mapper the function to transform the validation errors
     * @return the same result if {@link Ok}, or a new result with transformed errors
     */
    default Result<T> mapErr(Function<ValidationErrors, ValidationErrors> mapper) {
        return switch (this) {
            case Ok<T> self -> self;
            case Err<T>(ValidationErrors errors) -> new Err<>(mapper.apply(errors));
        };
    }

    /**
     * Chains error transformations, allowing recovery from errors.
     * <p>
     * If this result is {@link Ok}, returns it unchanged. If this result is {@link Err}, applies
     * the mapper function which can either recover (return {@link Ok}) or transform the errors
     * (return a different {@link Err}).
     * <p>
     * This enables error recovery and fallback logic in the error track.
     * <p>
     * Example:
     * <pre>{@code
     * Result<User> result = findUserInCache(id)
     *     .flatMapErr(errors -> findUserInDatabase(id))  // Try database on cache miss
     *     .flatMapErr(errors -> Result.ok(defaultUser));  // Use default user as fallback
     * }</pre>
     *
     * @param mapper function that receives errors and produces a new result
     * @return this result if {@link Ok}, otherwise the result produced by the mapper
     */
    default Result<T> flatMapErr(Function<ValidationErrors, Result<T>> mapper) {
        return switch (this) {
            case Ok<T> ok -> ok;
            case Err<T>(ValidationErrors errors) -> mapper.apply(errors);
        };
    }

    /**
     * Transforms both the success value and the validation errors using the provided functions.
     * <p>
     * This is a bifunctor map operation. If this result is {@link Ok}, applies the success function
     * to the value. If this result is {@link Err}, applies the error function to the errors.
     * <p>
     * This is useful when you need to transform both success and failure paths simultaneously.
     * <p>
     * Example:
     * <pre>{@code
     * Result<String> result = validateAge(age)
     *     .bimap(
     *         age -> "Valid age: " + age,
     *         errors -> errors.withPrefix("user")
     *     );
     * }</pre>
     *
     * @param onSuccess function to transform the success value
     * @param onError function to transform the validation errors
     * @param <U> the type of the transformed success value
     * @return a new result with transformed value or errors
     */
    default <U extends @Nullable Object> Result<U> bimap(
            Function<T, U> onSuccess,
            Function<ValidationErrors, ValidationErrors> onError
    ) {
        return switch (this) {
            case Ok<T>(T value) -> new Ok<>(onSuccess.apply(value));
            case Err<T>(ValidationErrors errors) -> new Err<>(onError.apply(errors));
        };
    }

    /**
     * Performs a side effect with the success value without transforming it.
     * <p>
     * This is useful for logging, debugging, or other side effects in the success path.
     * The value is not modified, and this result is returned unchanged.
     * <p>
     * Example:
     * <pre>{@code
     * Result<User> result = validateUser(user)
     *     .peek(u -> logger.info("Validated user: {}", u.name()))
     *     .flatMap(u -> saveUser(u));
     * }</pre>
     *
     * @param action action to perform with the success value (must not be null)
     * @return this result unchanged
     */
    default Result<T> peek(Consumer<T> action) {
        if (this instanceof Ok<T>(T value)) {
            action.accept(value);
        }
        return this;
    }

    /**
     * Performs a side effect with the validation errors without transforming them.
     * <p>
     * This is useful for logging, debugging, or other side effects in the error path.
     * The errors are not modified, and this result is returned unchanged.
     * <p>
     * Example:
     * <pre>{@code
     * Result<User> result = validateUser(user)
     *     .peekErr(errors -> logger.warn("Validation failed: {} errors", errors.count()))
     *     .mapErr(errors -> errors.withPrefix("user"));
     * }</pre>
     *
     * @param action action to perform with the validation errors (must not be null)
     * @return this result unchanged
     */
    default Result<T> peekErr(Consumer<ValidationErrors> action) {
        if (this instanceof Err<T>(ValidationErrors errors)) {
            action.accept(errors);
        }
        return this;
    }

    /**
     * Chains this result with another validation operation that may also fail (monadic bind).
     * <p>
     * This is the monadic flatMap operation. If this result is {@link Ok}, applies the function
     * which returns a new {@code Result}. If this result is {@link Err}, returns the errors without
     * calling the function.
     * <p>
     * Use this to chain dependent validations where each step may fail.
     * <p>
     * <b>Exception handling (Error Channel):</b> If the mapper function throws {@link JavalidationException},
     * it is caught and converted to {@link Err}.
     * <p>
     * Example:
     * <pre>{@code
     * Result<User> user = validateEmail(email)
     *     .flatMap(e -> findUserByEmail(e))    // may throw JavalidationException → caught
     *     .flatMap(u -> validateUserStatus(u));
     * }</pre>
     *
     * @param mapper the function that produces the next result
     * @param <U> the type of the next result's success value
     * @return the result produced by the mapper function, or the current errors
     * <p>
     * <b>Implementation Note:</b> Only {@link JavalidationException} is caught and converted to {@link Err}. All other
     * exceptions (such as {@link NullPointerException}, {@link IllegalStateException},
     * {@link java.io.IOException}, etc.) propagate normally through the call stack. This
     * design distinguishes expected validation failures from unexpected programming errors,
     * enabling fail-fast behavior for bugs while maintaining functional error accumulation
     * for domain validation failures. See the "Error Channel" section in the class documentation
     * for the complete rationale.
     */
    default <U extends @Nullable Object> Result<U> flatMap(Function<T, Result<U>> mapper) {
        try {
            return switch (this) {
                case Ok<T>(T value) -> mapper.apply(value);
                case Err<T>(ValidationErrors errors) -> new Err<>(errors);
            };
        } catch (JavalidationException e) {
            return new Err<>(e.getErrors());
        }
    }

    /**
     * Adds additional validation logic to this result.
     * <p>
     * If this result is {@link Ok}, runs the predicate function which can add errors to a {@link Validation}.
     * If this result is {@link Err}, returns the existing errors without running the predicate.
     * <p>
     * This enables imperative-style validation within the functional pipeline:
     * <pre>{@code
     * Result<Person> result = Result.ok(person)
     *     .check((p, v) -> {
     *         if (p.age() < 18) {
     *             v.addFieldError("age", "Must be 18 or older");
     *         }
     *         if (p.name().length() < 2) {
     *             v.addFieldError("name", "Name too short");
     *         }
     *     });
     * }</pre>
     *
     * @param predicate a function that receives the value and a validation to add errors to
     * @return a new result with any additional errors, or the existing errors
     */
    default Result<T> check(BiConsumer<T, Validation> predicate) {
        return switch (this) {
            case Ok<T>(T value) -> {
                Validation validation = Validation.create();
                predicate.accept(value, validation);
                yield validation.asResult(value);
            }
            case Err<T> err -> err;
        };
    }

    /**
     * Filters the success value using a predicate, adding a root error if the predicate fails.
     * <p>
     * If this result is {@link Ok} and the predicate returns {@code false}, returns {@link Err} with
     * the given error message. Otherwise preserves the current state.
     * <p>
     * <b>Note on chaining:</b> Chaining multiple {@code filter()} calls creates a fail-fast pipeline
     * where the first failed filter stops execution. To accumulate multiple validation errors,
     * use {@link #check(BiConsumer)} or combine separate {@link Result}s with {@link #and(Result)}.
     * <p>
     * Example:
     * <pre>{@code
     * // Single filter (or fail-fast chain for single field)
     * Result<Integer> result = Result.ok(15)
     *     .filter(age -> age >= 18, "Must be 18 or older");  // Err("Must be 18 or older")
     *
     * // For accumulating multiple errors, use check():
     * Result<Integer> result = Result.ok(age)
     *     .check((a, v) -> {
     *         if (a < 0) v.addRootError("Cannot be negative");
     *         if (a < 18) v.addRootError("Must be at least 18");
     *     });
     * }</pre>
     *
     * @param predicate the condition to test
     * @param message the error message template (supports MessageFormat placeholders)
     * @param args arguments for the message template
     * @return this result if the predicate passes or this is already {@link Err}, otherwise a new {@link Err}
     * @see #check(BiConsumer)
     * @see #and(Result)
     */
    default Result<T> filter(Predicate<T> predicate, String message, Object... args) {
        return check((value, validation) -> {
            if (!predicate.test(value)) {
                validation.addRootError(message, args);
            }
        });
    }

    /**
     * Filters the success value using a predicate, adding a field error if the predicate fails.
     * <p>
     * If this result is {@link Ok} and the predicate returns {@code false}, returns {@link Err} with
     * the given field error. Otherwise preserves the current state.
     * <p>
     * <b>Note on chaining:</b> Chaining multiple {@code filter()} calls on the same field creates
     * a fail-fast pipeline. To accumulate errors for multiple fields, validate each field separately
     * and combine results with {@link #and(Result)}.
     * <p>
     * Example:
     * <pre>{@code
     * // Single field validation (chaining OK for same field, stops at first error)
     * Result<String> name = Result.ok(userName)
     *     .filter(n -> n != null, "name", "Required")
     *     .filter(n -> n.length() >= 2, "name", "Too short");
     *
     * // Multiple fields: use .and() to accumulate ALL errors
     * Result<User> user = validateName(name)
     *     .and(validateAge(age))
     *     .and(validateEmail(email))
     *     .combine((n, a, e) -> new User(n, a, e));
     * }</pre>
     *
     * @param predicate the condition to test
     * @param field the field name for the error
     * @param message the error message template (supports MessageFormat placeholders)
     * @param args arguments for the message template
     * @return this result if the predicate passes or this is already {@link Err}, otherwise a new {@link Err}
     * @see #check(BiConsumer)
     * @see #and(Result)
     */
    default Result<T> filter(Predicate<T> predicate, String field, String message, Object... args) {
        return check((value, validation) -> {
            if (!predicate.test(value)) {
                validation.addFieldError(field, message, args);
            }
        });
    }

    /**
     * Handles both success and failure cases by applying the appropriate function (catamorphism).
     * <p>
     * This is the fundamental eliminator for the {@code Result} type, forcing you to handle both cases.
     * <p>
     * Example:
     * <pre>{@code
     * String message = result.fold(
     *     value -> "Success: " + value,
     *     errors -> "Validation failed: " + errors
     * );
     * }</pre>
     *
     * @param onSuccess function to apply if this is {@link Ok}
     * @param onFailure function to apply if this is {@link Err}
     * @param <U> the return type
     * @return the result of applying the appropriate function
     */
    default <U extends @Nullable Object> U fold(Function<T, U> onSuccess, Function<ValidationErrors, U> onFailure) {
        return switch (this) {
            case Ok<T>(T value) -> onSuccess.apply(value);
            case Err<T>(ValidationErrors errors) -> onFailure.apply(errors);
        };
    }

    /**
     * Returns the success value if present, otherwise returns the provided default value.
     * <p>
     * Example:
     * <pre>{@code
     * String value = result.getOrElse("default");
     * }</pre>
     *
     * @param defaultValue the value to return if this is {@link Err}
     * @return the success value or the default value
     */
    default T getOrElse(T defaultValue) {
        return switch (this) {
            case Ok<T>(T value) -> value;
            case Err<T>(ValidationErrors ignored) -> defaultValue;
        };
    }

    /**
     * Returns the success value if present, otherwise computes a default value using the supplier.
     * <p>
     * The supplier is only called if this result is {@link Err} (lazy evaluation).
     * <p>
     * Example:
     * <pre>{@code
     * String value = result.getOrElse(() -> computeDefault());
     * }</pre>
     *
     * @param supplier supplies the default value (only called if this is {@link Err})
     * @return the success value or the computed default value
     */
    default T getOrElse(Supplier<T> supplier) {
        return switch (this) {
            case Ok<T>(T value) -> value;
            case Err<T>(ValidationErrors ignored) -> supplier.get();
        };
    }

    /**
     * Wraps a supplier that may throw {@link JavalidationException} into a {@code Result}.
     * <p>
     * If the supplier executes successfully, returns {@link Ok} with the produced value.
     * If the supplier throws {@link JavalidationException}, returns {@link Err} with the exception's errors.
     * Other exceptions are not caught and will propagate.
     * <p>
     * Example:
     * <pre>{@code
     * Result<User> user = Result.of(() -> {
     *     Validation v = Validation.create();
     *     validateUser(user, v);
     *     return v.checkAndGet(() -> user);
     * });
     * }</pre>
     *
     * @param supplier the supplier that may throw JavalidationException
     * @param <T> the type of the produced value
     * @return {@link Ok} with the value, or {@link Err} with validation errors
     */
    static <T extends @Nullable Object> Result<T> of(Supplier<T> supplier) {
        try {
            return new Ok<>(supplier.get());
        } catch (JavalidationException e) {
            return new Err<>(e.getErrors());
        }
    }

    /**
     * Wraps a runnable that may throw {@link JavalidationException} into a {@code Result}.
     * <p>
     * If the runnable executes successfully, returns {@link Ok} with {@code null} value.
     * If the runnable throws {@link JavalidationException}, returns {@link Err} with the exception's errors.
     * Other exceptions are not caught and will propagate.
     * <p>
     * Example:
     * <pre>{@code
     * Result<Void> result = Result.of(() -> {
     *     Validation.create()
     *         .addFieldError("field", "error")
     *         .check();  // throws if errors present
     * });
     * }</pre>
     *
     * @param runnable the runnable that may throw JavalidationException
     * @return {@link Ok} with null value, or {@link Err} with validation errors
     */
    static Result<@Nullable Void> of(Runnable runnable) {
        try {
            runnable.run();
            return new Ok<>(null);
        } catch (JavalidationException e) {
            return new Err<>(e.getErrors());
        }
    }

    /**
     * Creates a successful result containing the given value.
     * <p>
     * Example:
     * <pre>{@code
     * Result<String> result = Result.ok("success");
     * Result<User> userResult = Result.ok(user);
     * Result<Void> voidResult = Result.ok(null);
     * }</pre>
     *
     * @param value the success value (may be {@code null})
     * @param <T> the type of the value
     * @return an {@link Ok} result containing the value
     */
    static <T extends @Nullable Object> Result<T> ok(T value) {
        return new Ok<>(value);
    }

    /**
     * Creates a failed result with a single root error.
     * <p>
     * Example:
     * <pre>{@code
     * Result<User> result = Result.err("User not found");
     * Result<Integer> ageResult = Result.err("Invalid age: must be positive");
     * }</pre>
     *
     * @param message the error message template (supports MessageFormat placeholders like {0}, {1})
     * @param <T> the type parameter (phantom type, as no value exists)
     * @return an {@link Err} result containing the error
     */
    static <T extends @Nullable Object> Result<T> err(String message) {
        return new Err<>(ValidationErrors.ofRoot(message));
    }

    /**
     * Creates a failed result with a single field error.
     * <p>
     * Example:
     * <pre>{@code
     * Result<User> result = Result.err("email", "Invalid email format");
     * Result<Integer> ageResult = Result.err("age", "Must be at least {0}", 18);
     * }</pre>
     *
     * @param field the field name
     * @param message the error message template (supports MessageFormat placeholders)
     * @param <T> the type parameter (phantom type, as no value exists)
     * @return an {@link Err} result containing the field error
     */
    static <T extends @Nullable Object> Result<T> err(String field, String message) {
        return new Err<>(ValidationErrors.ofField(field, message));
    }

    /**
     * Creates a failed result with the given validation errors.
     * <p>
     * Example:
     * <pre>{@code
     * ValidationErrors errors = ValidationErrors.of("Invalid input");
     * Result<User> result = Result.err(errors);
     * }</pre>
     *
     * @param errors the validation errors
     * @param <T> the type parameter (phantom type, as no value exists)
     * @return an {@link Err} result containing the errors
     */
    static <T extends @Nullable Object> Result<T> err(ValidationErrors errors) {
        return new Err<>(errors);
    }

    /**
     * Combines multiple results, accumulating all errors and computing a success value if all results are {@link Ok}.
     * <p>
     * This is a varargs alternative to the {@link #and(Result)} chaining API. All errors from all failed results
     * are accumulated. The success supplier is only called if all results are successful.
     * <p>
     * Example:
     * <pre>{@code
     * Result<Person> person = Result.combine(
     *     () -> new Person(name, age, email),
     *     validateName(name),
     *     validateAge(age),
     *     validateEmail(email)
     * );
     * }</pre>
     *
     * @param onSuccess supplies the success value if all results are {@link Ok}
     * @param results the results to combine
     * @param <R> the type of the combined success value
     * @return {@link Ok} with the computed value if all results succeed, otherwise {@link Err} with accumulated errors
     */
    static <R extends @Nullable Object> Result<R> combine(Supplier<R> onSuccess, Result<?>... results) {
        Validation validation = Validation.create();
        for (Result<?> result : results) {
            if (result instanceof Err(ValidationErrors errors)) {
                validation.addAll(errors);
            }
        }
        return validation.asResult(onSuccess);
    }
}
