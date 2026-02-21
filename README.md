# javalidation

[![Maven](https://badges.mvnrepository.com/badge/io.github.raniagus/javalidation/badge.svg?label=Maven&color=orange)](https://mvnrepository.com/artifact/io.github.raniagus/javalidation)

A lightweight, dependency-free functional validation library for Java 21+, featuring a type-safe, serializable, and
fully i18n-compatible `Result<T>` with accumulated `ValidationErrors`.

## Features

**Validation:**
- **Error accumulation**: Collect multiple validation errors instead of failing fast
- **Dual error handling**: Separates expected validation failures (`JavalidationException`) from unexpected programming errors
- **Flexible validation styles**: Functional approach with `Result<T>` or imperative style with `JavalidationException`
- **Stream-based validation**: Specialized collectors (`toResultList()`, `toListOrThrow()`, `toPartitioned()`) for validating collections

**Type Safety & Composition:**
- **Type-safe error handling**: `Result<T>` sealed type (`Ok`/`Err`) ensures exhaustive handling at compile-time without `ClassCastException` risk
- **Rich functional API**: `map`, `flatMap`, `ensure`, `fold`, and applicative combiners for composing validations
- **Nested validation**: Prefix support for validating hierarchical data structures

**Integrations & i18n:**
- **Zero runtime dependencies**: Core library has no dependencies
- **Optional integrations**: Jackson 3.x serialization and Spring Boot 4.x autoconfiguration
- **Internationalization ready**: Template-based error messages with deferred formatting

**Modern Java:**
- Full use of Java 21 features (sealed types, pattern matching, records)
- Annotated with JSpecify nullness annotations for static analysis

## Installation

Javalidation is hosted in the Maven Central Repository as three separate modules. Also, snapshots of the latest commit
in the main branch can be consumed from Sonatype Central Portal repository[^1].

### Core Module

The core validation library with zero dependencies:

```xml
<dependency>
  <groupId>io.github.raniagus</groupId>
  <artifactId>javalidation</artifactId>
  <version>0.27.0</version>
</dependency>
```

### Jackson Module

For Jackson 3.x serialization support:

```xml
<dependency>
  <groupId>io.github.raniagus</groupId>
  <artifactId>javalidation-jackson</artifactId>
  <version>0.27.0</version>
</dependency>
```

### Jakarta Validation API + Processor

Library for code-generated `Validator<T>` interface and injector:

```xml
<dependency>
  <groupId>io.github.raniagus</groupId>
  <artifactId>javalidation-jakarta-validator</artifactId>
  <version>0.27.0</version>
</dependency>
```

Annotation processor for generating Validator implementations:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>io.github.raniagus</groupId>
                <artifactId>javalidation-jakarta-validator-processor</artifactId>
                <version>0.27.0</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

> [!TIP]
> Mark `target/generated-sources/annotations` directory as "Generated Sources Root" in IntelliJ to debug generated code.

### Spring Boot Starter

Provides Spring Boot 4.x autoconfiguration for:

- Jackson and MessageSource integration
- Using Validator with `@Valid` annotation

```xml
<dependency>
  <groupId>io.github.raniagus</groupId>
  <artifactId>javalidation-spring-boot-starter</artifactId>
  <version>0.27.0</version>
</dependency>
```

The Spring Boot starter includes the core module automatically, and you can opt in to the Jackson and/or
Jakarta Validation modules, so their corresponding features will be autoconfigured.

## Quick Start

```java
import io.github.raniagus.javalidation.*;

record Person(String name, int age, String email) {}

// Validate multiple fields and accumulate ALL errors
Result<Person> result = validateName(name)
    .and(validateAge(age))
    .and(validateEmail(email))
    .combine(Person::new);

// Handle result with pattern matching
String message = switch (result) {
    case Result.Ok(Person p) -> "Valid person: " + p.name();
    case Result.Err(ValidationErrors errors) -> "Validation failed: " + errors;
};

// Or throw if invalid
Person validPerson = result.getOrThrow();

// Individual field validators
static Result<String> validateName(String name) {
    return Result.ok(name)
        .ensureAt(n -> n != null && !n.isEmpty(), "name", "Name is required")
        .ensureAt(n -> n.length() >= 2, "name", "Name must be at least {0} characters", 2);
}

static Result<Integer> validateAge(int age) {
    return Result.ok(age)
        .ensureAt(a -> a >= 18, "age", "Must be {0} or older", 18)
        .ensureAt(a -> a <= 120, "age", "Age must be less than {0}", 120);
}

static Result<String> validateEmail(String email) {
    return Result.ok(email)
        .ensureAt(e -> e != null && e.contains("@"), "email", "Invalid email format");
}
```

**Key benefits:**
- All three validations run independently – **all errors are accumulated**
- Message templates support parameters: `"Must be {0} or older", 18`
- Type-safe with sealed types - compiler ensures you handle both Ok and Err cases

## Core Concepts

### Result<T> - The Railway

`Result<T>` is a sealed interface representing either success or validation failure:

```java
// Create results
Result<String> success = Result.ok("value");
Result<String> failure = Result.error("email", "Invalid format");

// Extract values
String value = success.getOrThrow();              // "value"
String fallback = failure.getOrElse("default");   // "default"

// Transform success values
Result<Integer> age = Result.ok("25")
    .map(value -> {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw JavalidationException.of("Not a valid number: {0}", value);
        }
    });  // Ok(25)

// Chain dependent validations (stops at first error)
Result<User> user = validateEmail(email)
    .flatMap(e -> findUserByEmail(e))
    .flatMap(u -> validateUserActive(u));
```

`Result<T>` has two variants:
- `Ok<T>(T value)` - successful validation
- `Err<T>(ValidationErrors errors)` - failed validation with accumulated errors

### ValidationErrors

An immutable collection of validation errors with two categories:

```java
// Root-level errors (not tied to specific fields)
ValidationErrors rootError = ValidationErrors.of("User validation failed");

// Field-specific errors
ValidationErrors fieldError = ValidationErrors.at("email", "Invalid email format");

// With parameters
ValidationErrors withParams = ValidationErrors.at("age", "Must be at least {0}", 18);

// Multiple errors
ValidationErrors errors = ValidationErrors.at("email", "Invalid format")
    .mergeWith(ValidationErrors.at("age", "Too young"));
```

### Error Accumulation

Unlike traditional fail-fast validation, javalidation accumulates multiple errors:

```java
// Fail-fast approach (traditional) - only gets the first error
if (name.isEmpty()) throw new Exception("Name required");
if (age < 18) throw new Exception("Must be 18+");  // Never reached if name is empty

// Error accumulation (javalidation) - gets ALL errors
Result<Person> result = validateName(name)
    .and(validateAge(age))
    .and(validateEmail(email))
    .combine(Person::new);

// If multiple validations fail, all errors are collected:
// {
//   "name": ["Name is required"],
//   "age": ["Must be 18 or older"],
//   "email": ["Invalid email format"]
// }
```

### Separating Expected and Unexpected Errors

Javalidation distinguishes between **expected validation failures** and **unexpected programming errors**:

**Expected errors** (`JavalidationException`):
- Represent business validation failures (invalid user input, business rule violations)
- Automatically caught by `Result` transformation methods (`.map()`, `.flatMap()`)
- Safe to return to API clients as structured feedback
- Example: "Email format is invalid", "Insufficient inventory"

**Unexpected errors** (all other exceptions):
- Represent programming bugs or infrastructure failures
- Propagate normally through the call stack
- Should be logged and monitored
- Result in generic 500-level responses to clients
- Example: `NullPointerException`, `IOException`, `SQLException`

```java
Result<User> result = Result.ok(userId)
    .map(id -> userRepository.findByIdOrThrow(id))  // throws JavalidationException → becomes Err
    .flatMap(u -> validateUserStatus(u));           // continues on error track

// But if findByIdOrThrow throws NullPointerException, it propagates normally (indicates a bug)
```

This design enables **type-safe error accumulation for business validation** while preserving **fail-fast debugging for programming errors**.

## Functional Validation composition

Use `Result<T>` and functional composition for pure validation without side effects:

### Simple POJO Validation

```java
record Person(String name, int age, String email, String password) {}

static Result<String> validateName(String fieldName, String value) {
    return Result.ok(value)
        .ensureAt(n -> n != null && !n.isEmpty(), fieldName, "Name is required")
        .ensureAt(n -> n.length() >= 2, fieldName, "Name must be at least 2 characters")
        .ensureAt(n -> n.length() <= 50, fieldName, "Name must not exceed 50 characters");
}

static Result<Integer> validateAge(String fieldName, int value) {
    return Result.ok(value)
        .ensureAt(a -> a >= 18, fieldName, "Must be at least 18 years old")
        .ensureAt(a -> a <= 120, fieldName, "Age cannot exceed 120");
}

static Result<String> validateEmail(String fieldName, String value) {
    return Result.ok(value)
        .ensureAt(e -> e != null && !e.isEmpty(), fieldName, "Email is required")
        .ensureAt(e -> e.matches("^[^@]+@[^@]+\\.[^@]+$"), fieldName, "Invalid email format");
}

static Result<String> validatePassword(String fieldName, String value) {
    return Result.ok(value)
        .ensureAt(p -> p != null && p.length() >= 8, fieldName, "Password must be at least 8 characters")
        .ensureAt(p -> p.matches(".*[A-Z].*"), fieldName, "Password must contain uppercase letter")
        .ensureAt(p -> p.matches(".*[0-9].*"), fieldName, "Password must contain a number");
}

// Combine all validations
static Result<Person> validatePerson(String name, int age, String email, String password) {
    return validateName("name", name)
        .and(validateAge("age", age))
        .and(validateEmail("email", email))
        .and(validatePassword("password", password))
        .combine(Person::new);
}
```

### Nested Object Validation

Use `withPrefix()` to namespace errors for nested structures:

```java
record Message(Person from, Person to, String text) {}

static Result<Message> validateMessage(Result<Person> from, Result<Person> to, String text) {
    return from.withPrefix("from")
        .and(to.withPrefix("to"))
        .and(validateText("text", text))
        .combine(Message::new);
}


// Allows reusing an existing validation
Result<Person> result = validatePerson(name, age, email);

// With nested validation
Result<Message> result = validateMessage(
        validatePerson(fromName, fromAge, fromEmail),
        validatePerson(toName, toAge, toEmail),
        messageText
);
```

### Collection Validation

Use `ResultCollector.toResultList()` with `withIndex()` for automatic indexing and `withPrefix()` for nested collections:

```java
import static io.github.raniagus.javalidation.ResultCollector.*;

record Order(List<Item> items) {}
record Item(String name, double price) {}

public Result<Order> validateOrder(Order order) {
    return Result.ok(order)
            .flatMap(o -> validateItems("items", o.items()))
            .map(Order::new);
}

public Result<List<Item>> validateItems(String fieldName, List<Item> items) {
    return Result.ok(items)
            .ensureAt(i -> i != null && !i.isEmpty(), fieldName, "Order must contain at least one item")
            .flatMap(i -> i.stream()
                    .map(this::validateItem)
                    .collect(withPrefix(fieldName, withIndex(toResultList())))
            );
}

public Result<Item> validateItem(Item item) {
    return validateItemName(item.name())
            .and(validatePrice(item.price()))
            .combine((name, price) -> item);
}

// Example Output: { "items[0].name": ["Name is required"], "items[1].price": ["Price must be a positive number"] }
```

## Imperative Validation

### Throwing Errors

Use `JavalidationException` for imperative validation where you want to stop the execution immediately:

```java
public User findUserByEmail(String email) {
    User user = database.findByEmail(email);
    if (user == null) {
        throw JavalidationException.at("email", "User not found");
    }
    return user;
}

public void checkInventory(String productId, int quantity) {
    int available = inventoryService.getAvailable(productId);
    if (quantity > available) {
        throw JavalidationException.at("quantity", 
            "Only {0} units available", available);
    }
}

public void processOrder(Order order) {
    // Check user exists
    User user = findUserByEmail(order.email());
    
    // Check inventory before processing order
    checkInventory(order.productId(), order.quantity());
    
    // Process order...
}
```

### Collecting Stream Results

Use `toListOrThrow()` to collect stream results imperatively:

```java
import static io.github.raniagus.javalidation.ResultCollector.*;

public List<User> validateAndProcessUsers(List<User> users) {
    // Validates all items, accumulates errors, then throws if any failed
    return users.stream()
        .map(this::validateAndCreateUser)
        .collect(withPrefix("users", withIndex(toListOrThrow())));
}

private User validateAndCreateUser(User user) {
    if (userRepository.existsByEmail(user.email())) {
        throw JavalidationException.at("email", "Email already registered");
    }

    if (!emailVerificationService.verify(user.email())) {
        throw JavalidationException.at("email", "Email could not be verified");
    }

    return database.create(user);
}

// If any validation fails, throws JavalidationException with all errors:
// "users[0].email": ["Email could not be verified"],
// "users[2].email": ["Email already registered"]
```

For more complex scenarios with imperative state validation that ensure not throwing an exception on the first error,
please check the [Advanced Patterns](#advanced-patterns) section.

## Full Example

Combining business rules validation and internal checks in a REST API:

```java
// Controller
public void registerUser(Context ctx) {
    if (!startsWith(ctx.getHeader("Content-Type"), "application/json")) {
        // 415 Unsupported Media Type: Request body is not JSON
        return ctx.status(415).body("Invalid Content-Type");
    }

    try {
        UserRegistrationRequest request = ctx.bodyAsClass(UserRegistrationRequest.class);
        return switch (validator.validateRegistrationRequest(request)) {
            // 2xx Success
            case Result.Ok(User user) -> ctx.ok(userService.createUser(user));

            // 422 Unprocessable Content: Request is well-formed but violates business rules
            case Result.Err(ValidationErrors errors) -> ctx.status(422).body(errors);
        };

    } catch (JavalidationException e) {
        // 409 Conflict: Request is valid but conflicts with the current state
        return ctx.status(409).body(e.getErrors());

    } catch (JsonProcessingException e) {
        // 400 Bad Request: Request is malformed for specified Content-Type
        return ctx.status(400).body("Invalid request format");

    } catch (Exception e) {
        // 500: Unexpected error - log and alert
        logger.error("Unexpected error during registration", e);
        return ctx.status(500).body("Internal server error");
    }
}

// Business rules validator
public class UserValidator {
    public Result<User> validateRegistrationRequest(UserRegistrationRequest request) {
        return validateName("name", request.name())
            .and(validateAge("age", request.age()))
            .and(validateEmail("email", request.email()))
            .and(validatePassword("password", request.password()))
            .combine(User::new);
    }

    // validateName(), validateAge(), etc.
}

// Service with internal checks
public class UserService {
    public User createUser(User user) {
        // Check if email already exists (database query)
        if (userRepository.existsByEmail(user.getEmail())) {
            throw JavalidationException.at("email", "Email already registered");
        }

        // Check external validation service
        if (!emailVerificationService.verify(user.getEmail())) {
            throw JavalidationException.at("email", "Email could not be verified");
        }

        return userRepository.save(user);
    }
}
```

## Integrations

### Jackson Serialization

Configure Jackson to serialize `Result<T>` and `ValidationErrors`:

```java
import io.github.raniagus.javalidation.jackson.JavalidationModule;
import com.fasterxml.jackson.databind.json.JsonMapper;

// Default: structured errors
JavalidationModule module = JavalidationModule.getDefault();

// With opt-in options
JavalidationModule module = JavalidationModule.builder()
        .withFlattenedErrors() // Flattened errors (both root and field errors in a single object)
        .withDotNotation()     // Dot notation for field errors (eg: "users.0.address")
        .withBracketNotation() // Bracket notation for field errors (eg: "users[0][address]")
        .build();

// Register module
JsonMapper mapper = JsonMapper.builder()
    .addModule(module)
    .build();
```

**ValidationErrors JSON (default structure):**
```json
{
  "rootErrors": ["Order validation failed"],
  "fieldErrors": {
    "email": ["Invalid email format"],
    "age": ["Must be at least 18"],
    "addresses[0].street": ["Street is required"],
    "addresses[0].city": ["City is required"]
  }
}
```

**ValidationErrors JSON (flattened):**
```json
{
  "": ["Order validation failed"],
  "email": ["Invalid email format"],
  "age": ["Must be at least 18"],
  "addresses[0].street": ["Street is required"],
  "addresses[0].city": ["City is required"]
}
```

**ValidationErrors JSON (with dot notation):**
```json
{
  "rootErrors": ["Order validation failed"],
  "fieldErrors": {
    "email": ["Invalid email format"],
    "age": ["Must be at least 18"],
    "addresses.0.street": ["Street is required"],
    "addresses.0.city": ["City is required"]
  }
}
```

**ValidationErrors JSON (with bracket notation):**
```json
{
  "rootErrors": ["Order validation failed"],
  "fieldErrors": {
    "email": ["Invalid email format"],
    "age": ["Must be at least 18"],
    "addresses[0][street]": ["Street is required"],
    "addresses[0][city]": ["City is required"]
  }
}
```

**Result<T> serialization:**
```java
Result<User> success = Result.ok(new User("Alice", 30));
// {"ok": true, "value": {"name": "Alice", "age": 30}}

Result<User> failure = Result.error("email", "Invalid format");
// {"ok": false, "errors": {"fieldErrors": {"email": ["Invalid format"]}}}
```

### Spring Boot Integration

#### Configuration (optional)
```yaml
# application.yml
io.github.raniagus.javalidation:
  key-notation: property_path # Choose how to serialize field keys (property_path, dots or brackets) 
  use-message-source: true    # Use Spring MessageSource for i18n (default: true)
  flatten-errors: false       # Flatten JSON error structure (default: false)
```

#### Internationalization

Create message files:
```properties
# messages_en.properties
user.name.required=Name is required
user.age.minimum=Must be at least {0} years old
```

```properties
# messages_es.properties
user.name.required=El nombre es obligatorio
user.age.minimum=Debe tener al menos {0} años
```

Use message keys in validation:
```java
Result<User> validateUser(String name, String age) {
    return Result.ok(name)
            .ensureAt(n -> n != null && !n.isEmpty(), "name", "user.name.required")
            .and(Result.ok(age)
                    .ensureAt(a -> a >= 18, "age", "user.age.minimum", 18))
            .combine(User::new);
}
```

Spring automatically formats messages based on the request locale.

#### Bean validation

When including `javalidation-jakarta-validator` dependency and `javalidation-jakarta-validator-processor`, bean
validation is autoconfigured to use `Validators.validate(T)`, which is a compile-time generated service locator for
different `Validator<T>` instances, to generate `BindingResult`s or throw `ValidationException`s.

To tell the annotation processor to generate a `Validator<T>`, annotate any method with `@Valid`:

```java
@PostMapping("/register")
public ResponseEntity<?> registerUser(@RequestBody @Valid UserDto request) {
    return ResponseEntity.ok(userService.createUser(user));
}
```

**Example:**

```java
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;

public record UserDto(
        @NotBlank String name,
        @Email String email,
        @NotEmpty List<@NotNull OrderDto> orders,
        @NotNull Map<@NotBlank String, @NotNull @Min(0) Integer> inventory
) {
    public record OrderDto(
            @NotEmpty String productId,
            @Min(0) int quantity
    ) {}
}
```
<details>

<summary>Generated <code>UserDtoValidator</code></summary>

```java
import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class UserDtoValidator implements Validator {
    private final Validator ordersItemValidator = new UserDto$OrderDtoValidator();

    @Override
    public void validate(Validation validation, UserDto root) {
        validation.withField("name", () -> {
            var name = root.name();
            if (name == null || name.isBlank()) {
                validation.addError("must not be blank");
                return;
            }
        });
        validation.withField("email", () -> {
            var email = root.email();
            if (email == null) return;
            if (!email.toString().matches("^[^@]+@[^@]+\\.[^@]+$")) {
                validation.addError("must be a well-formed email address");
            }
        });
        validation.withField("orders", () -> {
            var orders = root.orders();
            if (orders == null || orders.isEmpty()) {
                validation.addError("must not be empty");
                return;
            }
            validation.withEach(orders, ordersItem -> {
                if (ordersItem == null) {
                    validation.addError("must not be null");
                    return;
                }
                ordersItemValidator.validate(validation, ordersItem);
            });
        });
        validation.withField("inventory", () -> {
            var inventory = root.inventory();
            if (inventory == null) {
                validation.addError("must not be null");
                return;
            }
            inventory.forEach((inventoryKey, inventoryValue) -> {
                if (inventoryKey == null || inventoryKey.isBlank()) {
                    validation.addError("must not be blank");
                    return;
                }
                validation.withField(inventoryKey, () -> {
                    if (inventoryValue == null) {
                        validation.addError("must not be null");
                        return;
                    }
                    if (!(inventoryValue >= 0)) {
                        validation.addError("must be greater than or equal to {0}", 0);
                    }
                });
            });
        });
    }
}
```

</details>

> [!IMPORTANT]
> - Only Records can be annotated with `@Valid`. Sealed Interfaces are only supported if all permitted subtypes are Records.
> - Validation groups are not supported. All constraints are always applied, regardless of the `groups` attribute.
> - Using `@Valid` on a `Map` key results in undefined field error namespacing behavior.

#### Full Example

```java
record UserRegistrationRequest(
        @NotBlank("user.name.required") String name,
        @Email("user.email.invalid") String email,
        @Min(value = 18, message = "user.age.minimum") int age
) {}
```

```java
@PostMapping("/register")
public ResponseEntity<?> registerUser(@RequestBody @Valid UserRegistrationRequest request) {
    return ResponseEntity.ok(userService.createUser(user));
}

@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<?> handleValidation(MethodArgumentNotValidException e) {
    return ResponseEntity.status(422).body(JavalidationSpringValidator.toValidationErrors(e));
}

@ExceptionHandler(JavalidationException.class)
public ResponseEntity<?> handleJavalidation(JavalidationException e) {
    return ResponseEntity.status(409).body(e.getErrors());
}

@ExceptionHandler(Exception.class)
public ResponseEntity<?> handleUnexpectedError(Exception e) {
    logger.error("Unexpected error during registration", e);
    return ResponseEntity.status(500).body("Internal server error");
}
```

```properties
user.name.required=Name is required
user.email.invalid=Invalid email format
user.age.minimum=Must be at least {0} years old
```

## Advanced Patterns

### Partial Success

Process valid items even when some fail validation:

```java
import static io.github.raniagus.javalidation.ResultCollector.*;

public ProcessOrderResult processOrder(Order order) {
    PartitionedResult<List<Item>> partitioned = order.items().stream()
            .map(this::validateItem)
            .collect(withIndex(toPartitioned()));

    // Continue with valid items
    processValidItems(partitioned.value());

    // Return partial success with errors for invalid items
    return new ProcessOrderResult(partitioned.errors());
}

public Result<Item> validateItem(Item item) {
    // ...
}
```

### Complex Internal State Validation

For complex validation scenarios requiring mutable state accumulation, use `Validation.create()`:

```java
import static io.github.raniagus.javalidation.ResultCollector.*;

record Order(
        List<Item> items,
        PaymentMethod paymentMethod
) {}

public void validateComplexOrder(Order order) {
    Validation validation = Validation.create();

    // Basic root checks
    if (order == null) {
        validation.addError("Order is required");
    }

    // Field validation by calling utility method
    validation.withField("items", () -> {
        validateItemsList(validation, order.items());
    });

    // Additional cross-field validation
    double total = order.items().stream().mapToDouble(Item::price).sum();
    if (total > 10000 && order.paymentMethod().equals(PaymentMethod.CASH)) {
        validation.addErrorAt("paymentMethod", 
            "Cash payments limited to {0} for orders over {1}", 1000, 10000);
    }

    // Throw if any errors accumulated
    validation.check();
}

public void validateItemsList(Validation validation, List<Item> items) {
    // Basic field checks
    if (items.isEmpty()) {
        validation.addError("Order must contain at least one item"); // "items": ["Order must contain at least one item"]
    }

    // Validate items and accumulate errors with index
    validation.withEach(items, (item) -> {
        if (item == null) {
            validation.addError("Item is required"); // "items[0]", "items[1]", ...
        }
    });

    // Alternative: the same approach but with ResultCollector.into(Validation) for Stream<Result<T>>
    items.stream()
            .map(this::validateItemResult) // Must return Result<T>
            .collect(withIndex(into(validation))); // Mutates Validation instance
}
```

### Imperative Validation inside `Result<T>` chain

```java
Result<Order> validateOrder(Order order) {
    return Result.ok(order)
            .check((o, validation) -> {
                if (o.items().isEmpty()) {
                    validation.addError("Order must contain at least one item");
                }

                double total = o.items().stream().mapToDouble(Item::price).sum();
                if (total > 10000 && o.paymentMethod().equals(PaymentMethod.CASH)) {
                    validation.addErrorAt("paymentMethod",
                            "Cash payments limited to {0} for orders over {1}", 1000, 10000);
                }
            });
}
```

## API Reference

### Result<T>

| Method                                           | Description                               |
|--------------------------------------------------|-------------------------------------------|
| `of(Supplier<T>)`/ `of(Runnable)`                | Wrap supplier or runnable in try-catch    |
| `ok(T)`                                          | Create successful result                  |
| `error(String, Object...)`                       | Create failed result with root error      |
| `errorAt(String, String, Object...)`             | Create failed result with field error     |
| `error(ValidationErrors)`                        | Create failed result from existing errors |
| `map(Function)`                                  | Transform success value                   |
| `flatMap(Function)`                              | Chain validations                         |
| `ensure(Predicate, String, Object...)`           | Conditional validation                    |
| `ensureAt(Predicate, Object, String, Object...)` | Conditional validation (for fields)       |
| `check(BiConsumer)`                              | Add imperative validation logic           |
| `and(Result)`                                    | Start applicative combiner chain          |
| `or(Result)` / `or(Supplier)`                    | Provide fallback                          |
| `fold(Function, Function)`                       | Handle both cases                         |
| `getOrThrow()`                                   | Extract value or throw                    |
| `getOrElse(T)` / `getOrElse(Supplier)`           | Extract value or default                  |
| `withPrefix(String)`                             | Namespace errors for nested objects       |

### ValidationErrors

| Method                          | Description                    |
|---------------------------------|--------------------------------|
| `empty()`                       | Create empty errors            |
| `of(String, Object...)`         | Create with single root error  |
| `of(String, String, Object...)` | Create with single field error |
| `mergeWith(ValidationErrors)`   | Merge two error sets           |
| `withPrefix(String)`            | Add prefix to all fields       |
| `isEmpty()` / `isNotEmpty()`    | Check if errors exist          |
| `count()`                       | Total number of errors         |

### Validation

| Method                                      | Description                                                |
|---------------------------------------------|------------------------------------------------------------|
| `create()`                                  | Create new empty validation                                |
| `addError(String, Object...)`               | Add root-level error                                       |
| `addErrorAt(Object, String, Object...)`     | Add field-specific error                                   |
| `addAll(ValidationErrors)`                  | Merge errors                                               |
| `addAll(ValidationErrors, Object[])`        | Merge errors with prefix                                   |
| `withField(Object, Runnable)`               | Scope validation under a field prefix                      |
| `withEach(Iterable, Consumer / BiConsumer)` | Scope validation over a collection (optionally with index) |
| `check()`                                   | Throw if errors exist                                      |
| `asResult(Supplier)`                        | Convert to Result                                          |

### ResultCollector

| Method                                   | Description                                                      |
|------------------------------------------|------------------------------------------------------------------|
| `toResultList()` / `toResultList(int)`   | Returns `Result<List<T>>` (functional style)                     |
| `toListOrThrow()` / `toListOrThrow(int)` | Returns `List<T>` or throws (imperative style)                   |
| `toPartitioned()` / `toPartitioned(int)` | Returns valid items + errors (partial success)                   |
| `into(Validation)`                       | Accumulates errors into existing `Validation` (mutable state)    |
| `withIndex(Collector<...>)`              | Wraps collector to add `[0]`, `[1]`, etc. prefixes               |
| `withPrefix(String, Collector<...>)`     | Wraps collector to add field prefix to all errors                |

> [!NOTE]
> The optional `int` parameter provides an `initialCapacity` hint for ArrayList optimization.

### JavalidationException

| Method                          | Description                       |
|---------------------------------|-----------------------------------|
| `of(String, Object...)`         | Create with root error            |
| `at(String, String, Object...)` | Create with field error           |
| `of(ValidationErrors)`          | Create from ValidationErrors      |
| `getErrors()`                   | Get accumulated errors            |

## License

This project is licensed under the MIT License.

[^1]: [How to consume snapshot releases](https://central.sonatype.org/publish/publish-portal-snapshots/#consuming-snapshot-releases-for-your-project)
