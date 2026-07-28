# Known Limitations

## 1. Records only — permanent design decision

The processor only generates validators for `record` types. Plain classes and interfaces (other than sealed interfaces whose all permitted subtypes are records) are not supported. This is a deliberate choice to keep the processor simple.

**Behaviour:** Using `@Valid` on a non-record parameter produces a **compile error** (not a warning). The processor emits no validator for that type.

**Rationale:** Records are immutable data carriers. The intended idiom is one record shape per use case (e.g., `CreateUserRequest` vs. `UpdateUserRequest`) rather than reusing a single mutable class across different validation contexts.

**No plans to extend this.**

## 2. Validation groups are not supported

The `groups` attribute present on all Jakarta constraint annotations is detected and **silently ignored** with a compile-time warning. All constraints are always applied regardless of the `groups` value.

**Behaviour:** `@NotNull(groups = OnCreate.class)` emits a warning and the constraint is applied unconditionally.

**Rationale:** Same as above — records as one-shape-per-use-case makes groups redundant.

**No plans to support groups.**

## 3. Validator-backed custom constraints are not supported

The processor supports reusable composed constraints whose `@Constraint(validatedBy = {})` is
empty, provided their built-in composition has one unambiguous supported scalar type. It generates
a public `AnnotationNameValidator` in the annotation package. Constraints backed by a custom
`ConstraintValidator` remain unsupported.

**Workaround:** Break a validator-backed custom constraint into supported Jakarta counterparts on
the record field, or implement its validation imperatively.

**Future work:**
- Support a wider set of common scalar types and multi-family compositions.
- Support configuration forwarding for every custom constraint attribute and `@OverridesAttribute`.

## 4. Sealed interface subtypes must all be records

When `@Valid` is placed on a sealed interface, the processor expects all permitted subtypes to be records. Non-record permitted subtypes produce a **compile error** and are skipped.

## 5. Error message keys are opaque at compile time

Generated validators bake in message keys (e.g., `io.github.raniagus.javalidation.constraints.NotNull.message`). Resolution happens at runtime via `TemplateStringFormatter`.

In the Spring Boot starter, the library's bundled `messages.properties` (covering all 22 constraint keys with default English strings) is automatically injected as the **parent** of the application's `MessageSource` via `javalidationMessageSourceParentConfigurer`. This means all library keys are resolved out of the box with no user configuration. Users can override any key by defining it in their own `messages.properties`.

`MessageSourceTemplateStringFormatter` tries `MessageSource` first. If a key is not found (e.g. a user-defined custom message that is itself a `MessageFormat` pattern like `"Hello {0}!"`), it falls back to the default `TemplateStringFormatter` which treats the key as a `MessageFormat` pattern directly.

Setting `io.github.raniagus.javalidation.use-message-source=false` disables `MessageSource` lookup entirely — all keys are passed directly to the `MessageFormat` fallback, so library keys will appear as raw strings in responses.
