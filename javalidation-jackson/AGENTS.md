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
// Default (structured format, property-path notation, MessageFormat formatter)
JavalidationModule module = JavalidationModule.getDefault();

// Custom builder
JavalidationModule module = JavalidationModule.builder()
    .withTemplateStringFormatter(myFormatter)  // custom i18n formatter
    .withFlattenedErrors()                      // flat {"": [], "field": []}
    .withDotNotation()                          // dots only: items.0.price
    .withBracketNotation()                      // brackets: [items][0][price]
    .withFieldKeyFormatter(formatter)           // custom FieldKeyFormatter
    .withFieldKeySerializer(serializer)         // custom ValueSerializer<FieldKey>
    .withTemplateStringSerializer(serializer)   // custom ValueSerializer<TemplateString>
    .withValidationErrorsSerializer(serializer) // custom ValidationErrors serializer
    .withResultSerializer(serializer)           // custom Result serializer
    .withResultDeserializerFactory(factory)     // custom Result deserializer factory
    .build();

// Register with ObjectMapper
JsonMapper mapper = JsonMapper.builder()
    .addModule(module)
    .build();
```

### JSON Wire Formats

**Structured (default):**
```json
// Ok
{"value": {"name": "Alice"}}

// Err
{
  "errors": {
    "rootErrors": [{"message": "some.key", "args": []}],
    "fieldErrors": {
      "email": [{"message": "some.key", "args": []}]
    }
  }
}
```

**Flattened (`withFlattenedErrors()`):**
```json
{
  "": ["Root error message"],
  "email": ["Invalid email format"]
}
```

### Key Notation Options

| Method | Example output for `items[0].price` |
|--------|--------------------------------------|
| `.withPropertyPathNotation()` (default) | `items[0].price` |
| `.withDotNotation()` | `items.0.price` |
| `.withBracketNotation()` | `[items][0][price]` |

## Feature Deep-Dive

- `.agents/features/jackson-integration.md`
