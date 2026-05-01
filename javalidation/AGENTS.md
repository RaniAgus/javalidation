# javalidation — Core Module

**Package:** `io.github.raniagus.javalidation`
**Dependencies:** zero (only `org.jspecify` annotations at compile-time)

This module is the foundation of the library. Every other module depends on it.

## Source File Index

### Root package — `io.github.raniagus.javalidation`

| File | Role |
|------|------|
| `Result.java` | Sealed interface: `Ok<T>` / `Err<T>`. Railway-oriented success/failure monad. |
| `Validation.java` | Mutable builder for accumulating errors imperatively. |
| `ValidationErrors.java` | Immutable record holding `List<TemplateString>` root errors and `Map<FieldKey, List<TemplateString>>` field errors. |
| `JavalidationException.java` | Unchecked exception wrapping `ValidationErrors`. Thrown by `getOrThrow()`, `check()`, `checkAndGet()`. |
| `TemplateString.java` | Record holding a message-key `String` + `Object[]` args; formatting is deferred. |
| `FieldKey.java` | Ordered array of `FieldKeyPart` segments representing a field path (e.g. `items[0].price`). |
| `FieldKeyPart.java` | Sealed interface: `StringKey(String)` for named fields, `IntKey(int)` for numeric indices. |
| `PartialResult.java` | Record `(T success, ValidationErrors errors)` — holds both partial successes and errors side-by-side. |
| `ResultCollector.java` | Public interface + static factory methods for `Collector<Result<T>, ?, R>` stream collectors. The interface itself is internal infrastructure; only the static factories are public API. |
| `PrefixStack.java` | Public sealed cons-list (`Empty` / `Cons`) used to pass accumulated `FieldKeyPart` prefixes down the `ResultCollectorWrapper` chain in O(1) per level. Only allocated once per stream element; converted to a `FieldKeyPart[]` by the leaf collector via `toArray()`. |
| `ResultCollectorWrapper.java` | Internal `WithIndex` and `WithPrefix` wrappers. Each overrides `add(Result, PrefixStack)` to prepend its own segment and forward to the inner collector. |
| `ListResultCollector.java` | Internal implementation for `toListOrThrow`, `toResultList`, and `toPartialResult` collectors. Leaf: converts `PrefixStack` to `FieldKeyPart[]` once via `toArray()`. |
| `ValidationCollector.java` | Internal implementation for `toValidation` and `addErrorsTo` collectors. Leaf: same `PrefixStack.toArray()` conversion. |

### `combiner` sub-package — `io.github.raniagus.javalidation.combiner`

| File | Role |
|------|------|
| `ResultCombiner2.java` … `ResultCombiner10.java` | Applicative-style combiners for 2–10 `Result` values. Obtained via `Result.and(...)`. |

### `format` sub-package — `io.github.raniagus.javalidation.format`

| File | Role |
|------|------|
| `TemplateStringFormatter.java` | `@FunctionalInterface` — formats `TemplateString` to `String`. Default: `MessageFormatTemplateStringFormatter`. |
| `MessageFormatTemplateStringFormatter.java` | Implementation using `java.text.MessageFormat`. |
| `FieldKeyFormatter.java` | `@FunctionalInterface` — formats `FieldKey` to `String`. Default: `PropertyPathNotationFormatter`. |
| `PropertyPathNotationFormatter.java` | Renders `items[0].price` (dots for strings, brackets for ints). **Default.** |
| `DotNotationFormatter.java` | Renders `items.0.price` (all dots). |
| `BracketNotationFormatter.java` | Renders `[items][0][price]` (all brackets). |

### `function` sub-package — `io.github.raniagus.javalidation.function`

| File | Role |
|------|------|
| `TriFunction.java` … `DecaFunction.java` | `@FunctionalInterface` types for 3–10 arguments. Used by `ResultCombiner3`–`ResultCombiner10`. |

## Key Public API

### `Result<T>`

```
Result.ok(value)                       → Ok<T>
Result.error(message, args...)         → Err<T>
Result.error(ValidationErrors)         → Err<T>
Result.of(Supplier<T>)                 → Ok<T> or Err<T>
Result.combine(Supplier<R>, results…)  → internal — used by combiners

result.map(fn)                         → Result<U>
result.flatMap(fn)                     → Result<U>
result.mapErr(fn)                      → Result<T>
result.flatMapErr(fn)                  → Result<T>
result.bimap(onSuccess, onError)       → Result<U>
result.ensure(predicate, msg, args…)   → Result<T>
result.ensureAt(predicate, field, msg) → Result<T>
result.and(other)                      → ResultCombiner2<T,U>
result.or(supplier)                    → Result<T>
result.or(other)                       → Result<T>
result.fold(onSuccess, onFailure)      → U
result.getOrThrow()                    → T | throws JavalidationException
result.getOrElse(default)             → T
result.getOrElse(supplier)            → T
result.peek(action)                    → Result<T>
result.peekErr(action)                 → Result<T>
result.withPrefix(parts…)             → Result<T>
result.errors()                        → ValidationErrors
```

### `Validation`

```
Validation.create()                      → Validation (factory)

validation.addError(msg, args…)          → Validation
validation.addErrorAt(field, msg, args…) → Validation  (String or Number field)
validation.withField(field, runnable)    → Validation  (String or Number field)
validation.withEach(items, consumer)     → Validation  (Consumer or BiConsumer)
validation.addAll(Validation)            → Validation
validation.addAll(ValidationErrors)      → Validation
validation.addAllAt(FieldKey, errors)    → Validation

validation.finish()                      → ValidationErrors
validation.asResult(value)               → Result<T>
validation.asResult(supplier)            → Result<T>
validation.check()                       → void | throws JavalidationException
validation.checkAndGet(supplier)         → T | throws JavalidationException
```

### `ValidationErrors`

```
ValidationErrors.empty()
ValidationErrors.of(msg, args…)
ValidationErrors.at(field, msg, args…)   (String, Number, or FieldKey)

errors.mergeWith(other)     → ValidationErrors
errors.withPrefix(parts…)   → ValidationErrors
errors.rootErrors()          → List<TemplateString>
errors.fieldErrors()         → Map<FieldKey, List<TemplateString>>
errors.isEmpty()             → boolean
errors.isNotEmpty()          → boolean
errors.count()               → int
```

### `ResultCollector` (stream collectors)

```java
// Collector factories (static methods on ResultCollector)
ResultCollector.toListOrThrow()                 // → List<T> or throw
ResultCollector.toListOrThrow(initialCapacity)
ResultCollector.toResultList()                  // → Result<List<T>>
ResultCollector.toResultList(initialCapacity)
ResultCollector.toPartialResult()               // → PartialResult<List<T>>
ResultCollector.toValidation()                  // → Validation (errors only)
ResultCollector.addErrorsTo(validation)         // → Validation (mutates existing)

// Wrappers
ResultCollector.withIndex(collector)            // adds [0], [1]… prefix
ResultCollector.withPrefix(String, collector)   // adds string prefix
ResultCollector.withPrefix(int, collector)      // adds int prefix
```

### `FieldKey` / `FieldKeyPart`

```
FieldKey.of(String...)  → FieldKey
FieldKey.of(Number...)  → FieldKey
FieldKey.of(Object...)  → FieldKey  (mixed; Number → IntKey, else → StringKey)
FieldKey.of(FieldKeyPart...) → FieldKey
fieldKey.withPrefix(parts)   → FieldKey

FieldKeyPart.StringKey(String value)
FieldKeyPart.IntKey(int value)
FieldKeyPart.ofPath(String[])  → FieldKeyPart[]
FieldKeyPart.ofPath(Number[])  → FieldKeyPart[]
FieldKeyPart.ofPath(Object[])  → FieldKeyPart[]
```

## Test Conventions

Tests use plain AssertJ (`org.assertj.core.api.Assertions`) — **not** `JavalidationAssertions`.
`ValidationErrors` is compared by value as a record, constructed inline in expected values.

For general naming, class structure, and AssertJ patterns, see `.agents/testing-style.md`.

## Feature Deep-Dives

- **Functional (monadic) style:** `.agents/features/functional-style.md`
- **Imperative style:** `.agents/features/imperative-style.md`
- **Stream collectors:** `.agents/features/stream-collectors.md`
- **Result / error merging:** `.agents/features/result-merging.md`
- **FieldKey internals and prefix mechanics:** `.agents/features/field-key.md`
