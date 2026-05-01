# Feature: Stream Collectors

`ResultCollector` provides `java.util.stream.Collector` factories for processing streams of
`Result<T>` elements. All collectors accumulate **all** errors before deciding outcome (no fail-fast).

**Source:** `javalidation/src/main/java/io/github/raniagus/javalidation/ResultCollector.java`

---

## Collector Types

### `toListOrThrow()` — collect to `List<T>` or throw

Gathers all success values into a list. If any element is `Err`, throws `JavalidationException`
with all accumulated errors after the full stream is consumed.

```java
try {
    List<User> users = items.stream()
        .map(this::validateUser)
        .collect(ResultCollector.toListOrThrow());
} catch (JavalidationException e) {
    ValidationErrors errors = e.getErrors();
}

// With initial capacity hint (avoids ArrayList resizing)
.collect(ResultCollector.toListOrThrow(items.size()))
```

### `toResultList()` — collect to `Result<List<T>>`

Like `toListOrThrow()` but returns `Err` instead of throwing.

```java
Result<List<User>> result = items.stream()
    .map(this::validateUser)
    .collect(ResultCollector.toResultList());

// With initial capacity hint
.collect(ResultCollector.toResultList(items.size()))
```

### `toPartialResult()` — collect successes and errors simultaneously

Returns `PartialResult<List<T>>` — holds both valid items and all accumulated errors.
Use when you want to process what succeeded even if some items failed.

```java
PartialResult<List<User>> partial = items.stream()
    .map(this::validateUser)
    .collect(ResultCollector.toPartialResult());

List<User> validUsers = partial.success();       // items that passed
ValidationErrors errors = partial.errors();      // all collected errors
boolean anyErrors = partial.hasErrors();
Result<List<User>> asResult = partial.toResult();
```

### `toValidation()` — collect errors into a new `Validation`

Success values are **discarded**. Returns a `Validation` (mutable builder) with all accumulated errors.

```java
Validation validation = items.stream()
    .map(this::validateItem)
    .collect(withIndex(ResultCollector.toValidation()));

validation.check(); // throw if any errors
```

### `addErrorsTo(Validation)` — collect errors into an existing `Validation`

Like `toValidation()` but mutates and returns the provided `Validation`. Success values discarded.

```java
Validation validation = Validation.create();

users.stream()
    .map(this::validateUser)
    .collect(ResultCollector.withPrefix("users", ResultCollector.addErrorsTo(validation)));

orders.stream()
    .map(this::validateOrder)
    .collect(ResultCollector.withPrefix("orders", ResultCollector.addErrorsTo(validation)));

validation.check();
```

---

## Wrapper Collectors

Wrappers modify how errors are indexed or prefixed within the inner collector.
They work at the `FieldKey` level: each wrapper prepends one or more `FieldKeyPart` segments
to every `FieldKey` in the element's errors before the inner collector stores them. The string
representations shown below are just the default property-path notation rendering of those keys.

### `withIndex(collector)` — automatic `IntKey(i)` error prefix

Each stream element is assigned a 0-based index. Errors from element `i` are prefixed with an
`IntKey(i)` segment — rendered as `[i]` in property-path notation.

```java
// Without indexing:
// fieldErrors key for "field" error: FieldKey([StringKey("field")])
// rendered: "field"

// With indexing:
Result<List<User>> result = users.stream()
    .map(this::validateUser)
    .collect(ResultCollector.withIndex(ResultCollector.toResultList()));
// fieldErrors keys: FieldKey([IntKey(0), StringKey("field")])
// rendered (property-path): "[0].field", "[2].field"
```

### `withPrefix(String, collector)` — `StringKey` prefix

Prepends a single `StringKey` segment to every error's `FieldKey`. All errors produced by the
inner collector are namespaced under the given field name.

```java
Result<List<Item>> items = order.getItems().stream()
    .map(this::validateItem)
    .collect(ResultCollector.withPrefix("items", ResultCollector.toResultList()));
// Before: FieldKey([StringKey("price")])  → rendered: "price"
// After:  FieldKey([StringKey("items"), StringKey("price")])  → rendered: "items.price"
```

### `withPrefix(int, collector)` — `IntKey` prefix

Prepends a single `IntKey` segment to every error's `FieldKey`. Useful for a fixed position
in a parent collection.

```java
.collect(ResultCollector.withPrefix(0, ResultCollector.toResultList()));
// Before: FieldKey([StringKey("price")])  → rendered: "price"
// After:  FieldKey([IntKey(0), StringKey("price")])  → rendered: "[0].price"
```

---

## Combining Wrappers

Wrappers compose by prepending their segment outermost-last. Each wrapper adds its own
`FieldKeyPart` at the front of the key after the inner wrappers have already applied.

```java
// Effective FieldKey for element i's "price" error:
// FieldKey([StringKey("order"), StringKey("items"), IntKey(i), StringKey("price")])
// Rendered (property-path): "order.items[0].price"

Result<List<Item>> items = order.getItems().stream()
    .map(this::validateItem)
    .collect(
        ResultCollector.withPrefix("order",
            ResultCollector.withPrefix("items",
                ResultCollector.withIndex(
                    ResultCollector.toResultList()
                )
            )
        )
    );
```

---

## Static Import Pattern

```java
import static io.github.raniagus.javalidation.ResultCollector.*;

Result<List<User>> result = users.stream()
    .map(this::validateUser)
    .collect(withIndex(toResultList()));
```

---

## When to Use Which Collector

| Need | Collector |
|------|-----------|
| Get list or throw (exception boundary) | `toListOrThrow()` |
| Get `Result<List<T>>` (functional) | `toResultList()` |
| Partial success + errors side-by-side | `toPartialResult()` |
| Accumulate errors, discard successes | `toValidation()` or `addErrorsTo(validation)` |
| Add position info to errors | wrap with `withIndex(…)` |
| Namespace errors under a path | wrap with `withPrefix(…)` |
