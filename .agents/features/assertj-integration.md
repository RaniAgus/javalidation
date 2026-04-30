# Feature: AssertJ Integration

`javalidation-assertj` provides fluent AssertJ assertions for all javalidation types,
making test code readable and providing helpful failure messages.

**Source:** `javalidation-assertj/src/main/java/io/github/raniagus/javalidation/assertj/`

---

## Static Import

Always use the javalidation-specific entry point, not AssertJ's standard one:

```java
import static io.github.raniagus.javalidation.assertj.JavalidationAssertions.assertThat;
```

> In `JakartaValidationsTest`, two `assertThat` imports coexist — one from `JavalidationAssertions`
> and one from `CompilationSubject`. Both must be kept; do not collapse them.

---

## `Result<T>` Assertions

```java
// Assert Ok and continue asserting on the unwrapped value
assertThat(result).isOk().isEqualTo(expectedValue);
assertThat(result).isOk().isNotNull();

// Assert Err and continue asserting on errors
assertThat(result).isErr()
    .hasErrorCount(2)
    .hasFieldError("name", "io.github.raniagus.javalidation.constraints.NotNull.message")
    .hasFieldError("age",  "io.github.raniagus.javalidation.constraints.Min.message", 18);

assertThat(result).isErr().hasNoRootErrors();
assertThat(result).isErr().isEmpty();    // no errors at all (always false for Err — use hasErrorCount(0) guard)
```

---

## `ValidationErrors` Assertions

Obtained directly from `assertThat(ValidationErrors)`, or transitively from `.isErr()`.

### Emptiness

```java
assertThat(errors).isEmpty();        // no root AND no field errors
assertThat(errors).isNotEmpty();
```

### Error Counts

```java
assertThat(errors).hasErrorCount(3);            // total root + all field
assertThat(errors).hasRootErrorCount(1);
assertThat(errors).hasFieldErrorCount(2);       // sum across all field keys
assertThat(errors).hasFieldErrorCountAt(FieldKey.of("email"), 2);
```

### Root Errors

```java
assertThat(errors).hasNoRootErrors();
assertThat(errors).hasRootError("some.message.key");
assertThat(errors).hasRootError("must be at least {0}", 18);
```

### Field Errors — Named Field

```java
assertThat(errors).hasFieldError("email", "some.message.key");
assertThat(errors).hasFieldError("email", "must be at least {0}", 18);
```

### Field Errors — Indexed Field

```java
assertThat(errors).hasFieldError(0, "some.message.key");   // key is [0]
```

### Field Errors — Property Path String (complex paths)

```java
// Parses "items[0].price" into FieldKey([StringKey("items"), IntKey(0), StringKey("price")])
assertThat(errors).hasFieldErrorAt("items[0].price", "some.message.key");
assertThat(errors).hasFieldErrorAt("user.address.street", "not.blank");
```

### Field Errors — Explicit `FieldKey`

```java
assertThat(errors).hasFieldErrorAt(FieldKey.of("user", "age"), "some.message.key");
assertThat(errors).hasFieldErrorAt(FieldKey.of("items", 0, "price"), "some.message.key");
```

### Key Presence Only

```java
assertThat(errors).hasFieldKey("email");             // key exists, any message
assertThat(errors).hasFieldKey("items", 0, "price"); // composite key
assertThat(errors).doesNotHaveFieldKey("email");
```

---

## `Validation` (mutable builder) Assertions

Calls `finish()` internally — read-only snapshot, does not affect the builder.

```java
Validation validation = Validation.create()
    .addError("not.null")
    .addErrorAt("email", "invalid.format");

assertThat(validation)
    .hasRootError("not.null")
    .hasFieldError("email", "invalid.format");
```

---

## `PartialResult<T>` Assertions

```java
// Assert errors present and continue with error assertions
assertThat(partial).hasErrors()
    .hasFieldError("price", "must.be.positive");

// Assert no errors and continue with success value assertions
assertThat(partial).hasNoErrors()
    .success().isEqualTo(expectedList);
```

---

## Chaining Rules

- `.hasErrorCount(N)` must come **immediately after `assertThat(...)`**, before any
  `.hasFieldError()` / `.hasRootError()` calls.
- `.isEmpty()` is used for the no-error case — no `hasErrorCount` needed.
- Assertions are chainable: each method returns `this` (`ValidationErrorsAssert` or `ResultAssert`).

### Example — correct chaining order

```java
assertThat(validator.validate(record))
    .hasErrorCount(2)                             // ← immediately after assertThat
    .hasFieldError("name", "not.null.key")        // ← then field errors
    .hasFieldError("email", "invalid.format.key");

assertThat(validator.validate(validRecord))
    .isEmpty();                                   // ← no-error case
```

---

## Failure Messages

Failure messages include the full `ValidationErrors` state to aid debugging:

```
Expected Result to be Err but it was Ok with value: Alice

Expected ValidationErrors to be empty but found 2 error(s):
  root=[], field={email=[TemplateString{message='invalid.format', args=[]}]}

Expected field errors to contain key <FieldKey{parts=[StringKey(email)]}>
  but found keys: [FieldKey{parts=[StringKey(name)]}]
```

---

## Dependency

To use `JavalidationAssertions` in a test module, add to `pom.xml`:

```xml
<dependency>
    <groupId>io.github.raniagus</groupId>
    <artifactId>javalidation-assertj</artifactId>
    <scope>test</scope>
</dependency>
```
