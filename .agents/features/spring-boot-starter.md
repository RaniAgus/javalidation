# Feature: Spring Boot Starter

`javalidation-spring-boot-starter` provides zero-configuration Spring Boot 4.x integration.
When present on the classpath, it auto-configures all beans needed for validation, serialization,
and `MessageSource`-backed i18n.

**Source:** `javalidation-spring-boot-starter/src/main/java/io/github/raniagus/javalidation/spring/`

---

## What Gets Auto-Configured

Three `@AutoConfiguration` classes are registered:

| Class | Activated when |
|-------|---------------|
| `JavalidationAutoConfiguration` | always |
| `JavalidationJacksonAutoConfiguration` | `JsonMapper` + `javalidation-jackson` on classpath |
| `JavalidationValidatorAutoConfiguration` | `Validators` + Spring MVC on classpath |

---

## Configuration Properties

```yaml
io.github.raniagus.javalidation:
  key-notation: property_path   # property_path (default) | dots | brackets
  use-message-source: true      # true (default) | false
  flatten-errors: false         # false (default) | true
```

### `key-notation`

Controls how `FieldKey` paths are serialized to JSON keys.

| Value | Example |
|-------|---------|
| `property_path` (default) | `items[0].price` |
| `dots` | `items.0.price` |
| `brackets` | `[items][0][price]` |

### `use-message-source`

When `true` (default), error message keys (e.g. `io.github.raniagus.javalidation.constraints.NotNull.message`)
are resolved via Spring `MessageSource`. The library's built-in English messages are injected automatically
as a parent `MessageSource`.

When `false`, raw keys are passed through `MessageFormat.format(key, args)`. Library keys will appear
as opaque strings.

### `flatten-errors`

When `false` (default), `ValidationErrors` serializes as:
```json
{"rootErrors": [...], "fieldErrors": {"field": [...]}}
```

When `true`, it serializes as:
```json
{"": [...], "field": [...]}
```

---

## Spring MVC Integration

`JavalidationSpringValidator` is registered as a `@Primary` Spring MVC `Validator`. It bridges:
- `Validators.hasValidator(clazz)` → `supports(clazz)`
- `Validators.validate(target)` → fills `Errors`

It is also wired into `WebMvcConfigurer.getValidator()` so that Spring MVC uses it for `@Valid`-annotated
controller method parameters automatically.

### Manual usage

```java
@RestController
public class UserController {
    private final JavalidationSpringValidator validator;

    @PostMapping("/users")
    public ResponseEntity<?> create(@RequestBody CreateUserRequest request, BindingResult result) {
        validator.validate(request, result);
        if (result.hasErrors()) {
            ValidationErrors errors = JavalidationSpringValidator.toValidationErrors(result);
            return ResponseEntity.badRequest().body(errors);
        }
        // ...
    }
}
```

### Reverse conversion

```java
// Spring Errors → ValidationErrors (for bridging from Spring MVC to javalidation)
ValidationErrors errors = JavalidationSpringValidator.toValidationErrors(bindingResult);
```

---

## MessageSource Integration Details

The `javalidationMessageSourceParentConfigurer` bean is a `BeanFactoryPostProcessor` that:
1. Finds the application's `messageSource` bean
2. Walks to the bottom of the `HierarchicalMessageSource` chain
3. Injects a `ResourceBundleMessageSource` pointing to
   `io/github/raniagus/javalidation/messages.properties` as the parent

This means all 22 library constraint keys have default English messages without any user configuration.
To **override** a key, define it in your own `messages.properties` (it takes precedence).

`MessageSourceTemplateStringFormatter` behavior:
1. Try `messageSource.getMessage(key, args, locale)`
2. If `NoSuchMessageException`, fall back to `MessageFormat.format(key, args)` — so custom `MessageFormat`
   patterns (e.g. `"Hello {0}!"`) work even without a `messages.properties` entry.

---

## `@EnableJavalidation` for Test Slices

Test slices like `@WebMvcTest` disable Spring Boot auto-configuration. Use `@EnableJavalidation`
to re-import the three auto-configuration classes:

```java
@WebMvcTest(MyController.class)
@EnableJavalidation
class MyControllerTest {
    @Autowired
    MockMvc mockMvc;

    // Javalidation MessageSource, Jackson module, and Spring validator are all configured
}
```

Without `@EnableJavalidation`, the Jackson module won't be applied and validation won't run.

---

## Exclusions

To disable specific auto-configurations:

```properties
spring.autoconfigure.exclude=\
  io.github.raniagus.javalidation.spring.JavalidationJacksonAutoConfiguration,\
  io.github.raniagus.javalidation.spring.JavalidationValidatorAutoConfiguration
```

---

## Testing Auto-Configuration

See `.agents/spring-boot-starter-tests.md` for the full guide.

Key pattern: all test classes **extend `AutoConfigurationTest`** and use nested static classes
with `@SpringBootTest(classes = TestApplication.class)`.

```bash
# All starter tests
./mvnw test -pl javalidation-spring-boot-starter

# One test class
./mvnw test -pl javalidation-spring-boot-starter \
  -Dtest=TemplateStringFormatterAutoConfigurationTest
```
