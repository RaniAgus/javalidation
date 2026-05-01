# Feature: Jackson Integration

`javalidation-jackson` integrates javalidation types with Jackson 3.x (`tools.jackson`).
It covers **two distinct serialization scenarios** with different audiences and wire formats.

**Source:** `javalidation-jackson/src/main/java/io/github/raniagus/javalidation/jackson/`

> ⚠️ Jackson groupId is **`tools.jackson`**, not `com.fasterxml.jackson`.

---

## Two Serialization Use Cases

| | `Result<T>` serialization | `ValidationErrors` serialization |
|---|---|---|
| **Audience** | Internal backend-to-backend traffic | Frontends / BFFs |
| **Messages** | Opaque `{code, args}` — not yet formatted | Already formatted strings |
| **Round-trip** | ✓ Full deserialize → `Result<T>` | ✗ Read-only / one-way |
| **FieldKey format** | Raw array `["items", 0, "price"]` | Rendered string (`items[0].price`) |
| **Layout options** | Fixed (`ok`, `value`/`errors`) | Structured (default) or Flattened |
| **Notation options** | N/A | Property-path, Dots, Brackets |

---

## Use Case 1: `Result<T>` — Internal Backend Traffic

### Purpose

`Result<T>` serialization is designed for service-to-service calls where:
- The receiving backend needs to **reconstruct a `Result<T>` Java object** (deserialize round-trip)
- Message formatting is deferred — each side resolves keys in its own locale / `MessageSource`
- The `FieldKey` path must survive as typed data (string vs integer segment distinction preserved)
- The receiving backend may **cache `Result.Err` responses** — e.g. a "not found" result for a
  given ID — to avoid redundant calls to the real API on subsequent requests

### Wire Format

`Result.Ok`:
```json
{
  "ok": true,
  "value": {"name": "Alice", "age": 30}
}
```

`Result.Err`:
```json
{
  "ok": false,
  "errors": {
    "rootErrors": [
      {"code": "io.github.raniagus.javalidation.constraints.NotNull.message", "args": []}
    ],
    "fieldErrors": [
      {
        "key": ["email"],
        "errors": [{"code": "io.github.raniagus.javalidation.constraints.Email.message", "args": []}]
      },
      {
        "key": ["items", 0, "price"],
        "errors": [{"code": "io.github.raniagus.javalidation.constraints.Min.message", "args": [1]}]
      }
    ]
  }
}
```

Key observations:
- Discriminator field `"ok": boolean` identifies Ok vs Err (not just presence of `value` or `errors`)
- Errors use `"code"` (not `"message"`) — preserving the raw `TemplateString.message()` key
- `fieldErrors` is a **list** of `{key: Object[], errors: [...]}`, not a map — the `key` array
  preserves the typed structure (`String` for named fields, `Integer` for numeric indices)

### Deserialization

`StructuredResultDeserializer` reconstructs `Result<T>` from this format:
- Reads the `"ok"` discriminator
- On `true`: reads `"value"` as `T` using the declared `JavaType`
- On `false`: reads `"errors"` as `StructuredValidationErrorsDto` → converts to `ValidationErrors`

The deserializer is registered as a type-parametric `Deserializers` resolver, so `Result<Person>`,
`Result<Order>`, etc. each get a correctly-typed deserializer instance.

### Round-Trip Requirement

If you replace the `resultSerializer` with a custom one, you **must** also replace the
`resultDeserializerFactory` to maintain round-trip compatibility. The `withTemplateStringFormatter`
builder method is intentionally excluded from affecting the `Result` serializer — error codes must
stay opaque for round-trip.

---

## Use Case 2: `ValidationErrors` — Frontend / BFF Exposure

### Purpose

`ValidationErrors` serialization is designed for exposing validation results to clients (React,
Vue, mobile apps, BFFs) where:
- Messages must be **already formatted** human-readable strings
- Clients need a simple map structure (no Java-specific `{code, args}` objects)
- Key notation must match the client-side form library's expected format

### Layout: Structured (default)

`ValidationErrors` is serialized as a JSON object with separate `rootErrors` and `fieldErrors`
sections. Empty sections are omitted.

```json
{
  "rootErrors": ["Invalid request"],
  "fieldErrors": {
    "email": ["Must be a valid email address"],
    "items[0].price": ["Must be at least 1"]
  }
}
```

Field keys are rendered strings using the configured `FieldKeyFormatter`.

### Layout: Flattened (`withFlattenedErrors()`)

All errors are merged into a single flat map. Root errors use the empty-string key `""`.

```json
{
  "": ["Invalid request"],
  "email": ["Must be a valid email address"],
  "items[0].price": ["Must be at least 1"]
}
```

Activated via:
- Builder: `JavalidationModule.builder().withFlattenedErrors()`
- Spring Boot property: `io.github.raniagus.javalidation.flatten-errors: true`

### Key Notation Options

Controls how `FieldKey` is rendered as a JSON key string. Applies to both layouts.

| Notation | Builder method | Spring property value | `items[0].price` | Compatible with |
|----------|---------------|----------------------|------------------|-----------------|
| Property-path (default) | `.withPropertyPathNotation()` | `property_path` | `items[0].price` | [conform](https://conform.guide/) |
| Dots | `.withDotNotation()` | `dots` | `items.0.price` | [react-hook-form](https://react-hook-form.com/) |
| Brackets | `.withBracketNotation()` | `brackets` | `items[0][price]` | [qs](https://github.com/ljharb/qs) |

### Message Formatting

`TemplateString` values are formatted to plain strings before serialization using
`TemplateStringFormatter`. Without a configured formatter, the default
`MessageFormatTemplateStringFormatter` is used (formats via `java.text.MessageFormat`).

```java
TemplateStringFormatter myFormatter = ts ->
    messageSource.getMessage(ts.message(), ts.args(), locale);

JavalidationModule module = JavalidationModule.builder()
    .withTemplateStringFormatter(myFormatter)
    .build();
```

In Spring Boot, the `MessageSourceTemplateStringFormatter` bean is auto-configured and wired in
automatically when `use-message-source: true` (default).

---

## Registration

### Standalone (without Spring)

```java
import tools.jackson.databind.json.JsonMapper;

JsonMapper mapper = JsonMapper.builder()
    .addModule(JavalidationModule.getDefault())
    .build();
```

### Spring Boot (automatic)

The starter (`javalidation-spring-boot-starter`) auto-registers the module when both
`tools.jackson.databind.json.JsonMapper` and `javalidation-jackson` are on the classpath.
No manual registration needed. Controlled via Spring Boot properties:

```yaml
io.github.raniagus.javalidation:
  key-notation: property_path   # property_path | dots | brackets
  use-message-source: true      # true (default) | false
  flatten-errors: false         # false (default) | true
```

---

## Builder Reference

```java
JavalidationModule.builder()
    // Key notation for ValidationErrors (mutually exclusive)
    .withPropertyPathNotation()                // items[0].price  (default, compatible with conform)
    .withDotNotation()                         // items.0.price   (compatible with react-hook-form)
    .withBracketNotation()                     // [items][0][price]
    .withFieldKeyFormatter(FieldKeyFormatter)  // custom formatter

    // Message formatting for ValidationErrors
    .withTemplateStringFormatter(formatter)    // resolves TemplateString keys to strings

    // ValidationErrors layout
    .withFlattenedErrors()                     // flat {"": [], "field": []}

    // Low-level overrides
    .withFieldKeySerializer(ValueSerializer<FieldKey>)
    .withTemplateStringSerializer(ValueSerializer<TemplateString>)
    .withValidationErrorsSerializer(ValueSerializer<ValidationErrors>)
    .withResultSerializer(ValueSerializer<Result<?>>)             // ⚠️ also update deserializer
    .withResultDeserializerFactory(Function<JavaType, ValueDeserializer<Result<?>>>)

    .build()
```
