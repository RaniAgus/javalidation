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

// From Optional (empty → error)
Result<User> result = Result.ofOptional(findUser(id), "user.not.found");
// With field prefix on failure:
Result.ofOptional(findUser(id), "user.not.found").withPrefix("user")
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

`and(function)` derives the next result from the previous success values:

```java
Result<Order> order = validateUser(input)
    .and(user -> validateAddress(user.address()))
    .and((user, address) -> validateCart(user, address))
    .combine((user, address, cart) -> new Order(user, address, cart));
```

Dependent `and(function)` calls are skipped when any prior required value is unavailable. Later
independent `and(Result)` calls still contribute their errors, so dependent chaining does not make
the whole combiner fail-fast.

Use `andUsing(projector, fn)` to depend on a **subset** of prior slots instead of all of them.
The projector selects which prior results to guard on; the function is skipped if any selected
result is `Err`, while unselected slots still contribute their errors independently:

```java
// R4 depends on R1 and R3 only — runs even if R2 fails
Result<Order> order = validateUser(input)
    .and(validateAddress(input))          // R2, not needed by R4
    .and(validateSomething(input))        // R3
    .andUsing(
        r -> r.first().and(r.third()),    // guard on R1 and R3 only
        (user, something) -> validateCart(user, something)
    )
    .combine((user, address, something, cart) -> new Order(user, address, something, cart));

// Single-slot: depend only on the second slot of RC2
result1.and(result2).andUsing(r -> r.second(), b -> validateFrom(b))
```

`andUsing` is available on `ResultCombiner2` through `ResultCombiner9` (RC10 terminates the chain).
Each combiner has N−1 overloads: one per sub-combiner size 1 through N−1.
The slot accessors `first()`, `second()`, …, `tenth()` are available on all combiners (RC2–RC10).

Use `getLast()` when the combined value should be the last result in the chain:

```java
Result<Address> address = validateUser(input)
    .and(user -> validateAddress(user.address()))
    .getLast();
```

### `sequence` — lift `List<Result<T>>` to `Result<List<T>>`

Accumulates all errors with `[i]` index prefixes. For streaming, prefer `withIndex(toResultList())`.

```java
List<Result<Item>> validated = items.stream().map(this::validateItem).toList();
Result<List<Item>> result = Result.sequence(validated);
// On failure: errors appear as [0].field, [2].field, etc.
// On success: Ok(List.copyOf(validItems))
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

`map`, `flatMap`, `flatMapErr`, `peek`, `peekErr`, and dependent `and(function)` catch **only** `JavalidationException` and convert it to `Err`.
All other exceptions (NPE, ISE, IOException, etc.) propagate normally. This distinguishes:
- **Expected validation failures** (`JavalidationException`) → `Err`, safe to return to clients
- **Programming errors / bugs** (other exceptions) → propagate, log at boundary, return 500
