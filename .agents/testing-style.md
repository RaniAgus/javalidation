# Testing Style Guide

Conventions derived from the existing test suites across all modules.

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

## Module-Specific Conventions

### `javalidation-jakarta-validator-processor`

Two test types are required for every new constraint/type combination:

1. **Code-generation test** — verifies the processor emits correct Java source.
   - Input fixture: `src/test/java/test/jakarta/FooRecord.java`
   - Expected output: `src/test/java/test/jakarta/FooRecordValidator.java`
   - Register base name in `@ValueSource(strings = {...})` in `JakartaValidationsTest`
   - `hasSourceEquivalentTo()` does a whitespace-flexible AST diff — not a string compare

2. **Validator logic test** — verifies runtime behaviour of the generated validator.
   - Add a `@Nested` class inside `JakartaValidationsTest` (or `CollectionValidationsTest`)
   - Use `JavalidationAssertions.assertThat(...)` (not plain AssertJ) for `ValidationErrors`
   - `.hasErrorCount(N)` must come **immediately after `assertThat(...)`**, before any
     `.hasFieldError()` / `.hasRootError()` calls
   - `.isEmpty()` for the no-error case — no `hasErrorCount` needed

`JakartaValidationsTest` imports **two** `assertThat` static methods that must coexist:
- `io.github.raniagus.javalidation.assertj.JavalidationAssertions.assertThat` (for `ValidationErrors`)
- `com.google.testing.compile.CompilationSubject.assertThat` (for `Compilation` objects)

For `@Valid`-nested validators, wire them manually in `@BeforeEach`:
```java
ValidatorsHolder holder = new ValidatorsHolder(Map.of(
        OuterRecord.class, outerValidator,
        OuterRecord.Inner.class, innerValidator
));

@BeforeEach
void setup() { holder.initialize(); }
```

### `javalidation-spring-boot-starter`

- All test classes **extend `AutoConfigurationTest`**
- `@SpringBootTest(classes = TestApplication.class)` goes on each **nested static class**,
  not the outer class
- Property overrides: `@TestPropertySource(properties = "key=value")` on the nested static class
- `@Autowired(required = false)` to assert bean absence without failing startup
- The `JsonMapper` bean is always autowired — **never build it manually** in tests
- Shared assertions across property variants: use class inheritance; `@TestPropertySource` on
  the subclass overrides the parent (see `TemplateStringFormatterAutoConfigurationTest`)

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
