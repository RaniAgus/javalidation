# javalidation-jakarta-validator — Validator API

**Package:** `io.github.raniagus.javalidation.validator`
**Dependencies:** `javalidation` (core), `jakarta.validation-api`

Provides the `Validator` interface and supporting types. The `Validators` class here is a **stub**
that is replaced at compile-time by the annotation processor (see `javalidation-jakarta-validator-processor`).

## Source File Index

| File | Role |
|------|------|
| `Validator.java` | Core interface. Implement to validate a type `T`. |
| `InitializableValidator.java` | Extended interface for validators with `@Valid`-annotated nested fields. Has `initialize(ValidatorsHolder)`. |
| `Validators.java` | **Stub** — static registry. Replaced by generated class at compile-time. Throws `IllegalStateException` if the processor didn't run. |
| `ValidatorsHolder.java` | Runtime container mapping `Class<T>` → `InitializableValidator<T>`. Used in tests and DI wiring. |

## Key Public API

### `Validator<T>` interface

```java
public interface Validator<T> {
    // Override this for validation logic:
    void validate(Validation validation, T value);

    // Default — creates Validation, calls validate(validation, value), returns finish():
    default ValidationErrors validate(T value);
}
```

### `InitializableValidator<T>` interface

```java
public interface InitializableValidator<T> extends Validator<T> {
    // Called once during wiring to inject cross-validator references:
    void initialize(ValidatorsHolder holder);
}
```

Generated validators always implement `InitializableValidator`. If the record has no `@Valid` nested
fields, `initialize(holder)` is a no-op.

### `Validators` (generated static registry)

```java
// Checks if a generated validator exists for this class
Validators.hasValidator(Class<?> clazz) → boolean

// Runs the validator for any annotated record type
Validators.validate(T instance) → ValidationErrors

// Gets the typed validator instance
Validators.getValidator(Class<T> type) → Validator<T>
```

### `ValidatorsHolder` (used in tests and DI)

```java
new ValidatorsHolder(Map<Class<?>, InitializableValidator<?>> validators)

holder.initialize()                     // calls initialize(this) on all validators
holder.hasValidator(Class<?> clazz)     → boolean
holder.validate(T instance)             → ValidationErrors
holder.getValidator(Class<T> clazz)     → Validator<T>
```

## Usage — Programmatic (without Spring)

```java
// Wire validators manually (test or standalone)
ValidatorsHolder holder = new ValidatorsHolder(Map.of(
    MyRecord.class, new MyRecordValidator(),
    MyRecord.Nested.class, new MyRecord$NestedValidator()
));
holder.initialize();

ValidationErrors errors = holder.validate(new MyRecord(...));
```

## Usage — With Processor (compile-time)

Annotate your records with `jakarta.validation.constraints.*` and add `@Valid` for nested records. The processor generates:
- `MyRecordValidator.java` — implements `InitializableValidator<MyRecord>`
- `Validators.java` — static registry replacing the stub

```java
// After processor runs, this works:
ValidationErrors errors = Validators.validate(myRecord);
```

## Feature Deep-Dive

- `.agents/features/jakarta-validator.md`
- `.agents/validator-processor-tests.md`
