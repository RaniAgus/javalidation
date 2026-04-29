# Agent Guide

## Environment

- Java 21 via sdkman. Run `sdk use java 21.0.9-tem` or rely on `.sdkmanrc` auto-env.
- Maven wrapper: `./mvnw`

## Module layout (build order matters)

| Module                                     | artifact                                  | depends on                 |
|--------------------------------------------|-------------------------------------------|----------------------------|
| `javalidation`                             | core (zero deps)                          | —                          |
| `javalidation-assertj`                     | AssertJ assertions for `Result<T>` etc.   | core                       |
| `javalidation-jackson`                     | Jackson 3.x integration                   | core                       |
| `javalidation-jakarta-validator`           | `Validators` stub + `Validator` interface | core                       |
| `javalidation-jakarta-validator-processor` | APT annotation processor                  | jakarta-validator          |
| `javalidation-spring-boot-starter`         | Spring Boot 4.x auto-config               | jackson, jakarta-validator |

**Jackson uses groupId `tools.jackson` (not `com.fasterxml.jackson`).**

## Common commands

```bash
# Build and test all modules
./mvnw verify

# Test one module only
./mvnw test -pl javalidation-jakarta-validator-processor

# Test one specific test class
./mvnw test -pl javalidation-jakarta-validator-processor \
  -Dtest=JakartaValidationsTest

# Test one nested class (JUnit 5 syntax)
./mvnw test -pl javalidation-jakarta-validator-processor \
  -Dtest="JakartaValidationsTest\$EmailRecordValidatorTest"
```

## Non-obvious behaviours

**Processor compiles with `<proc>none</proc>`** — the processor module does not process its own annotations. Do not remove this flag.

**Fixture files are classpath resources, not compiled sources.** The `src/test/java/test/` trees under the processor module are copied as resources via `maven-resources-plugin`. They are loaded in tests via `JavaFileObjects.forResource(...)` and compared with `hasSourceEquivalentTo()` (whitespace-flexible source diff). Do not expect IDEs to compile them as part of the test source set.

**Surefire `--add-opens`** for `jdk.compiler` internals (required by `compile-testing`) are configured in the parent POM. `mvn test` picks them up automatically; running tests directly in an IDE requires copying those flags to the run configuration.

**`Validators.java` stub replacement.** The `javalidation-jakarta-validator` artifact ships a stub `Validators` that throws `IllegalStateException`. The processor generates a class with the same FQN (`io.github.raniagus.javalidation.validator.Validators`) into `target/generated-sources/annotations`; `javac` treats it as the authoritative definition, replacing the stub at compile time.

**Validation error messages are opaque keys**, not resolved strings. The key `io.github.raniagus.javalidation.constraints.NotNull.message` is baked into generated validators. Resolution happens at runtime via `TemplateStringFormatter` (Spring `MessageSource`-backed by default). A user who does not configure this will receive raw keys.

## Sub-guides

- [`.agents/validator-processor-tests.md`](.agents/validator-processor-tests.md) — how to add tests in `javalidation-jakarta-validator-processor`
- [`.agents/spring-boot-starter-tests.md`](.agents/spring-boot-starter-tests.md) — how to add tests in `javalidation-spring-boot-starter`
- [`.agents/known-limitations.md`](.agents/known-limitations.md) — known limitations and future work
