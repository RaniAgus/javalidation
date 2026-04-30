# Feature: Imperative Style

The imperative API is centred on `Validation` — a mutable builder that accumulates errors in place,
then converts to `ValidationErrors`, `Result`, or throws.

**Source:** `javalidation/src/main/java/io/github/raniagus/javalidation/Validation.java`

---

## Creating a Validation

```java
Validation validation = Validation.create();
```

---

## Adding Errors

### Root errors (not tied to a specific field)

```java
validation.addError("request.invalid");
validation.addError("must be at least {0} characters", 8);
```

### Field errors (tied to a named field or index)

```java
validation.addErrorAt("email", "not.null");
validation.addErrorAt("email", "invalid.format");   // multiple errors per field OK
validation.addErrorAt("age",   "must.be.at.least", 18);

// Numeric index (for collection elements)
validation.addErrorAt(0, "must.not.be.null");
```

---

## Scoped Field Contexts

`withField` is the primary building block for structured validation. It manages a field-path
prefix stack so that any errors added within the runnable are automatically scoped to the field.

### `withField(String, Runnable)` — named field scope

Root errors added within the runnable become errors for that field;
field errors become nested (dot-notation).

```java
validation.withField("person", () -> {
    if (request.person() == null) {
        validation.addError("not.null");       // → "person": ["not.null"]
    } else {
        if (request.person().name() == null) {
            validation.addErrorAt("name", "not.null");  // → "person.name": ["not.null"]
        }
        if (request.person().age() < 18) {
            validation.addErrorAt("age", "min", 18);    // → "person.age": ["min"]
        }
    }
});
```

### `withField(Number, Runnable)` — indexed scope (renders as `[n]`)

```java
validation.withField(0, () -> {
    validation.addError("not.null");          // → "[0]": ["not.null"]
    validation.addErrorAt("name", "not.null"); // → "[0].name": ["not.null"]
});
```

### Nesting — `withField` inside `withField`

```java
validation.withField("order", () -> {
    validation.withField("address", () -> {
        validation.addErrorAt("street", "not.blank"); // → "order.address.street"
    });
});
```

---

## Collection Iteration

### `withEach(Iterable<T>, Consumer<T>)` — iterate with auto index prefix

```java
validation.withEach(request.tags(), tag -> {
    if (tag.name() == null) {
        validation.addError("not.null");           // → "[0]", "[1]", etc.
    } else if (tag.name().isBlank()) {
        validation.addErrorAt("name", "not.blank"); // → "[0].name", etc.
    }
});
```

### `withEach(Iterable<T>, BiConsumer<T, Integer>)` — with index exposed

```java
validation.withEach(request.tags(), (tag, index) -> {
    if (duplicates.contains(index)) {
        validation.addError("duplicate.at.index", index);
    }
});
```

---

## Merging External Errors

### `addAll(Validation)` — merge another mutable builder

```java
Validation sub = validateAddress(address);
validation.addAll(sub);   // root and field errors merged as-is
```

### `addAll(ValidationErrors)` — merge an immutable snapshot

```java
ValidationErrors addressErrors = validateAddress(address);
validation.addAll(addressErrors);
```

### `addAllAt(FieldKey, ValidationErrors)` — merge with prefix

```java
ValidationErrors addressErrors = validateAddress(user.address());
validation.addAllAt(FieldKey.of("address"), addressErrors);
// root errors from addressErrors → "address": [...]
// field errors "street" → "address.street": [...]
```

---

## Terminating the Builder

### `finish()` — snapshot to `ValidationErrors`

Returns an immutable `ValidationErrors` backed by the builder's current state.
Do not mutate the `Validation` after calling `finish()`.

```java
ValidationErrors errors = validation.finish();
if (errors.isNotEmpty()) { … }
```

### `asResult(T value)` → `Result<T>`

```java
Result<User> result = validation.asResult(user);
// Ok(user) if no errors; Err(errors) otherwise
```

### `asResult(Supplier<T>)` — lazy value (defers construction until validation passes)

```java
Result<Response> result = validation.asResult(() -> buildResponse(data));
// Supplier is only called if no errors. JavalidationException in supplier → Err.
```

### `check()` — throw if errors

```java
validation.check();   // throws JavalidationException if errors exist
// proceed with valid state...
```

### `checkAndGet(Supplier<T>)` — throw or return value

```java
User valid = validation.checkAndGet(() -> user);
```

---

## Typical Full Pattern

```java
public Result<CreateOrderResponse> createOrder(CreateOrderRequest request) {
    Validation validation = Validation.create();

    validation.withField("customer", () -> {
        if (request.customer() == null) {
            validation.addError("not.null");
        } else {
            validation.addAll(validateCustomer(request.customer()));
        }
    });

    validation.withEach(request.items(), item -> {
        if (item.quantity() <= 0) {
            validation.addErrorAt("quantity", "must.be.positive");
        }
    });

    return validation.asResult(() -> orderService.create(request));
}
```

---

## Generated Validator Pattern

The annotation processor generates code that follows this pattern internally. Each record field
becomes a `validation.withField(fieldName, () -> { … })` block, with constraint checks inside.
The `validate(Validation validation, T root)` method signature matches `Validator<T>`.
