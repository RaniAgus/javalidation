# Feature: Result Merging and Error Prefixing

This feature covers how validation errors from multiple sources are combined and how field paths
are namespaced via prefixes. There are three entry points: `ValidationErrors`, `Result`, and `Validation`.

**Sources:**
- `javalidation/src/main/java/io/github/raniagus/javalidation/ValidationErrors.java`
- `javalidation/src/main/java/io/github/raniagus/javalidation/Result.java` (`withPrefix`)
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

## Prefixing — Adding Context to Error Paths

### `ValidationErrors.withPrefix`

Returns a new `ValidationErrors` with all field paths prepended by the given segments.
Root errors become field errors under the given prefix.

```java
ValidationErrors addressErrors = validateAddress(address);
// addressErrors: root=["Invalid address"], fields={"street": ["Required"]}

ValidationErrors prefixed = addressErrors.withPrefix("address");
// prefixed: root=[], fields={"address": ["Invalid address"], "address.street": ["Required"]}
```

#### Mixed prefix (strings + integers)

```java
for (int i = 0; i < items.size(); i++) {
    ValidationErrors itemErrors = validateItem(items.get(i));
    ValidationErrors prefixed = itemErrors.withPrefix("items", i);
    // fields: "items[0].price", "items[1].name", etc.
}
```

#### Overloads

```java
errors.withPrefix(String... prefix)   // string segments
errors.withPrefix(Number... prefix)   // numeric segments
errors.withPrefix(Object... prefix)   // mixed (Number → IntKey, else → StringKey)
```

### `Result.withPrefix`

Same semantics as `ValidationErrors.withPrefix`, but on the `Result` level.
If `Ok`, the result is returned unchanged.

```java
Result<Address> result = validateAddress(address)
    .withPrefix("user", "address");
// Err errors are prefixed with "user.address.*"
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

---

## Error Accumulation Semantics

| Operation | Accumulates all | Fail-fast |
|-----------|----------------|-----------|
| `Validation.addError*` + `finish/check` | ✓ | ✗ |
| `Result.and(…).combine(…)` | ✓ | ✗ |
| `Result.map` / `flatMap` chain | ✗ | ✓ (stops at first Err) |
| `Result.ensure` chain | ✗ | ✓ (stops at first failed predicate) |
| `ResultCollector.toResultList()` | ✓ | ✗ |
| `Result.or(…)` | merges errors | tries fallback |

---

## `FieldKey` Path Segments

Paths are represented as ordered arrays of `FieldKeyPart`:
- `FieldKeyPart.StringKey("name")` → rendered as `.name` or `[name]`
- `FieldKeyPart.IntKey(0)` → rendered as `[0]` or `.0`

The default formatter (`PropertyPathNotationFormatter`) renders:
- String keys with a dot separator: `address.street`
- Integer keys with bracket notation: `items[0]`
- Combined: `items[0].price`, `users[2].address.street`
