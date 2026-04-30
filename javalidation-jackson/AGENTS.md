# javalidation-jackson — Jackson 3.x Integration

**Package:** `io.github.raniagus.javalidation.jackson`
**Dependencies:** `javalidation` (core), `tools.jackson.databind` (Jackson 3.x)

Provides a `JavalidationModule` for registering serializers/deserializers that handle `Result<T>`,
`ValidationErrors`, `FieldKey`, and `TemplateString` in Jackson `ObjectMapper`/`JsonMapper`.

> ⚠️ Jackson groupId is **`tools.jackson`**, not `com.fasterxml.jackson`.

## Source File Index

| File | Role |
|------|------|
| `JavalidationModule.java` | Main `SimpleModule` subclass. Entry point for all registrations. Has a `Builder`. |
| `JavalidationModule.Builder` | Fluent builder: notation, formatter, flattened/structured errors, custom serializers. |
| `StructuredResultSerializer.java` | Serializes `Result<T>` as `{"value": …}` (Ok) or `{"errors": …}` (Err). |
| `StructuredResultDeserializer.java` | Deserializes `{"value": …}` → `Ok`, `{"errors": …}` → `Err`. |
| `StructuredResultDeserializerResolver.java` | `Deserializers` impl that wires the deserializer per `JavaType`. |
| `FieldKeySerializer.java` | Serializes `FieldKey` as a JSON key (string). Delegates to `FieldKeyFormatter`. |
| `TemplateStringSerializer.java` | Serializes `TemplateString` as a formatted string. Delegates to `TemplateStringFormatter`. |
| `FlattenedErrorsSerializer.java` | Serializes `ValidationErrors` as `{"": […], "field": […]}` (flat map). |
| `ValidationErrorsMixIn.java` | Default mixin for `ValidationErrors` when no custom serializer is configured. |
| `StructuredErrorDto.java` | DTO for the structured errors representation during deserialization. |
| `StructuredFieldErrorDto.java` | DTO for individual field error entries during deserialization. |
| `StructuredValidationErrorsDto.java` | DTO combining root errors and field errors for deserialization. |

## Key Public API

### `JavalidationModule`

```java
// Default (structured Result format + property-path notation + MessageFormat formatter)
JavalidationModule module = JavalidationModule.getDefault();

// Custom builder
JavalidationModule module = JavalidationModule.builder()
    .withTemplateStringFormatter(myFormatter)  // custom i18n formatter (for ValidationErrors)
    .withFlattenedErrors()                      // flat {"": [], "field": []} for ValidationErrors
    .withDotNotation()                          // dots: items.0.price (react-hook-form compat)
    .withBracketNotation()                      // brackets: [items][0][price]
    .withFieldKeyFormatter(formatter)           // custom FieldKeyFormatter
    .withFieldKeySerializer(serializer)         // custom ValueSerializer<FieldKey>
    .withTemplateStringSerializer(serializer)   // custom ValueSerializer<TemplateString>
    .withValidationErrorsSerializer(serializer) // custom ValidationErrors serializer
    .withResultSerializer(serializer)           // custom Result serializer (⚠️ also update deserializer)
    .withResultDeserializerFactory(factory)     // custom Result deserializer factory
    .build();

// Register with ObjectMapper
JsonMapper mapper = JsonMapper.builder()
    .addModule(module)
    .build();
```

### Two Wire Formats

**`Result<T>` (internal backend traffic, round-trip):**
```json
// Ok
{"ok": true, "value": {"name": "Alice"}}

// Err — opaque code/args, fieldErrors as typed array
{
  "ok": false,
  "errors": {
    "rootErrors": [{"code": "io.github...NotNull.message", "args": []}],
    "fieldErrors": [
      {"key": ["email"], "errors": [{"code": "io.github...Email.message", "args": []}]},
      {"key": ["items", 0, "price"], "errors": [{"code": "io.github...Min.message", "args": [1]}]}
    ]
  }
}
```

**`ValidationErrors` (frontend/BFF, formatted messages):**
```json
// Structured layout (default)
{
  "rootErrors": ["Invalid request"],
  "fieldErrors": {
    "email": ["Must be a valid email address"],
    "items[0].price": ["Must be at least 1"]
  }
}

// Flattened layout (withFlattenedErrors())
{
  "": ["Invalid request"],
  "email": ["Must be a valid email address"],
  "items[0].price": ["Must be at least 1"]
}
```

### Key Notation Options for `ValidationErrors`

| Method | Example `items[0].price` | Compatible with |
|--------|--------------------------|-----------------|
| `.withPropertyPathNotation()` (default) | `items[0].price` | [conform](https://conform.guide/) |
| `.withDotNotation()` | `items.0.price` | [react-hook-form](https://react-hook-form.com/) |
| `.withBracketNotation()` | `[items][0][price]` | — |

## Feature Deep-Dive

- `.agents/features/jackson-integration.md`
