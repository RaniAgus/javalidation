# Feature: Jakarta Validator (Annotation-Driven Validation)

This feature covers end-to-end use of Jakarta validation annotations (`jakarta.validation.constraints.*`)
to generate type-safe validators via the APT annotation processor.

**Sources:**
- `javalidation-jakarta-validator/src/main/java/io/github/raniagus/javalidation/validator/`
- `javalidation-jakarta-validator-processor/src/main/java/io/github/raniagus/javalidation/validator/processor/`

---

## How It Works

1. You annotate your `record` with standard Jakarta constraint annotations.
2. The annotation processor (`ValidatorProcessor`) runs at compile-time and generates:
   - `MyRecordValidator.java` — implements `InitializableValidator<MyRecord>`
   - `Validators.java` — a static registry replacing the stub
3. At runtime, call `Validators.validate(myRecord)` → `ValidationErrors`.

---

## Annotating Records

```java
import jakarta.validation.constraints.*;

public record CreateUserRequest(
    @NotNull @NotBlank String name,
    @NotNull @Email String email,
    @NotNull @Min(18) @Max(120) Integer age,
    @Valid Address address     // @Valid triggers nested validator generation
) {}

public record Address(
    @NotNull @NotBlank String street,
    @NotNull @Pattern(regexp = "\\d{5}") String zipCode
) {}
```

### Supported Constraint Annotations

All 22 built-in `jakarta.validation.constraints.*` are supported:

| Constraint | Notes |
|-----------|-------|
| `@NotNull` | any object |
| `@Null` | any object |
| `@NotEmpty` | `CharSequence`, `Collection`, `Map`, array |
| `@NotBlank` | `CharSequence` |
| `@Size(min, max)` | `CharSequence`, `Collection`, `Map`, array |
| `@Min(value)` | numeric types and `CharSequence` |
| `@Max(value)` | numeric types and `CharSequence` |
| `@DecimalMin(value, inclusive)` | numeric |
| `@DecimalMax(value, inclusive)` | numeric |
| `@Positive` | numeric |
| `@PositiveOrZero` | numeric |
| `@Negative` | numeric |
| `@NegativeOrZero` | numeric |
| `@Digits(integer, fraction)` | numeric, `CharSequence` |
| `@Pattern(regexp)` | `CharSequence` — generates `static final Pattern FIELDNAME_PATTERN` |
| `@Email` | `CharSequence` — generates `static final Pattern FIELDNAME_PATTERN` |
| `@Past` | temporal types |
| `@PastOrPresent` | temporal types |
| `@Future` | temporal types |
| `@FutureOrPresent` | temporal types |
| `@AssertTrue` | `boolean`/`Boolean` |
| `@AssertFalse` | `boolean`/`Boolean` |

---

## `@Valid` for Nested Records

`@Valid` on a record component generates:
- A `initialize(ValidatorsHolder holder)` call that wires the nested validator at startup.
- In `validate(...)`, the field is delegated to `holder.validate(field)` with errors prefixed.

```java
public record OrderRequest(
    @Valid CustomerRequest customer,        // nested record
    @Valid List<ItemRequest> items          // list of records (each element validated)
) {}
```

### Sealed Interface Support

`@Valid` on a sealed interface field generates a validator that pattern-matches permitted subtypes:

```java
public record PaymentRequest(
    @Valid PaymentMethod method  // sealed interface
) {}

public sealed interface PaymentMethod permits CreditCard, BankTransfer {}
public record CreditCard(…) implements PaymentMethod {}
public record BankTransfer(…) implements PaymentMethod {}
```

All permitted subtypes must be records. Non-record subtypes cause a compile error.

---

## Runtime Usage (without Spring)

```java
// Static registry (after processor runs)
ValidationErrors errors = Validators.validate(myRecord);
boolean canValidate = Validators.hasValidator(MyRecord.class);
Validator<MyRecord> v = Validators.getValidator(MyRecord.class);

// Manual wiring with ValidatorsHolder (for tests or DI)
ValidatorsHolder holder = new ValidatorsHolder(Map.of(
    MyRecord.class, new MyRecordValidator(),
    MyRecord.Nested.class, new MyRecord$NestedValidator()
));
holder.initialize();  // must call before validate
ValidationErrors errors = holder.validate(myRecord);
```

---

## Message Keys

Generated validators use constraint message keys of the form:
```
io.github.raniagus.javalidation.constraints.<ConstraintName>.message
```

Examples:
- `io.github.raniagus.javalidation.constraints.NotNull.message`
- `io.github.raniagus.javalidation.constraints.Email.message`
- `io.github.raniagus.javalidation.constraints.Min.message` (with arg: min value)

These are **opaque keys** at validation time. They are resolved to human-readable strings by
`TemplateStringFormatter`. The Spring Boot starter provides default English strings automatically
via a bundled `messages.properties`.

---

## Limitations

- **Records only** — plain classes and non-sealed interfaces are not supported.
- **No validation groups** — `groups` attribute is silently ignored.
- **No custom/composed constraints** — only built-in `jakarta.validation.constraints.*`.
- **Sealed interfaces** — all permitted subtypes must be records.

See `.agents/known-limitations.md` for full details.

---

## Generated Class Conventions

```java
@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class MyRecordValidator implements InitializableValidator<MyRecord> {
    // @Pattern fields get:
    static final Pattern FIELDNAME_PATTERN = Pattern.compile("regexp");

    @Override
    public void initialize(ValidatorsHolder holder) {
        // empty unless @Valid nested fields exist
    }

    @Override
    public void validate(Validation validation, MyRecord root) {
        validation.withField("fieldName", () -> {
            var value = root.fieldName();
            if (value == null) {
                validation.addError("io.github.raniagus.javalidation.constraints.NotNull.message");
                return;
            }
            // constraint checks...
        });
    }
}
```

Nested validator class names use `$` separator:
- Inner record `Foo.Bar` → `Foo$BarValidator`

---

## Tests

See `.agents/validator-processor-tests.md` for the full guide on adding code-generation tests
and validator logic tests.
