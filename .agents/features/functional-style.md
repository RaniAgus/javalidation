# Feature: Functional (Monadic) Style

The functional API is centred on `Result<T>` — a sealed interface with two variants:
- `Result.Ok<T>(T value)` — validation passed, carries the success value
- `Result.Err<T>(ValidationErrors errors)` — validation failed, carries accumulated errors

**Source:** `javalidation/src/main/java/io/github/raniagus/javalidation/Result.java`

---

## Creating a Result

```java
// Success
Result<String> ok = Result.ok("Alice");
Result<Void>   ok = Result.ok(null);          // null is a valid value

// Failure — root error
Result<String> err = Result.error("user.not.found");
Result<String> err = Result.error("must be at least {0}", 18);

// Failure — field error
Result<String> err = Result.error(ValidationErrors.at("email", "invalid.format"));

// From existing ValidationErrors
Result<String> err = Result.error(validationErrors);

// Wrap a supplier that may throw JavalidationException
Result<User> result = Result.of(() -> service.findOrThrow(id));
```

---

## Transforming the Happy Path

### `map` — transform the value, pass errors through

```java
Result<Integer> age = Result.ok("25").map(Integer::parseInt);       // Ok(25)
Result<String>  err = Result.error("invalid").map(s -> s + "!");    // Err(...)
```

`map` catches `JavalidationException` and converts it to `Err`. All other exceptions propagate normally (fail-fast).

### `flatMap` — chain to another validation (monadic bind)

```java
Result<User> user = validateEmail(email)
    .flatMap(e -> findUserByEmail(e))      // may throw JavalidationException
    .flatMap(u -> validateUserStatus(u));
```

### `peek` — side effect on success, no transformation

```java
result.peek(u -> logger.info("Valid user: {}", u.name()));
```

`peek` catches `JavalidationException` thrown by the side-effect action and converts it to `Err`.
All other exceptions propagate normally.

---

## Filtering on the Happy Path

### `ensure` — fail with a root error if predicate is false

```java
Result<Integer> age = Result.ok(15)
    .ensure(a -> a >= 18, "must.be.adult");          // Err
Result<Integer> age = Result.ok(25)
    .ensure(a -> a >= 18, "must.be.adult");          // Ok(25)
```

### `ensureAt` — fail with a field error

```java
Result<User> result = Result.ok(user)
    .ensureAt(u -> u.name() != null, "name", "not.null")
    .ensureAt(u -> u.name().length() >= 2, "name", "too.short");
// These chain: stops at first failure (sequential for the same field)
```

> **Note:** chaining `ensure`/`ensureAt` is fail-fast per chain. Use `and/combine` to accumulate
> errors from independent fields simultaneously.

---

## Handling the Error Path

### `mapErr` — transform errors, pass value through

```java
result.mapErr(errors -> errors.withPrefix("request"));
```

### `flatMapErr` — recover from or transform errors

```java
Result<User> result = findInCache(id)
    .flatMapErr(errors -> findInDatabase(id))    // try database on cache miss
    .flatMapErr(errors -> Result.ok(defaultUser)); // final fallback
```

### `peekErr` — side effect on errors, no transformation

```java
result.peekErr(errors -> logger.warn("{} errors", errors.count()));
```

`peekErr` catches `JavalidationException` thrown by the side-effect action and converts it to `Err`.
All other exceptions propagate normally.

### `or` — eager or lazy fallback

```java
// Lazy (preferred — avoids evaluating if not needed)
result.or(() -> fallback());

// Eager
result.or(fallbackResult);
```

---

## Bifunctor Operations

### `bimap` — transform both paths at once

```java
Result<String> r = validateAge(age).bimap(
    a  -> "Valid age: " + a,
    es -> es.withPrefix("user")
);
```

---

## Combining Multiple Results (Applicative Style)

Use `and(…).combine(…)` to accumulate errors from **independent** validations simultaneously.
All errors are collected even if some results fail.

```java
// 2 results
Result<Person> person = validateName(name)
    .and(validateAge(age))
    .combine((n, a) -> new Person(n, a));

// 3 results
Result<Address> address = validateStreet(street)
    .and(validateCity(city))
    .and(validateZip(zip))
    .combine((s, c, z) -> new Address(s, c, z));

// Up to 10 results (ResultCombiner2 through ResultCombiner10)
```

---

## Eliminating a Result

### `fold` — handle both variants, return a single type

```java
String msg = result.fold(
    value  -> "Valid: " + value,
    errors -> "Errors: " + errors.count()
);
```

### `getOrThrow` — unwrap or throw

```java
User user = result.getOrThrow();   // throws JavalidationException if Err
```

### `getOrElse` — unwrap or return default

```java
String value = result.getOrElse("default");
String value = result.getOrElse(() -> computeDefault());
```

---

## Pattern Matching (Java 21+)

`Result` is a sealed interface — exhaustive switch is possible:

```java
String message = switch (result) {
    case Result.Ok(var value)   -> "Success: " + value;
    case Result.Err(var errors) -> "Errors: " + errors;
};
```

---

## Error Channel Design

`map`, `flatMap`, `flatMapErr`, `peek`, and `peekErr` catch **only** `JavalidationException` and convert it to `Err`.
All other exceptions (NPE, ISE, IOException, etc.) propagate normally. This distinguishes:
- **Expected validation failures** (`JavalidationException`) → `Err`, safe to return to clients
- **Programming errors / bugs** (other exceptions) → propagate, log at boundary, return 500
