# javalidation-jakarta-validator-processor — Annotation Processor

**Package:** `io.github.raniagus.javalidation.validator.processor`
**Dependencies:** `javalidation-jakarta-validator`, `jakarta.validation-api`, `jspecify`

APT annotation processor that generates `*Validator` classes and a `Validators` registry from
records annotated with `jakarta.validation.constraints.*` and `@Valid`.

> ⚠️ This module compiles with `<proc>none</proc>` — it does NOT process its own annotations.

## Source File Index

### Processor

| File | Role |
|------|------|
| `ValidatorProcessor.java` | Main `AbstractProcessor`. Entry point. Discovers all classes in source roots, orchestrates writers, persists class names across incremental rounds via `META-INF/.../validators.list`. |

### Class Writers

| File | Role |
|------|------|
| `ValidatorClassWriter.java` | Abstract base for all class writers. Holds `fullName()`, `simpleName()`, `packageName()`, `generate()`. |
| `RecordValidatorClassWriter.java` | Generates `*Validator` for a `record` type. Implements `initialize(...)` and `validate(...)`. |
| `SealedValidatorClassWriter.java` | Generates `*Validator` for a `sealed interface` whose permitted subtypes are all records — delegates to each subtype's validator. |
| `ValidatorsClassWriter.java` | Generates the `Validators.java` registry (`hasValidator`, `validate`, `getValidator`). |
| `ClassWriter.java` | Utility for writing Java source text (indentation helpers, import management). |

### Field Writers

| File | Role |
|------|------|
| `ValidationWriter.java` | Abstract base for writing individual field validation blocks. |
| `NullSafeWriter.java` | Generates null-check code (`if (value == null) { addError(...); return; }`). |
| `NullUnsafeWriter.java` | Generates code for primitive/non-nullable fields (no null check). |
| `FieldWriter.java` | Composes `NullSafeWriter`/`NullUnsafeWriter` with constraint writers for a single record component. |
| `WithNestedObjectWriters.java` | Mixin for writing `initialize(holder)` and `@Valid`-nested field delegation. |

### Parsing and Type Handling

| File | Role |
|------|------|
| `JakartaAnnotationParser.java` | Parses Jakarta constraint annotations on record components into internal models. |
| `TypeAdapter.java` | Maps `TypeMirror` to the correct writer strategy (numeric kind, temporal kind, nullable, etc.). |
| `NumericKind.java` | Enum: `INT`, `LONG`, `DOUBLE`, `BIG_DECIMAL`, `BIG_INTEGER`, `CHAR_SEQUENCE`. |
| `TemporalKind.java` | Enum: `INSTANT`, `LOCAL_DATE`, `LOCAL_DATE_TIME`, `LOCAL_TIME`, `OFFSET_DATE_TIME`, `ZONED_DATE_TIME`, `YEAR`, `YEAR_MONTH`, `MONTH_DAY`, `CALENDAR`, `DATE`. |
| `ValidationOutput.java` | Value type carrying the generated `validate(...)` method body for a single component. |

## Supported Jakarta Constraints

The processor recognises all 22 built-in `jakarta.validation.constraints.*` annotations:

| Constraint | Applicable Types |
|-----------|-----------------|
| `@NotNull` | any object |
| `@Null` | any object |
| `@NotEmpty` | `CharSequence`, `Collection`, `Map`, `array` |
| `@NotBlank` | `CharSequence` |
| `@Size(min, max)` | `CharSequence`, `Collection`, `Map`, `array` |
| `@Min(value)` | numeric (int, long, BigInteger, BigDecimal, CharSequence) |
| `@Max(value)` | numeric |
| `@DecimalMin(value, inclusive)` | numeric |
| `@DecimalMax(value, inclusive)` | numeric |
| `@Positive` | numeric |
| `@PositiveOrZero` | numeric |
| `@Negative` | numeric |
| `@NegativeOrZero` | numeric |
| `@Digits(integer, fraction)` | numeric, `CharSequence` |
| `@Pattern(regexp)` | `CharSequence` |
| `@Email` | `CharSequence` |
| `@Past` | temporal types |
| `@PastOrPresent` | temporal types |
| `@Future` | temporal types |
| `@FutureOrPresent` | temporal types |
| `@AssertTrue` | `boolean`/`Boolean` |
| `@AssertFalse` | `boolean`/`Boolean` |

**See `.agents/known-limitations.md` for what is not supported.**

## Generated Code Conventions

- Class annotation order: `@NullMarked` first, then `@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")`
- Imports sorted alphabetically
- `@Pattern`/`@Email` generate a `static final Pattern FIELDNAME_PATTERN` field
- `@Digits` on `CharSequence` generates `FIELDNAME_DIGITS_PATTERN`
- `initialize(ValidatorsHolder holder)` is empty unless record has `@Valid` fields
- Nested record validators are named `OuterRecord$InnerRecordValidator` (dollar-separated)

## Test Fixture Conventions

Test fixtures live in `src/test/java/test/` (copied as classpath resources, not compiled):
- `test/jakarta/` — fixtures for individual constraint annotations
- `test/collection/` — fixtures for `@Valid` on `Iterable`/`Map` fields

Each fixture pair is:
- `FooRecord.java` — input record with annotations
- `FooRecordValidator.java` — expected generated output

**See `.agents/validator-processor-tests.md` for the full guide.**

## Test Commands

```bash
# All processor tests
mvn test -pl javalidation-jakarta-validator-processor

# One test class
mvn test -pl javalidation-jakarta-validator-processor -Dtest=JakartaValidationsTest

# One nested class
mvn test -pl javalidation-jakarta-validator-processor \
  -Dtest="JakartaValidationsTest\$EmailRecordValidatorTest"
```

## Non-Obvious Behaviours

**Surefire `--add-opens` for `jdk.compiler`.** The parent POM configures `--add-opens` flags
required by the `compile-testing` library (which accesses `jdk.compiler` internals). Running
`mvn test` picks them up automatically. If you run tests directly in an IDE, copy those
`-J--add-opens` JVM flags from the Surefire plugin configuration into the IDE's run configuration,
or the tests will fail with `InaccessibleObjectException`.

**Generated validators bake in opaque message keys**, not human-readable strings. The key
`io.github.raniagus.javalidation.constraints.NotNull.message` is written literally into the
generated source. Resolution to a human-readable string happens at runtime via
`TemplateStringFormatter` (backed by `MessageSource` in Spring Boot). Without a configured
formatter, users will see raw keys in serialized errors.

## Feature Deep-Dive

- `.agents/features/jakarta-validator.md`
- `.agents/validator-processor-tests.md`
- `.agents/known-limitations.md`
