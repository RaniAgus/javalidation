# Feature: Jackson Integration

`javalidation-jackson` integrates javalidation types with Jackson 3.x (`tools.jackson`).
It registers serializers/deserializers for `Result<T>`, `ValidationErrors`, `FieldKey`, and `TemplateString`.

**Source:** `javalidation-jackson/src/main/java/io/github/raniagus/javalidation/jackson/`

> ⚠️ Jackson groupId is **`tools.jackson`**, not `com.fasterxml.jackson`.

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
No manual registration needed.

---

## Default Wire Format

### `Result.Ok`

```json
{"value": {"name": "Alice", "age": 30}}
```

### `Result.Err`

```json
{
  "errors": {
    "rootErrors": [
      {"message": "io.github.raniagus.javalidation.constraints.NotNull.message", "args": []}
    ],
    "fieldErrors": {
      "email": [
        {"message": "io.github.raniagus.javalidation.constraints.Email.message", "args": []}
      ],
      "age": [
        {"message": "io.github.raniagus.javalidation.constraints.Min.message", "args": [18]}
      ]
    }
  }
}
```

With a `TemplateStringFormatter` configured (e.g. `MessageFormatTemplateStringFormatter`),
`TemplateString` serializes to the **formatted string** (not the code/args structure).

---

## Flattened Error Format

Activated via `JavalidationModule.builder().withFlattenedErrors()` or Spring property `flatten-errors: true`.

```json
{
  "": ["Root error message"],
  "email": ["Invalid email format"],
  "age": ["Must be at least 18"]
}
```

The empty-string key `""` represents root errors.

---

## Key Notation Options

Controls how `FieldKey` is serialized as a JSON key string.

| Mode | Code | Example `items[0].price` |
|------|------|--------------------------|
| Property-path (default) | `.withPropertyPathNotation()` / default | `items[0].price` |
| Dots | `.withDotNotation()` | `items.0.price` |
| Brackets | `.withBracketNotation()` | `[items][0][price]` |

---

## Custom `TemplateStringFormatter`

When a formatter is provided, `TemplateString` instances are serialized as plain formatted strings
(the message key is resolved). Without a formatter, they serialize as `{"message": "...", "args": [...]}`.

```java
TemplateStringFormatter myFormatter = ts ->
    messageSource.getMessage(ts.message(), ts.args(), locale);

JavalidationModule module = JavalidationModule.builder()
    .withTemplateStringFormatter(myFormatter)
    .build();
```

In Spring Boot, the `MessageSourceTemplateStringFormatter` bean is auto-registered and wired in automatically.

---

## Builder Reference

```java
JavalidationModule.builder()
    // Key notation (mutually exclusive)
    .withDotNotation()                         // dots only
    .withBracketNotation()                     // brackets only
    .withFieldKeyFormatter(FieldKeyFormatter)  // custom

    // TemplateString formatting
    .withTemplateStringFormatter(formatter)    // resolves messages to strings

    // Error structure
    .withFlattenedErrors()                     // flat {"": [], "field": []}

    // Low-level overrides
    .withFieldKeySerializer(ValueSerializer<FieldKey>)
    .withTemplateStringSerializer(ValueSerializer<TemplateString>)
    .withValidationErrorsSerializer(ValueSerializer<ValidationErrors>)
    .withResultSerializer(ValueSerializer<Result<?>>)
    .withResultDeserializerFactory(Function<JavaType, ValueDeserializer<Result<?>>>)

    .build()
```

⚠️ If you replace the `resultSerializer`, you must also replace the `resultDeserializerFactory`
to keep round-trip serialization working.

---

## Deserialization

The `StructuredResultDeserializer` reconstructs `Result<T>` from JSON:
- `{"value": …}` → `Ok<T>` (deserializes `value` as `T`)
- `{"errors": …}` → `Err<T>` (deserializes errors from structured DTO)

The deserializer is registered as a `Deserializers` resolver so that `Result<Person>`,
`Result<Order>`, etc. each get a properly typed deserializer.

---

## Internal Serializers (not usually customised)

| Class | Serializes | Default behaviour |
|-------|-----------|-------------------|
| `FieldKeySerializer` | `FieldKey` (as JSON key) | `PropertyPathNotationFormatter` |
| `TemplateStringSerializer` | `TemplateString` | `MessageFormatTemplateStringFormatter` (formats to string) |
| `StructuredResultSerializer` | `Result<?>` | `{"value": …}` or `{"errors": …}` |
| `FlattenedErrorsSerializer` | `ValidationErrors` | `{"": [], "field": []}` |
| `ValidationErrorsMixIn` | `ValidationErrors` default | Structured `rootErrors` / `fieldErrors` |
