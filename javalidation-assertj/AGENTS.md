# javalidation-assertj — AssertJ Integration

**Package:** `io.github.raniagus.javalidation.assertj`
**Dependencies:** `javalidation` (core), `org.assertj:assertj-core`

Provides fluent AssertJ assertions for `Result<T>`, `ValidationErrors`, `Validation`, and `PartialResult<T>`.

## Source File Index

| File | Role |
|------|------|
| `JavalidationAssertions.java` | Entry-point. Static `assertThat(...)` overloads for all four types. |
| `ResultAssert.java` | Assertions for `Result<T>`. `.isOk()` → `OkResultAssert`; `.isErr()` → `ValidationErrorsAssert`. |
| `OkResultAssert.java` | Assertions on the unwrapped success value (delegates to standard AssertJ). |
| `ValidationErrorsAssert.java` | Assertions for `ValidationErrors` (and transitively for `Validation` and `Result.Err`). |
| `PartialResultAssert.java` | Assertions for `PartialResult<T>`. |
| `PropertyPathNotationParser.java` | Parses property-path strings like `"items[0].price"` into `FieldKey` for use in assertions. |

## Public API

### Entry point

```java
import static io.github.raniagus.javalidation.assertj.JavalidationAssertions.assertThat;

assertThat(Result<T> actual)          → ResultAssert<T>
assertThat(ValidationErrors actual)   → ValidationErrorsAssert
assertThat(Validation actual)         → ValidationErrorsAssert  // calls actual.finish()
assertThat(PartialResult<T> actual)   → PartialResultAssert<T>
```

### `ResultAssert<T>`

```
.isOk()     → OkResultAssert<T>  (fails if Err)
.isErr()    → ValidationErrorsAssert  (fails if Ok)
```

### `OkResultAssert<T>`

Extends `AbstractObjectAssert<OkResultAssert<T>, T>`. All standard AssertJ object assertions are available:
```
.isEqualTo(expected)
.isNull()  / .isNotNull()
// …and any other AbstractObjectAssert methods
```

### `ValidationErrorsAssert`

```
// Emptiness
.isEmpty()           // no root errors AND no field errors
.isNotEmpty()

// Error counts
.hasErrorCount(n)          // total root + field
.hasRootErrorCount(n)
.hasFieldErrorCount(n)     // sum across all field keys
.hasFieldErrorCountAt(FieldKey, n)

// Root errors
.hasNoRootErrors()
.hasRootError(message, args...)

// Field errors
.hasNoFieldErrors()
.hasFieldError(String field, message, args...)   // single-segment string key
.hasFieldError(int index, message, args...)      // single-segment int key
.hasFieldErrorAt(String path, message, args...)  // property-path notation: "items[0].price"
.hasFieldErrorAt(FieldKey key, message, args...) // explicit FieldKey
.hasFieldKey(Object... path)                     // key is present (any message)
.doesNotHaveFieldKey(Object... path)             // key is absent
```

### `PartialResultAssert<T>`

```
.hasErrors()    → ValidationErrorsAssert (fails if no errors)
.hasNoErrors()  → PartialResultAssert<T> (fails if errors present)
.success()      → AbstractObjectAssert (for the success value field)
```

## Usage Patterns

```java
// In processor tests — two assertThat imports coexist:
import static io.github.raniagus.javalidation.assertj.JavalidationAssertions.assertThat;
import static com.google.testing.compile.CompilationSubject.assertThat;
// Use unambiguous call site syntax

// Fluent chain for a single field error
assertThat(validator.validate(record))
    .hasErrorCount(1)
    .hasFieldError("email", "io.github.raniagus.javalidation.constraints.Email.message");

// Fluent chain for multiple errors
assertThat(validator.validate(record))
    .hasErrorCount(2)
    .hasFieldError("name", "io.github.raniagus.javalidation.constraints.NotNull.message")
    .hasFieldError("age",  "io.github.raniagus.javalidation.constraints.Min.message", 18);

// No-error case
assertThat(validator.validate(record)).isEmpty();

// Result assertions
assertThat(result).isOk().isEqualTo(expectedValue);
assertThat(result).isErr()
    .hasRootError("some.message.key")
    .hasNoFieldErrors();

// Nested path assertion
assertThat(errors)
    .hasFieldErrorAt("items[0].price", "io.github.raniagus.javalidation.constraints.Min.message", 0);
```

## Key Rules (from `.agents/validator-processor-tests.md`)

- `.hasErrorCount(N)` must come **immediately after `assertThat(...)`** on the same chain, before any `.hasFieldError()` / `.hasRootError()` calls.
- `.isEmpty()` is used for the no-error case — no `hasErrorCount` needed there.

## Feature Deep-Dive

- `.agents/features/assertj-integration.md`
