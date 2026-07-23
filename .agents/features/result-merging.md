# Feature: Result Merging

This feature covers how validation errors from multiple sources are combined into one.
There are two styles: **imperative merging** (via `Validation.addAll`) and
**applicative combining** (via `Result.and(…).combine(…)`).

> For how field paths are prefixed and how `FieldKey` is built internally, see
> `.agents/features/field-key.md`.

**Sources:**
- `javalidation/src/main/java/io/github/raniagus/javalidation/ValidationErrors.java`
- `javalidation/src/main/java/io/github/raniagus/javalidation/Validation.java` (`addAll`, `addAllAt`)
- `javalidation/src/main/java/io/github/raniagus/javalidation/combiner/` (applicative combining)

---

## `ValidationErrors.mergeWith`

Combines two `ValidationErrors` into one. Root errors are concatenated; field errors for the
same key are concatenated.

```java
ValidationErrors a = ValidationErrors.at("email", "not.null");
ValidationErrors b = ValidationErrors.at("email", "invalid.format");
ValidationErrors merged = a.mergeWith(b);
// merged.fieldErrors() = {"email": ["not.null", "invalid.format"]}
```

---

## Merging into `Validation`

### `validation.addAll(ValidationErrors)` — merge flat (no prefix)

Root and field errors are added as-is.

```java
ValidationErrors addressErrors = validateAddress(user.address());
Validation validation = Validation.create();
validation.addAll(addressErrors);
```

### `validation.addAll(Validation)` — merge another builder

Same as above but from a mutable builder.

```java
Validation sub = validateAddress(user.address());
Validation validation = Validation.create();
validation.addAll(sub);
```

### `validation.addAllAt(FieldKey, ValidationErrors)` — merge with prefix

Root errors from `errors` become field errors at `prefix`. Field errors are nested under `prefix`.

```java
ValidationErrors addressErrors = validateAddress(user.address());
validation.addAllAt(FieldKey.of("address"), addressErrors);
// root errors → "address": [...]
// "street" → "address.street": [...]
// "zip.code" → "address.zip.code": [...]
```

### Using `withField` as a prefix scope

`withField` is the idiomatic way to prefix errors when using `Validation`. Any errors added
within the runnable are automatically scoped.

```java
validation.withField("address", () -> {
    validation.addAll(validateAddress(user.address()));
    // OR manually:
    // validation.addError("invalid");           → "address": ["invalid"]
    // validation.addErrorAt("street", "req");   → "address.street": ["required"]
});
```

---

## Applicative Combining (Accumulate Across Multiple Results)

When you have independent validations that should all run simultaneously (accumulating all errors),
use `Result.and(…).combine(…)`.

```java
Result<Person> person = validateName(name)
    .and(validateAge(age))
    .and(validateEmail(email))
    .combine((n, a, e) -> new Person(n, a, e));

// If name is null AND age < 18: Err with both errors
// If all valid: Ok(new Person(...))
```

Combiners are available from 2 (`ResultCombiner2`) up to 10 (`ResultCombiner10`).
Each combiner also has `getLast()`, equivalent to `combine((a, b, ..., n) -> n)`.

### Dependent `and(function)` Steps

`Result.and(function)` and `ResultCombiner2` through `ResultCombiner9` also accept a function that
receives the success values collected so far and returns the next `Result`.

```java
Result<Order> order = validateUser(input)
    .and(user -> validateAddress(user.address()))
    .and((user, address) -> validateCart(user, address))
    .and(validateFeatureFlags()) // independent; still contributes errors
    .combine((user, address, cart, flags) -> new Order(user, address, cart, flags));
```

Semantics:
- `and(Result)` is independent and always contributes its errors.
- `and(function)` is dependent and runs only when all previous values are `Ok`.
- If a dependent step is skipped, it contributes no value and no error.
- Later independent `and(Result)` steps still accumulate errors after a dependent skip or failure.

---

## Error Accumulation Semantics

| Operation | Accumulates all | Fail-fast |
|-----------|----------------|-----------|
| `Validation.addError*` + `finish/check` | ✓ | ✗ |
| `Result.and(…).combine(…)` | ✓ | ✗ |
| Dependent `and(function)` inside a combiner | skips dependent step only | ✗ for later independent steps |
| `Result.map` / `flatMap` chain | ✗ | ✓ (stops at first Err) |
| `Result.ensure` chain | ✗ | ✓ (stops at first failed predicate) |
| `ResultCollector.toResultList()` | ✓ | ✗ |
| `Result.sequence(list)` | ✓ (errors prefixed with `[i]`) | ✗ |
| `Result.or(…)` | merges errors | tries fallback |
