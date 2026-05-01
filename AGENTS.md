# Agent Guide

This is the entrypoint for agentic coding agents working in this repository. It provides a roadmap to all other documentation.

## Environment

- Java 21 via sdkman. Run `sdk use java 21.0.9-tem` or rely on `.sdkmanrc` auto-env.
- Maven: `mvn` (no wrapper — run `mvn` directly)

## Repository Structure

`javalidation` is a multi-module Maven library that provides Railway-Oriented Programming (ROP) validation for Java 21+. It accumulates validation errors instead of failing fast, and offers both functional (monadic) and imperative APIs.

| Module                                     | Artifact                                  | Depends on                 |
|--------------------------------------------|-------------------------------------------|----------------------------|
| `javalidation`                             | Core — zero deps                          | —                          |
| `javalidation-assertj`                     | AssertJ assertions for `Result<T>` etc.   | core                       |
| `javalidation-jackson`                     | Jackson 3.x serialization/deserialization | core                       |
| `javalidation-jakarta-validator`           | `Validators` stub + `Validator` interface | core                       |
| `javalidation-jakarta-validator-processor` | APT annotation processor                  | jakarta-validator          |
| `javalidation-spring-boot-starter`         | Spring Boot 4.x auto-config               | jackson, jakarta-validator |

**Jackson uses groupId `tools.jackson` (not `com.fasterxml.jackson`).**

## Documentation Structure

### Module-Specific Guidance

Each module has its own `AGENTS.md` with source file index, public API, and patterns:

- **`javalidation/AGENTS.md`** — core types (`Result`, `Validation`, `ValidationErrors`, `FieldKey`, `TemplateString`, combiners, collectors, formatters)
- **`javalidation-assertj/AGENTS.md`** — AssertJ assertion classes and usage patterns
- **`javalidation-jackson/AGENTS.md`** — `JavalidationModule`, serializers, deserializers, DTO types
- **`javalidation-jakarta-validator/AGENTS.md`** — `Validator`, `Validators` stub, `ValidatorsHolder`, `InitializableValidator`
- **`javalidation-jakarta-validator-processor/AGENTS.md`** — annotation processor internals, class writers, supported constraints
- **`javalidation-spring-boot-starter/AGENTS.md`** — auto-configuration classes, beans, `@EnableJavalidation`, properties

### Feature Documentation

Deep-dive guides for each cross-cutting feature live in `.agents/features/`:

- **`.agents/features/functional-style.md`** — `Result` monadic API: `map`, `flatMap`, `fold`, `ensure`, `and/combine`, `or`, `bimap`, `peek`
- **`.agents/features/imperative-style.md`** — `Validation` builder API: `addError`, `addErrorAt`, `withField`, `withEach`, `addAll`, `check`, `asResult`
- **`.agents/features/stream-collectors.md`** — `ResultCollector` stream API: `toListOrThrow`, `toResultList`, `toPartialResult`, `toValidation`, `addErrorsTo`, `withIndex`, `withPrefix`
- **`.agents/features/result-merging.md`** — `ValidationErrors.mergeWith`, `Validation.addAll`, `addAllAt`, applicative combining
- **`.agents/features/field-key.md`** — `FieldKey` array internals, `withPrefix` mechanics, prefix stack in `Validation`, `withIndex`/`withPrefix` in collectors, rendering
- **`.agents/features/jackson-integration.md`** — Two use cases: `Result<T>` (internal traffic, round-trip, opaque `{code,args}`) vs `ValidationErrors` (frontend/BFF, formatted, two layouts × three notations with client-library compat)
- **`.agents/features/assertj-integration.md`** — `JavalidationAssertions.assertThat(...)`, `ResultAssert`, `ValidationErrorsAssert`, `PartialResultAssert`
- **`.agents/features/jakarta-validator.md`** — `@Valid` annotation processing, `Validator` interface, `Validators.validate(...)`, `ValidatorsHolder`
- **`.agents/features/spring-boot-starter.md`** — auto-config beans, `JavalidationProperties`, `@EnableJavalidation`, `MessageSource` integration, `JavalidationSpringValidator`

### Existing Sub-guides

- **`.agents/validator-processor-tests.md`** — how to add tests in `javalidation-jakarta-validator-processor`
- **`.agents/spring-boot-starter-tests.md`** — how to add tests in `javalidation-spring-boot-starter`
- **`.agents/testing-style.md`** — test conventions: JUnit 5 + AssertJ patterns, naming, structure, module-specific rules for processor and Spring Boot tests
- **`.agents/known-limitations.md`** — known limitations and future work
- **`.agents/release-process.md`** — GPG signing, Maven Central publishing, GitHub Actions workflow, required secrets, tag-based release flow

## Common Commands

```bash
# Build and test all modules
mvn verify

# Build, test, sign, and publish to Maven Central
mvn verify -P release

# Test one module only
mvn test -pl javalidation-jakarta-validator-processor

# Test one specific test class
mvn test -pl javalidation-jakarta-validator-processor \
  -Dtest=JakartaValidationsTest

# Test one nested class (JUnit 5 syntax)
mvn test -pl javalidation-jakarta-validator-processor \
  -Dtest="JakartaValidationsTest\$EmailRecordValidatorTest"
```

## How to Navigate

**Working on core validation logic?** Start with `javalidation/AGENTS.md`, then read either
`.agents/features/functional-style.md` or `.agents/features/imperative-style.md` depending on
which API style you're modifying.

**Working on stream/collection validation?** Read `.agents/features/stream-collectors.md` and
`javalidation/AGENTS.md`.

**Working on error merging or prefixing?** Read `.agents/features/result-merging.md` for combining strategies, and `.agents/features/field-key.md` for how paths are built and prefixed.

**Working on Jackson serialization?** Start with `javalidation-jackson/AGENTS.md`, then
`.agents/features/jackson-integration.md`.

**Working on AssertJ assertions?** Read `javalidation-assertj/AGENTS.md` and
`.agents/features/assertj-integration.md`.

**Working on the annotation processor?** Read `javalidation-jakarta-validator-processor/AGENTS.md`
and `.agents/validator-processor-tests.md`.

**Working on Spring Boot integration?** Read `javalidation-spring-boot-starter/AGENTS.md`,
`.agents/features/spring-boot-starter.md`, and `.agents/spring-boot-starter-tests.md`.

**Need to understand a specific feature end-to-end?** Go directly to the relevant file in
`.agents/features/`.

## Cross-Cutting Concerns

- **Error key format** — All constraint message keys follow `io.github.raniagus.javalidation.constraints.<ConstraintName>.message`. The Spring Boot starter injects library defaults from `io/github/raniagus/javalidation/messages.properties` as the parent `MessageSource`.
- **`FieldKey` path segments** — String segments render as `field.nested`; integer segments render as `[0]` in property-path notation (default). Dot and bracket notation are also available.
- **`TemplateString`** — Stores the raw message key and `Object[]` args separately for deferred formatting. Never format at validation time; always format at serialization time.
- **`jspecify` null-safety** — All public APIs are annotated with `@NullMarked` / `@Nullable` from `org.jspecify`. The `@Nullable` annotation on type parameters (e.g., `Result<T extends @Nullable Object>`) means `null` is a valid success value.

## Working Rules

**Step-by-step workflow for any task:**

1. Read the root `AGENTS.md` (this file) to understand the structure and find the right starting point.
2. Read the relevant module's `AGENTS.md` for its source file index and API surface.
3. Read the relevant feature doc in `.agents/features/` for deep-dive patterns and examples.
4. Read the actual source files to verify your understanding before making changes.
5. Implement the change following the patterns in the feature doc and module `AGENTS.md`.
6. Run `mvn test -pl <module>` to verify your changes.
7. If you change the public API surface, update the corresponding `AGENTS.md` and feature doc.
8. Before writing any new test, read `.agents/testing-style.md` for naming and structural conventions.
9. After completing any task, update the relevant docs with non-obvious behaviours that required
   codebase exploration — so future agents don't have to re-discover them. If no existing doc covers
   the behaviour, add it to the most specific applicable file (module `AGENTS.md`, a feature doc in
   `.agents/features/`, or a new sub-guide in `.agents/`).

**Docs-in-sync rule:** When you add a new public type or method, update the module `AGENTS.md` source file index. When you add a new cross-cutting feature, create a feature doc in `.agents/features/` and link it from this file and the relevant module `AGENTS.md`.