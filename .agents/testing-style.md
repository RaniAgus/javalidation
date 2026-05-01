# Testing Style Guide

Cross-cutting conventions that apply to all modules. For module-specific rules, see the
"Test Conventions" section in each module's `AGENTS.md`.

---

## Test Framework & Libraries

- **JUnit 5** — `@Test`, `@Nested`, `@BeforeEach`, `@ValueSource`, `@ParameterizedTest`
- **AssertJ** — all assertions; never use JUnit's `Assertions.assertEquals`
- **`javalidation-assertj`** — `JavalidationAssertions.assertThat(...)` for `ValidationErrors` in
  the processor and Spring Boot modules (not plain AssertJ for those types)
- **`compile-testing`** — used only in `javalidation-jakarta-validator-processor` for source-level
  code generation assertions

---

## Test Class Structure

- Class is **package-private** (no `public`), named `<ProductionClass>Test`
- Group related tests in `@Nested` inner classes named after the concept under test
  (e.g. `WithIndexTests`, `FactoryMethodTests`, `EqualsTests`, `HashCodeTests`)
- Exception: `ValidationTest` uses flat methods with `// -- method name --` comment separators
  instead of `@Nested` — this is older style; **prefer `@Nested`** in new tests
- No shared mutable state between tests; each test declares its own local variables
- `@BeforeEach` is only used when truly required (e.g. calling `ValidatorsHolder.initialize()`
  before each test in `@Valid`-wiring scenarios)
- `var` is preferred for local variable type inference

---

## Test Method Naming

BDD-style, all camelCase:

```
given<Setup>_when<Action>_then<Outcome>
```

Examples:
- `givenAllOkResults_whenWithIndexToListOrThrow_thenReturnsListOfValues`
- `givenSamePath_whenCompareTo_thenReturnsZero`
- `givenNullMessage_whenAddError_thenThrowsNullPointerException`

---

## AssertJ Patterns

### Value equality (records compare by value)
```java
assertThat(result).isEqualTo(expected);
```

### Exception assertions
```java
assertThatThrownBy(() -> stream.collect(withIndex(toListOrThrow())))
        .asInstanceOf(throwable(JavalidationException.class))
        .extracting(JavalidationException::getErrors)
        .isEqualTo(new ValidationErrors(...));
```

### Sealed type narrowing (`Result.Err`, `Result.Ok`)
```java
assertThat(result)
        .asInstanceOf(InstanceOfAssertFactories.type(Result.Err.class))
        .extracting(Result.Err::errors)
        .isEqualTo(new ValidationErrors(...));
```

### `ValidationErrors` constructor in expected values
`ValidationErrors` is a record — construct it inline:
```java
new ValidationErrors(
        List.of(),                                                      // root errors
        Map.of(
                FieldKey.of(1, "field"), List.of(TemplateString.of("error")),
                FieldKey.of(2),          List.of(TemplateString.of("root"))
        )
)
```

---

## Running Tests

```bash
# All modules
mvn verify

# One module
mvn test -pl <module>

# One test class
mvn test -pl <module> -Dtest=MyTest

# One nested class (JUnit 5 syntax — escape the $ in shell)
mvn test -pl <module> -Dtest="MyTest\$MyNestedTest"
```
