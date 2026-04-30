# javalidation-spring-boot-starter — Spring Boot 4.x Auto-Configuration

**Package:** `io.github.raniagus.javalidation.spring`
**Dependencies:** `javalidation-jackson`, `javalidation-jakarta-validator`, Spring Boot 4.x

Provides zero-configuration Spring Boot integration: auto-configures all beans needed to use
javalidation in a Spring MVC application.

## Source File Index

| File | Role |
|------|------|
| `JavalidationAutoConfiguration.java` | Core auto-config. Registers `FieldKeyFormatter`, `TemplateStringFormatter`, and `MessageSource` parent injection. |
| `JavalidationJacksonAutoConfiguration.java` | Jackson auto-config. Registers `JavalidationModule` and its serializer components. |
| `JavalidationValidatorAutoConfiguration.java` | Spring MVC validator auto-config. Registers `JavalidationSpringValidator` as `@Primary` and wires it into `WebMvcConfigurer`. |
| `JavalidationProperties.java` | `@ConfigurationProperties(prefix = "io.github.raniagus.javalidation")`. Three properties: `key-notation`, `use-message-source`, `flatten-errors`. |
| `JavalidationSpringValidator.java` | Implements Spring's `Validator` interface. Bridges `Validators.validate(...)` → Spring `Errors`. Also provides `toValidationErrors(Errors)` for the reverse. |
| `MessageSourceTemplateStringFormatter.java` | `TemplateStringFormatter` backed by Spring `MessageSource`. Falls back to `MessageFormat` if key not found. |
| `KeyNotation.java` | Enum: `PROPERTY_PATH` (default), `DOTS`, `BRACKETS`. |
| `EnableJavalidation.java` | `@ImportAutoConfiguration` annotation for test slices (`@WebMvcTest`, etc.) that disable auto-config. |

## Auto-Configuration Beans

### `JavalidationAutoConfiguration`

| Bean | Condition | Type |
|------|-----------|------|
| `propertyPathNotationFieldKeyFormatter` | `key-notation=property_path` (default) | `FieldKeyFormatter` |
| `dotNotationFieldKeyFormatter` | `key-notation=dots` | `FieldKeyFormatter` |
| `bracketNotationFieldKeyFormatter` | `key-notation=brackets` | `FieldKeyFormatter` |
| `defaultTemplateStringFormatter` | `use-message-source=false` | `TemplateStringFormatter` |
| `messageSourceTemplateStringFormatter` | `MessageSource` bean present + `use-message-source=true` (default) | `TemplateStringFormatter` |
| `javalidationMessageSourceParentConfigurer` | `messageSource` bean present + `use-message-source=true` | `BeanFactoryPostProcessor` |

### `JavalidationJacksonAutoConfiguration`

| Bean | Type |
|------|------|
| `javalidationModule` | `JavalidationModule` |
| `fieldKeySerializer` | `ValueSerializer<FieldKey>` |
| `templateStringValueSerializer` | `ValueSerializer<TemplateString>` |
| `flattenedErrorsSerializer` | `ValueSerializer<ValidationErrors>` (only if `flatten-errors=true`) |

### `JavalidationValidatorAutoConfiguration`

| Bean | Type |
|------|------|
| `javalidationSpringValidator` | `JavalidationSpringValidator` (`@Primary`) |
| `javalidationMvcConfigurer` | `WebMvcConfigurer` (wires validator into Spring MVC) |

## `JavalidationProperties` Reference

```yaml
io.github.raniagus.javalidation:
  key-notation: property_path   # property_path (default) | dots | brackets
  use-message-source: true      # true (default) | false
  flatten-errors: false         # false (default) | true
```

## `JavalidationSpringValidator`

```java
// Implements Spring's org.springframework.validation.Validator
validator.supports(Class<?> clazz)          → boolean  (delegates to Validators.hasValidator)
validator.validate(Object target, Errors)   → void     (calls Validators.validate, fills Errors)

// Static helper: Spring Errors → ValidationErrors
JavalidationSpringValidator.toValidationErrors(Errors errors) → ValidationErrors
```

## `@EnableJavalidation`

Use on test classes (e.g., `@WebMvcTest`) to import all three auto-configuration classes:
```java
@WebMvcTest(MyController.class)
@EnableJavalidation
class MyControllerTest { … }
```

Imports: `JavalidationAutoConfiguration`, `JavalidationJacksonAutoConfiguration`, `JavalidationValidatorAutoConfiguration`.

## MessageSource Integration

When `use-message-source=true` (default), the starter:
1. Registers a `BeanFactoryPostProcessor` that walks to the bottom of the `MessageSource` hierarchy
2. Injects `io/github/raniagus/javalidation/messages.properties` as a parent `ResourceBundleMessageSource`
3. This provides all 22 constraint keys with default English strings out of the box
4. User's own `messages.properties` takes precedence (it is higher in the hierarchy)
5. `MessageSourceTemplateStringFormatter` tries `MessageSource` first; falls back to raw `MessageFormat.format(key, args)` if the key is not found

## Test Conventions

**See `.agents/spring-boot-starter-tests.md` for the full guide.**

All test classes extend `AutoConfigurationTest` and use `@SpringBootTest(classes = TestApplication.class)` on nested static classes.

```bash
# All starter tests
./mvnw test -pl javalidation-spring-boot-starter

# One test class
./mvnw test -pl javalidation-spring-boot-starter \
  -Dtest=TemplateStringFormatterAutoConfigurationTest
```

## Feature Deep-Dive

- `.agents/features/spring-boot-starter.md`
- `.agents/spring-boot-starter-tests.md`
