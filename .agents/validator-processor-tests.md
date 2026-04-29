# Adding tests in `javalidation-jakarta-validator-processor`

Every new constraint/type combination requires two distinct things: a **code generation test** and a **validator logic test**. Both live in the same module.

## 1. Code generation test

This verifies that the annotation processor emits the correct Java source.

### Step 1 — Add the input fixture

Create `src/test/java/test/jakarta/FooRecord.java` (this is a classpath resource, not a compiled source):

```java
package test.jakarta;

import jakarta.validation.constraints.NotNull;

public record FooRecord(@NotNull String value) {}
```

### Step 2 — Add the expected output fixture

Create `src/test/java/test/jakarta/FooRecordValidator.java`. This must exactly match what the processor generates. Conventions:

- First two annotations: `@NullMarked` then `@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")`
- Imports sorted alphabetically
- `@Pattern`/`@Email` constraints generate a `static final Pattern` field named `FIELDNAME_PATTERN`
- `@Digits` on a `CharSequence` generates `FIELDNAME_DIGITS_PATTERN`
- `initialize(ValidatorsHolder holder)` is empty unless the record has `@Valid` nested fields

Example:

```java
package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class FooRecordValidator implements InitializableValidator<FooRecord> {
    @Override
    public void initialize(ValidatorsHolder holder) {
    }

    @Override
    public void validate(Validation validation, FooRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) {
                validation.addError("io.github.raniagus.javalidation.constraints.NotNull.message");
                return;
            }
        });
    }
}
```

### Step 3 — Register the base name in the parameterized test

In `JakartaValidationsTest.java`, add `"FooRecord"` to the `@ValueSource(strings = {...})` list. The test compiles the input fixture with the processor and asserts the generated source equals the expected fixture using `hasSourceEquivalentTo()` (whitespace-flexible).

For collection/nested fixtures, register in `CollectionValidationsTest.java` instead, which has its own parameterized test.

### How `hasSourceEquivalentTo` works

`compile-testing`'s `hasSourceEquivalentTo()` does a source-level diff — it normalises whitespace but is sensitive to identifiers, types, and structure. It is not a string comparison. You can format freely as long as the AST matches.

## 2. Validator logic test

This verifies the runtime behaviour of the generated validator.

Add a `@Nested` class in `JakartaValidationsTest` (or `CollectionValidationsTest` for collection types).
Always use `JavalidationAssertions.assertThat(...)` (not `org.assertj.core.api.Assertions.assertThat`) for
assertions on `ValidationErrors`:

```java
import static io.github.raniagus.javalidation.assertj.JavalidationAssertions.assertThat;

@Nested
class Foo {
    FooRecordValidator validator = new FooRecordValidator();

    @Test
    void nullValue_hasFieldError() {
        assertThat(validator.validate(new FooRecord(null))).hasErrorCount(1)
                .hasFieldError("value", "io.github.raniagus.javalidation.constraints.NotNull.message");
    }

    @Test
    void nonNullValue_noErrors() {
        assertThat(validator.validate(new FooRecord("hello")))
                .isEmpty();
    }
}
```

Key rules:
- `.hasErrorCount(N)` must come **immediately after `assertThat(...)`**, on the same line, before any `.hasFieldError()` / `.hasRootError()` calls.
- For multi-error assertions chain additional `.hasFieldError(...)` / `.hasRootError(...)` calls after `.hasErrorCount(N)`.
- `.isEmpty()` is used for the no-error case — no `hasErrorCount` needed there.

Note: `JakartaValidationsTest` imports **two** `assertThat` static methods — one from `JavalidationAssertions`
(for `ValidationErrors`) and one from `CompilationSubject` (for `Compilation` objects in the code-generation test).
Both must coexist; do not collapse them.

### Cross-validator / `@Valid` nested field tests

If the record under test has `@Valid`-annotated fields that reference other validators, you must wire them up manually:

```java
@Nested
class ValidatedFooRecordValidatorTest {
    private final InitializableValidator<ValidatedFooRecord> validator = new ValidatedFooRecordValidator();
    private final InitializableValidator<ValidatedFooRecord.Bar> barValidator = new ValidatedFooRecord$BarValidator();

    private final ValidatorsHolder validatorsHolder = new ValidatorsHolder(Map.of(
            ValidatedFooRecord.class, validator,
            ValidatedFooRecord.Bar.class, barValidator
    ));

    @BeforeEach
    void setup() {
        validatorsHolder.initialize();
    }

    // tests ...
}
```

### Building expected `ValidationErrors` for multi-error assertions

Chain `.hasErrorCount(N)` followed by as many `.hasFieldError(...)` / `.hasRootError(...)` calls as needed:

```java
assertThat(validator.validate(new FooRecord(null, -1))).hasErrorCount(2)
        .hasFieldError("name", "io.github.raniagus.javalidation.constraints.NotNull.message")
        .hasFieldError("age", "io.github.raniagus.javalidation.constraints.Min.message", 18);
```

## Test dependency

`javalidation-assertj` is declared as a `<scope>test</scope>` dependency in
`javalidation-jakarta-validator-processor/pom.xml`. Any new test module that wants to use
`JavalidationAssertions.assertThat(...)` must add the same entry.

## Running tests

```bash
# All processor tests
./mvnw test -pl javalidation-jakarta-validator-processor

# One test class
./mvnw test -pl javalidation-jakarta-validator-processor -Dtest=JakartaValidationsTest

# One nested class
./mvnw test -pl javalidation-jakarta-validator-processor \
  -Dtest="JakartaValidationsTest\$FooRecordValidatorTest"
```
