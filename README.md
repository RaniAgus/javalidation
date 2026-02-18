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
- **Rich functional API**: `map`, `flatMap`, `filter`, `fold`, and applicative combiners for composing validations
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
  <version>0.20.0</version>
</dependency>
```

### Jackson Module

For Jackson 3.x serialization support:

```xml
<dependency>
  <groupId>io.github.raniagus</groupId>
  <artifactId>javalidation-jackson</artifactId>
  <version>0.20.0</version>
</dependency>
```

### Jakarta Validation API + Processor (Experimental)

For Bean Validation support:

```xml
<dependency>
  <groupId>io.github.raniagus</groupId>
  <artifactId>javalidation-jakarta-validator</artifactId>
  <version>0.20.0</version>
</dependency>
```

Add the annotation processor to your compiler configuration:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>io.github.raniagus</groupId>
                <artifactId>javalidation-jakarta-validator-processor</artifactId>
                <version>0.20.0</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

### Spring Boot Starter

Provides Spring Boot 4.x autoconfiguration for:

- Jackson and MessageSource integration
- Using Validator with `@Valid` annotation

```xml
<dependency>
  <groupId>io.github.raniagus</groupId>
  <artifactId>javalidation-spring-boot-starter</artifactId>
  <version>0.20.0</version>
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
        .filter(n -> n != null && !n.isEmpty(), "name", "Name is required")
        .filter(n -> n.length() >= 2, "name", "Name must be at least {0} characters", 2);
}

static Result<Integer> validateAge(int age) {
    return Result.ok(age)
        .filter(a -> a >= 18, "age", "Must be {0} or older", 18)
        .filter(a -> a <= 120, "age", "Age must be less than {0}", 120);
}

static Result<String> validateEmail(String email) {
    return Result.ok(email)
        .filter(e -> e != null && e.contains("@"), "email", "Invalid email format");
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
Result<String> failure = Result.err("email", "Invalid format");

// Extract values
String value = success.getOrThrow();              // "value"
String fallback = failure.getOrElse("default");   // "default"

// Transform success values
Result<Integer> age = Result.ok("25")
    .map(value -> {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw JavalidationException.ofRoot("Not a valid number: {0}", value);
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
ValidationErrors rootError = ValidationErrors.ofRoot("User validation failed");

// Field-specific errors
ValidationErrors fieldError = ValidationErrors.ofField("email", "Invalid email format");

// With parameters
ValidationErrors withParams = ValidationErrors.ofField("age", "Must be at least {0}", 18);

// Multiple errors
ValidationErrors errors = ValidationErrors.ofField("email", "Invalid format")
    .mergeWith(ValidationErrors.ofField("age", "Too young"));
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

## Common Patterns

### Business Rules Validation

Use `Result<T>` and functional composition for pure validation without side effects:

#### Simple POJO Validation

```java
record Person(String name, int age, String email, String password) {}

static Result<String> validateName(String name) {
    return Result.ok(name)
        .filter(n -> n != null && !n.isEmpty(), "name", "Name is required")
        .filter(n -> n.length() >= 2, "name", "Name must be at least 2 characters")
        .filter(n -> n.length() <= 50, "name", "Name must not exceed 50 characters");
}

static Result<Integer> validateAge(int age) {
    return Result.ok(age)
        .filter(a -> a >= 18, "age", "Must be at least 18 years old")
        .filter(a -> a <= 120, "age", "Age cannot exceed 120");
}

static Result<String> validateEmail(String email) {
    return Result.ok(email)
        .filter(e -> e != null && !e.isEmpty(), "email", "Email is required")
        .filter(e -> e.matches("^[^@]+@[^@]+\\.[^@]+$"), "email", "Invalid email format");
}

static Result<String> validatePassword(String password) {
    return Result.ok(password)
        .filter(p -> p != null && p.length() >= 8, "password", "Password must be at least 8 characters")
        .filter(p -> p.matches(".*[A-Z].*"), "password", "Password must contain uppercase letter")
        .filter(p -> p.matches(".*[0-9].*"), "password", "Password must contain a number");
}

// Combine all validations
static Result<Person> validatePerson(String name, int age, String email, String password) {
    return validateName(name)
        .and(validateAge(age))
        .and(validateEmail(email))
        .and(validatePassword(password))
        .combine(Person::new);
}
```

#### Nested Object Validation

Use `withPrefix()` to namespace errors for nested structures:

```java
record Address(String street, String city, String zipCode) {}
record Person(String name, Address contactAddress) {}

static Result<Address> validateAddress(String street, String city, String zipCode) {
    return validateStreet(street)
        .and(validateCity(city))
        .and(validateZipCode(zipCode))
        .combine((s, c, z) -> new Address(s, c, z)); // Combine up to 10 results!
}

static Result<Person> validatePersonWithAddress(String name, String street, String city, String zipCode) {
    return validateName(name)
        .and(validateAddress(street, city, zipCode).withPrefix("contactAddress"))
        .combine((n, a) -> new Person(n, a));
}

// Errors become: "contactAddress.street", "contactAddress.city", "contactAddress.zipCode"
```

#### Collection Validation

Use `ResultCollector.toResultList()` with `withIndex()` for automatic indexing:

```java
import static io.github.raniagus.javalidation.ResultCollector.*;

record Order(List<Item> items) {}
record Item(String name, double price) {}

static Result<Item> validateItem(Item item) {
    return validateItemName(item.name())
        .and(validatePrice(item.price()))
        .combine((name, price) -> item);
}

// Validate all items in a collection
static Result<List<Item>> validateItems(List<Item> items) {
    return Result.ok(items)
            .filter(i -> !i.isEmpty(), "Order must contain at least one item")
            .flatMap(i -> i.stream()
                    .map(YourClass::validateItem)
                    .collect(withIndex(toResultList()))
            );
}
// Errors are automatically indexed: "[0].name", "[1].price", "[2].name"

// For nested collections with a custom prefix:
static Result<Order> validateOrder(Order order) {
    Result<List<Item>> itemsResult = Result.ok(order.items())
            .filter(i -> !i.isEmpty(), "Order must contain at least one item")
            .flatMap(i -> i.stream()
                .map(YourClass::validateItem)
                .collect(withPrefix("items", withIndex(toResultList())))
            );
    
    return itemsResult.map(Order::new);
}
// Errors become: "items[0].name", "items[1].price", "items[2].name"
```

### Internal Checks

Use `JavalidationException` for imperative validation with side effects (database queries, external API calls):

#### Throwing Validation Errors

```java
public User findUserByEmail(String email) {
    User user = database.findByEmail(email);
    if (user == null) {
        throw JavalidationException.ofField("email", "User not found");
    }
    return user;
}

public void checkInventory(String productId, int quantity) {
    int available = inventoryService.getAvailable(productId);
    if (quantity > available) {
        throw JavalidationException.ofField("quantity", 
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

#### Stream Collections with Imperative Validation

Use `toListOrThrow()` to validate collections imperatively:

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
        throw JavalidationException.ofField("email", "Email already registered");
    }

    if (!emailVerificationService.verify(user.email())) {
        throw JavalidationException.ofField("email", "Email could not be verified");
    }

    return database.create(user);
}

// If any validation fails, throws JavalidationException with all errors:
// "users[0].email": ["Email could not be verified"],
// "users[2].email": ["Email already registered"]
```

For more complex scenarios with imperative state validation that require not throwing an exception on the first error,
please check the [Advanced Patterns](#advanced-patterns) section.

## Real-World Example

Combining business rules validation and internal checks in a REST API:

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserValidator validator;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest request) {
        try {
            return switch (validator.validateRegistrationRequest(request)) {
                case Result.Ok(User user) -> ResponseEntity.ok(userService.createUser(user));

                // 422 Unprocessable Content: Request is well-formed but violates business rules
                case Result.Err(ValidationErrors errors) -> ResponseEntity.status(422).body(errors);
            };
        } catch (JavalidationException e) {
            // 409 Conflict: Request is valid but conflicts with the current state
            return ResponseEntity.status(409).body(e.getErrors());
        } catch (Exception e) {
            // 500: Unexpected error - log and alert
            logger.error("Unexpected error during registration", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}

// Business rules validator
@Component
public class UserValidator {
    public Result<User> validateRegistrationRequest(UserRegistrationRequest request) {
        return validateName(request.name())
            .and(validateAge(request.age()))
            .and(validateEmail(request.email()))
            .and(validatePassword(request.password()))
            .combine(User::new);
    }
    
    // validateName(), validateAge(), etc. from Business Rules section
}

// Service with internal checks
@Service
public class UserService {
    public User createUser(User user) {
        // Check if email already exists (database query)
        if (userRepository.existsByEmail(user.getEmail())) {
            throw JavalidationException.ofField("email", "Email already registered");
        }

        // Check external validation service
        if (!emailVerificationService.verify(user.getEmail())) {
            throw JavalidationException.ofField("email", "Email could not be verified");
        }

        return userRepository.save(user);
    }
}
```

**Error handling strategy:**
- **400 Bad Request**: Malformed requests (invalid JSON) – Handled on deserialization layer
- **422 Unprocessable Content**: Business rule violations (malformed email, weak password)
- **409 Conflict**: State conflicts (email already registered, verification failed)
- **500 Internal Server Error**: Unexpected bugs (NullPointerException, SQLException)

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

Result<User> failure = Result.err("email", "Invalid format");
// {"ok": false, "errors": {"fieldErrors": {"email": ["Invalid format"]}}}
```

### Spring Boot Integration

**Configuration (optional):**
```yaml
# application.yml
io.github.raniagus.javalidation:
  key-notation: property_path # Choose how to serialize field keys (property_path, dots or brackets) 
  use-message-source: true    # Use Spring MessageSource for i18n (default: true)
  flatten-errors: false       # Flatten JSON error structure (default: false)
  
```

**Internationalization:**

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
            .filter(n -> !n.isEmpty(), "name", "user.name.required")
            .and(Result.ok(age)
                    .filter(a -> a >= 18, "age", "user.age.minimum", 18)
            )
            .combine(User::new);
}
```

Spring automatically formats messages based on the request locale.

**Bean validation**:

When including `javalidation-jakarta-validation*` dependency and annotation processor, `@Valid` annotations are
automatically validated using `Validations.validate(T)`, which is a compile-time generated service locator for different
`Validator<T>` instances.

To tell the annotation processor to generate a `Validator<T>`, you need to annotate the class with `@Validate`:

```java
import io.github.raniagus.javalidation.validator.Validate;
import jakarta.validation.constraints.*;

@Validate
public record UserDto(
        @NotBlank String name, 
        @NotNull @Email String email, 
        @NotEmpty List<@NotNull OrderDto> orders
) {
    @Validate
    public record OrderDto(
            @NotEmpty String productId,
            @Min(0) int quantity
    ) {}
}
```

> **Important:** Only record classes are supported.

If you'd like to inject the validators manually, you could create each corresponding `@Bean` calling
`Validators.getValidator(Class)`:

```java
import io.github.raniagus.javalidation.validator.Validators;

@Bean
public Validator<UserDto> userValidator() {
    return Validators.getValidator(UserDto.class);
}

@Bean
public Validator<OrderDto> orderValidator() {
    return Validators.getValidator(OrderDto.class);
}
```

## Advanced Patterns

### Partial Success

Process valid items even when some fail validation:

```java
import static io.github.raniagus.javalidation.ResultCollector.*;

public ProcessOrderResult processOrder(Order order) {
    var partitioned = items.stream()
            .map(this::validateItem)
            .collect(withIndex(toPartitioned()));

    // Continue with valid items
    List<Item> validItems = partitioned.value();
    processValidItems(validItems);

    // Return partial success with errors for invalid items
    return new ProcessOrderResult(partitioned.errors());
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
        validation.addRootError("Order is required");
    }

    // Field validation by calling utility method
    validation.validateField("items", () -> {
        validateItemsList(validation, order.items());
    });

    // Additional cross-field validation
    double total = order.items().stream().mapToDouble(Item::price).sum();
    if (total > 10000 && order.paymentMethod().equals(PaymentMethod.CASH)) {
        validation.addFieldError("paymentMethod", 
            "Cash payments limited to {0} for orders over {1}", 1000, 10000);
    }

    // Throw if any errors accumulated
    validation.check();
}

public void validateItemsList(Validation validation, List<Item> items) {
    // Basic field checks
    if (items.isEmpty()) {
        validation.addRootError("Order must contain at least one item"); // "items": ["Order must contain at least one item"]
    }

    // Validate items and accumulate errors into the same Validation instance
    items.stream()
        .map(this::validateItem)
        .collect(withIndex(into(validation))); // "items[0]": ["Item is required"], ...
} 
```

The `into()` collector accumulates errors into an existing `Validation` instance, allowing you to combine stream-based validation with imperative validation logic.

## API Reference

### Result<T>

| Method                                 | Description                                        |
|----------------------------------------|----------------------------------------------------|
| `of(Supplier<T>)`/ `of(Runnable)`      | Wrap supplier or runnable in try-catch             |
| `ok(T)`                                | Create successful result                           |
| `err(String, Object...)`               | Create failed result with root error               |
| `err(String, String, Object...)`       | Create failed result with field error              |
| `err(ValidationErrors)`                | Create failed result from existing errors          |
| `map(Function)`                        | Transform success value                            |
| `flatMap(Function)`                    | Chain validations                                  |
| `filter(Predicate, String, String)`    | Conditional validation (field error)               |
| `check(BiConsumer)`                    | Add imperative validation logic                    |
| `and(Result)`                          | Start applicative combiner chain                   |
| `or(Result)` / `or(Supplier)`          | Provide fallback                                   |
| `fold(Function, Function)`             | Handle both cases                                  |
| `getOrThrow()`                         | Extract value or throw                             |
| `getOrElse(T)` / `getOrElse(Supplier)` | Extract value or default                           |
| `withPrefix(String)`                   | Namespace errors for nested objects                |

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

| Method                                     | Description                           |
|--------------------------------------------|---------------------------------------|
| `create()`                                 | Create new empty validation           |
| `addRootError(String, Object...)`          | Add root-level error                  |
| `addFieldError(String, String, Object...)` | Add field-specific error              |
| `addAll(ValidationErrors)`                 | Merge errors                          |
| `addAll(String, ValidationErrors)`         | Merge errors with prefix              |
| `check()`                                  | Throw if errors exist                 |
| `asResult(Supplier)`                       | Convert to Result                     |

### ResultCollector

| Method                                   | Description                                                      |
|------------------------------------------|------------------------------------------------------------------|
| `toResultList()` / `toResultList(int)`   | Returns `Result<List<T>>` (functional style)                     |
| `toListOrThrow()` / `toListOrThrow(int)` | Returns `List<T>` or throws (imperative style)                   |
| `toPartitioned()` / `toPartitioned(int)` | Returns valid items + errors (partial success)                   |
| `into(Validation)`                       | Accumulates errors into existing `Validation` (mutable state)    |
| `withIndex(Collector<...>)`              | Wraps collector to add `[0]`, `[1]`, etc. prefixes               |
| `withPrefix(String, Collector<...>)`     | Wraps collector to add field prefix to all errors                |

> **Note:** The optional `int` parameter provides an `initialCapacity` hint for ArrayList optimization.

### JavalidationException

| Method                                      | Description                       |
|---------------------------------------------|-----------------------------------|
| `ofRoot(String, Object...)`                 | Create with root error            |
| `ofField(String, String, Object...)`        | Create with field error           |
| `of(ValidationErrors)`                      | Create from ValidationErrors      |
| `getErrors()`                               | Get accumulated errors            |

## License

This project is licensed under the MIT License.

[^1]: [How to consume snapshot releases](https://central.sonatype.org/publish/publish-portal-snapshots/#consuming-snapshot-releases-for-your-project)
