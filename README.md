# javalidation

[![MvnRepository](https://badges.mvnrepository.com/badge/io.github.raniagus/javalidation/badge.svg?label=MvnRepository)](https://mvnrepository.com/artifact/io.github.raniagus/javalidation)

A functional Java 21+ validation library implementing the Railway-Oriented Programming pattern with a type-safe
`Result<T>` type for accumulating validation errors.

## Features

- **Type-safe error handling**: `Result<T>` sealed type (`Ok`/`Err`) ensures exhaustive handling at compile-time
- **Error accumulation**: Collect multiple validation errors instead of failing fast
- **Functional composition**: Rich API with `map`, `flatMap`, `filter`, `fold`, and applicative combiners
- **Nested validation**: Prefix support for validating hierarchical data structures
- **Zero runtime dependencies**: Core library has no dependencies
- **Optional integrations**: Jackson 3.x serialization and Spring Boot 4.x auto-configuration
- **Internationalization ready**: Template-based error messages with deferred formatting
- **Modern Java**: Full use of Java 21 features (sealed types, pattern matching, records)
- **Null safety**: Fully annotated with JSpecify nullness annotations

## Installation

Javalidation is hosted in the Maven Central Repository. Simply add the following
dependency into your `pom.xml` file:

```xml
    <dependency>
      <groupId>io.github.raniagus</groupId>
      <artifactId>javalidation</artifactId>
      <version>0.9.0</version>
    </dependency>
```

### Snapshots

Also, snapshots of the master branch are deployed automatically on each successful
commit. Instead of Maven Central, you have to consume the Sonatype snapshots
repository and add `-SNAPSHOT` suffix to the version identifier.

#### Consuming via Maven

Configure your `pom.xml` file with the following `<repositories>` section:

```xml
<repositories>
  <repository>
    <name>Central Portal Snapshots</name>
    <id>central-portal-snapshots</id>
    <url>https://central.sonatype.com/repository/maven-snapshots/</url>
    <releases>
      <enabled>false</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
</repositories>
```

#### Consuming via Gradle

Configure your `build.gradle` file with the following:

```gradle
repositories {
  maven {
    name = 'Central Portal Snapshots'
    url = 'https://central.sonatype.com/repository/maven-snapshots/'

    // Only search this repository for the specific dependency
    content {
      includeModule("io.github.raniagus", "javalidation")
    }
  }
  mavenCentral()
}
```

### Basic Example

```java
import io.github.raniagus.javalidation.*;

record Person(String name, int age, String email) {}

// Validate multiple fields and accumulate ALL errors
Result<Person> result = validateName(person.name())
    .and(validateAge(person.age()))
    .and(validateEmail(person.email()))
    .combine((n, a, e) -> person);

// Handle result
String message = switch (result) {
    case Result.Ok(Person p) -> "Valid person: " + p.name();
    case Result.Err(ValidationErrors errors) -> "Validation failed: " + errors;
};

// Or throw if invalid
Person validPerson = result.getOrThrow();

// Individual field validators
static Result<String> validateName(String name) {
    return Result.ok(name)
        .filter(n -> n != null && !n.isEmpty(), "name", "Name is required");
}

static Result<Integer> validateAge(int age) {
    return Result.ok(age)
        .filter(a -> a >= 18, "age", "Must be 18 or older");
}

static Result<String> validateEmail(String email) {
    return Result.ok(email)
        .filter(e -> e.contains("@"), "email", "Invalid email format");
}
```

## Core Concepts

### Result<T> - The Railway

`Result<T>` is a sealed interface with two variants:
- `Ok<T>(T value)` - successful validation
- `Err<T>(ValidationErrors errors)` - failed validation with accumulated errors

```java
// Create results
Result<String> success = Result.ok("value");
Result<String> failure = Result.err("Invalid input");
Result<String> fieldError = Result.err("email", "Invalid format");

// Extract values
String value = success.getOrThrow();              // "value"
String fallback = failure.getOrElse("default");   // "default"

// Pattern matching
boolean isValid = switch (result) {
    case Result.Ok<?> _ -> true;
    case Result.Err<?> _ -> false;
};
```

### Functional Operations

```java
// Map - transform success values
Result<Integer> age = Result.ok("25")
    .map(Integer::parseInt);  // Ok(25)

// FlatMap - chain dependent validations (stops at first error)
Result<User> user = validateEmail(email)
    .flatMap(e -> findUserByEmail(e))    // only runs if email valid
    .flatMap(u -> validateUserActive(u));  // only runs if user found

// Filter - single field validation (use .and() to accumulate multiple errors)
Result<Integer> adultAge = Result.ok(20)
    .filter(age -> age >= 18, "age", "Must be 18 or older");  // Ok(20)

Result<Integer> childAge = Result.ok(15)
    .filter(age -> age >= 18, "age", "Must be 18 or older");  // Err(...)

// Check - add multiple validations to same field
Result<Integer> validated = Result.ok(age)
    .check((a, v) -> {
        if (a < 0) v.addFieldError("age", "Cannot be negative");
        if (a < 18) v.addFieldError("age", "Must be at least 18");
        if (a > 120) v.addFieldError("age", "Invalid age");
    });

// Fold - handle both cases
String msg = result.fold(
    success -> "Valid: " + success,
    errors -> "Invalid: " + errors
);
```

### Imperative Validation with Validation Builder

For complex validation logic, use the `Validation` builder:

```java
Validation validation = Validation.create();

if (user.name() == null || user.name().isEmpty()) {
    validation.addFieldError("name", "Name is required");
}

if (user.name().length() < 2) {
    validation.addFieldError("name", "Name must be at least 2 characters");
}

if (user.age() < 0) {
    validation.addFieldError("age", "Age cannot be negative");
} else if (user.age() < 18) {
    validation.addFieldError("age", "Must be at least 18 years old");
}

if (!user.email().matches("^[^@]+@[^@]+\\.[^@]+$")) {
    validation.addFieldError("email", "Invalid email format");
}

// Convert to Result
Result<User> result = validation.asResult(user);

// Or throw if errors exist
validation.check();  // throws JavalidationException if invalid
```

### Combining Multiple Results

The applicative combiner pattern lets you validate multiple fields independently and accumulate all errors:

```java
// All three validations run, errors are accumulated
Result<Person> person = validateName(name)
    .and(validateAge(age))
    .and(validateEmail(email))
    .combine((validName, validAge, validEmail) -> 
        new Person(validName, validAge, validEmail));

// If multiple validations fail, all errors are collected:
// Err(ValidationErrors(
//   rootErrors=[],
//   fieldErrors={
//     "name": ["Name is required"],
//     "age": ["Must be at least 18"],
//     "email": ["Invalid email format"]
//   }
// ))
```

Supports up to 10 results (ResultCombiner2 through ResultCombiner10).

### Validating Nested Objects

Use `withPrefix()` to namespace errors for nested structures:

```java
record Address(String street, String city, String zipCode) {}
record User(String name, Address address) {}

Result<Address> addressResult = validateAddress(user.getAddress())
    .withPrefix("address");

// Errors become: "address.street", "address.city", etc.

// Or using Validation builder:
Address address = user.getAddress();

Validation validation = Validation.create();
ValidationErrors addressErrors = validateAddress(address);
validation.addAll("address", addressErrors);

Result<Address> addressResult = validation.asResult(address);
```

### Validating Collections

#### Imperative Approach

Use `Validation` for explicit iteration:

```java
List<Item> items = ...;
Validation validation = Validation.create();

for (int i = 0; i < items.size(); i++) {
    ValidationErrors itemErrors = validateItem(items.get(i));
    validation.addAll("items[" + i + "]", itemErrors);
    // Errors: "items[0].name", "items[1].price", etc.
}

Result<List<Item>> result = validation.asResult(items);
```

#### Stream-Based Approach with ResultCollector

The `ResultCollector` class provides three specialized collectors for validating streams. **All three collectors
accumulate ALL validation errors** before returning/throwing - they do not fail fast.

**1. toResultList() - Functional Style (Recommended)**

Returns `Result<List<T>>` with all validation errors accumulated:

```java
import io.github.raniagus.javalidation.ResultCollector;

// Validate all items, collect ALL errors
Result<List<User>> result = items.stream()
        .map(this::validateUser)
    .collect(ResultCollector.toResultList());

// Handle result
switch(result){
        case Result.

        Ok(List<User> users) ->

        processUsers(users);
    case Result.

        Err(ValidationErrors errors) ->{

        // errors contain all validation failures with indexes:
        // "[0].email": ["Invalid format"]
        // "[2].age": ["Must be 18 or older"]
        logErrors(errors);
    }
            }
```

**2. toList() - Imperative Style**

Returns `List<T>` directly, throwing `JavalidationException` with all accumulated errors if any validation fails:

```java
try{
List<User> users = items.stream()
        .map(this::validateUser)
        .collect(ResultCollector.toList());

// All items valid
processUsers(users);
}catch(
JavalidationException e){

// Contains ALL indexed errors: "[0].email", "[2].age", etc.
logErrors(e.getErrors());
        }
```

**3. toPartitioned() - Process Valid Items Regardless**

Returns `PartitionedResult<List<T>>` with both valid items and all errors:

```java
var partitioned = items.stream()
        .map(this::validateUser)
        .collect(ResultCollector.toPartitioned());

// Process valid items even if some failed
List<User> validUsers = partitioned.value();
ValidationErrors errors = partitioned.errors();

if(errors.

isNotEmpty()){
        logger.

warn("Processed {} valid users, {} failed",
     validUsers.size(),errors.

count());

logErrors(errors);
}

// Continue with valid users
processUsers(validUsers);
```

**Error Indexing:**

All three collectors automatically index errors by their position in the stream:

```java
// Input stream with 3 items (indices 0, 1, 2)
// Items at index 0 and 2 have validation errors

Result<List<Item>> result = stream.collect(ResultCollector.toResultList());

// Errors are prefixed with "[index]":
// "[0].field1": ["Error message"]
// "[0].field2": ["Another error"]  
// "[2].price": ["Must be positive"]
```

**Choosing the Right Collector:**

| Collector         | Use When                                              | Returns                      |
|-------------------|-------------------------------------------------------|------------------------------|
| `toResultList()`  | You want functional error handling with `Result` type | `Result<List<T>>`            |
| `toList()`        | You want imperative error handling with exceptions    | `List<T>`                    |
| `toPartitioned()` | You want to process valid items even if some fail     | `PartitionedResult<List<T>>` |

**Note:** All collectors process the entire stream and accumulate ALL validation errors before returning or throwing.
None of them fail fast on the first error.

## Error Handling: The Error Channel

javalidation implements a sophisticated dual error handling approach, inspired by Effect.TS, that distinguishes
between **expected validation failures** and **unexpected programming errors**. This design decision is fundamental
to the library's architecture.

### Design Philosophy

Traditional validation approaches force a choice:

1. **Fail-fast with exceptions**: `if (!valid) throw new ValidationException();`
    - Problem: Loses type safety, requires try-catch everywhere, only reports first error

2. **Return Either/Validation types**: `Either<Error, Value>` or `Validation<List<Error>, Value>`
    - Problem: ALL errors become values, including bugs like NullPointerException
    - Debugging becomes harder when programming errors don't fail fast

**The Error Channel Pattern solves this dilemma** by maintaining two separate error handling paths:

```
┌─────────────────────────────────────────────────────────────┐
│                    Error Channel Pattern                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  JavalidationException ──→ Caught by .map()/.flatMap()      │
│      (Expected)            Converted to Err<T>              │
│                            Accumulated with other errors    │
│                            Safe to return to API clients    │
│                                                             │
│  Other Exceptions  ──────→ Propagate normally               │
│      (Unexpected)          Fail fast                        │
│                            Caught by global handlers        │
│                            Logged and monitored             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

This enables:

- **Type-safe error accumulation** for business validation (via JavalidationException)
- **Fail-fast debugging** for programming errors (via normal exception propagation)
- **Clean separation** between "this is invalid user input" and "this is a bug"

### Error Channel (Well-Known Errors)

`JavalidationException` represents expected validation failures from the library's API. These are automatically caught
by transformation methods (`.map()`, `.flatMap()`) and converted to the error track:

```java
Result<User> result = Result.ok(userId)
    .map(id -> userRepository.findByIdOrThrow(id))  // throws JavalidationException
    .flatMap(u -> validateUserStatus(u));            // continues on error track

// If findByIdOrThrow throws JavalidationException, it automatically becomes Err
```

**Key characteristics:**

- **Automatic catching**: Transformation methods catch and convert to `Err`
- **Safe to return**: These errors provide structured validation feedback for API consumers
- **Functional composition**: Errors flow through the Result pipeline without breaking the chain
- **Expected failures**: Represent application-level validation logic, not bugs

### Exception Propagation (Unexpected Errors)

All other exceptions (`NullPointerException`, `IllegalStateException`, `IOException`, etc.) propagate normally through
the call stack:

```java
Result<User> result = Result.ok(userId)
    .map(id -> {
        if (id == null) throw new NullPointerException();  // PROPAGATES!
        return database.findUser(id);
    });
// NullPointerException is thrown, not caught
```

**Key characteristics:**

- **Normal propagation**: These exceptions bubble up to be caught by standard try-catch blocks
- **Indicate bugs**: Represent programming errors or infrastructure failures
- **Should be monitored**: Log these errors and alert on them
- **Generic responses**: Result in 500-level HTTP responses to clients

### Real-World Example

```java
// Service layer with both error types
public Result<Order> processOrder(OrderRequest request) {
    return validateOrderRequest(request)                    // JavalidationException → Err
            .map(req -> checkInventory(req))                    // JavalidationException → Err
            .flatMap(req -> reserveInventory(req))              // JavalidationException → Err
            .map(inv -> calculateShipping(inv))                 // IOException → PROPAGATES
            .flatMap(order -> chargePayment(order));            // JavalidationException → Err
}

// Controller handles both paths
@PostMapping("/orders")
public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
    try {
        Result<Order> result = orderService.processOrder(request);

        return switch (result) {
            case Result.Ok(Order order) -> ResponseEntity.ok(order);
            case Result.Err(ValidationErrors errors) ->
                // 409 Conflict: Request is valid but conflicts with current state
                // (e.g., insufficient inventory, payment declined, business rule violation)
                    ResponseEntity.status(409).body(Map.of(
                            "errors", errors,
                            "message", "Your order could not be processed"
                    ));
        };
    } catch (IOException e) {
        // 500-level: Infrastructure failure - log and alert
        logger.error("Payment service unreachable", e);
        return ResponseEntity.status(500).body("Service temporarily unavailable");
    } catch (Exception e) {
        // 500-level: Unknown bug - log and alert
        logger.error("Unexpected error processing order", e);
        return ResponseEntity.status(500).body("Internal server error");
    }
}
```

### Best Practices

**For validation failures in your domain logic:**

```java
// In repository layer
public User findByIdOrThrow(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new JavalidationException("User not found"));
}

// In business logic
public Result<Order> validateOrder(Order order) {
    if (order.items().isEmpty()) {
        return Result.err("Order must contain at least one item");
    }
    return Result.ok(order);
}
```

**At application boundaries (controllers):**

```java
@PostMapping("/users")
public ResponseEntity<?> createUser(@RequestBody UserRequest request) {
    try {
        Result<User> result = validateAndCreateUser(request);
        
        return switch (result) {
            case Result.Ok(User u) -> ResponseEntity.ok(u);
            case Result.Err(ValidationErrors e) ->
                // 409 Conflict: Valid request but business rules prevent processing
                // Use 400 Bad Request for malformed input (handled by framework)
                // Use 422 Unprocessable Content for semantic validation errors
                    ResponseEntity.status(409).body(e);
        };
    } catch (Exception e) {
        logger.error("Unexpected error creating user", e);
        return ResponseEntity.status(500).body("Internal server error");
    }
}
```

**When to use JavalidationException:**

- User input validation failures
- Business rule violations  
- "Not found" errors for expected resources
- Any failure you want accumulated with other validation errors

**When NOT to use JavalidationException:**

- Null pointer access (use JSpecify nullness annotations instead to prevent at compile-time)
- Database connection failures (infrastructure errors)
- Programming logic errors (assertions, IllegalStateException)
- External service failures (IO exceptions, timeout errors)

### Comparison with Other Approaches

| Approach                         | Validation Errors | Programming Errors | Error Accumulation | Type Safety |
|----------------------------------|-------------------|--------------------|--------------------|-------------|
| **Exceptions**                   | ❌ Not accumulated | ✅ Fail fast        | ❌ No               | ❌ Unchecked |
| **Either\<E,A\>**                | ✅ Type safe       | ❌ Become values    | ⚠️ Manual          | ✅ Yes       |
| **Validation\<E,A\>**            | ✅ Accumulated     | ❌ Become values    | ✅ Automatic        | ✅ Yes       |
| **Error Channel (This library)** | ✅ Accumulated     | ✅ Fail fast        | ✅ Automatic        | ✅ Yes       |

This design philosophy keeps the validation error channel clean and predictable while preserving fail-fast behavior for
genuine bugs and infrastructure problems. It's the best of both worlds: **functional error accumulation for business
validation, imperative exception handling for programming errors**.

## Spring Boot Integration

javalidation auto-configures Jackson serialization and i18n support in Spring Boot applications.

### Setup

The library auto-detects Spring Boot and Jackson on the classpath. Configuration is automatic!

```yaml
# application.yml (optional configuration)
io.github.raniagus.javalidation:
    use-message-source: true   # Use Spring MessageSource for i18n (default: true)
    flatten-errors: false      # Flatten JSON error structure (default: false)
```

### Controller Integration

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserRequest request) {
        Result<User> result = validateAndCreateUser(request);
        
        return switch (result) {
            case Result.Ok(User user) -> ResponseEntity.ok(user);
            case Result.Err(ValidationErrors errors) -> 
                ResponseEntity.badRequest().body(errors);
        };
    }
    
    private Result<User> validateAndCreateUser(UserRequest request) {
        return validateName(request.name())
            .and(validateAge(request.age()))
            .and(validateEmail(request.email()))
            .combine((name, age, email) -> new User(name, age, email));
    }
}
```

### JSON Serialization

#### Default Structure

```json
{
  "rootErrors": [
    {"message": "User validation failed", "args": []}
  ],
  "fieldErrors": {
    "name": [
      {"message": "Name is required", "args": []}
    ],
    "age": [
      {"message": "Must be at least {0}", "args": [18]}
    ]
  }
}
```

#### Flattened Structure

Set `io.github.raniagus.javalidation.flatten-errors: true`:

```json
{
  "": ["User validation failed"],
  "name": ["Name is required"],
  "age": ["Must be at least 18"]
}
```

### Internationalization

Create message files:

```properties
# messages_en.properties
user.name.required=Name is required
user.age.minimum=Must be at least {0} years old

# messages_es.properties
user.name.required=El nombre es obligatorio
user.age.minimum=Debe tener al menos {0} años
```

Use message keys in validation:

```java
validation.addFieldError("name", "user.name.required");
validation.addFieldError("age", "user.age.minimum", 18);
```

Spring automatically formats messages based on the request locale.

## Jackson Integration (Standalone)

Without Spring Boot, configure Jackson manually:

```java
// Default: structured errors
JavalidationModule module = JavalidationModule.getDefault();

// Flattened errors
JavalidationModule module = JavalidationModule.builder()
        .withFlattenedErrors()
        .build();

// Custom formatter
JavalidationModule module = JavalidationModule.builder()
        .withTemplateStringFormatter(customFormatter)
        .build();

// Register module
JsonMapper mapper = JsonMapper.builder()
        .addModule(module)
        .build();
```

## Advanced Patterns

### Custom Validators

```java
public class UserValidator {
    
    // Single-field validator: .filter() chains are OK here (fail-fast per field)
    // Use .and() to combine validators for DIFFERENT fields (accumulates errors)
    public static Result<String> validateName(String name) {
        return Result.ok(name)
            .filter(n -> n != null && !n.isEmpty(), "name", "Name is required")
            .filter(n -> n.length() >= 2, "name", "Name must be at least 2 characters")
            .filter(n -> n.length() <= 50, "name", "Name must not exceed 50 characters");
    }
    
    // Alternative: Use .check() to accumulate multiple errors for same field
    public static Result<Integer> validateAge(int age) {
        return Result.ok(age)
            .check((a, v) -> {
                if (a < 0) v.addFieldError("age", "Age cannot be negative");
                if (a < 18) v.addFieldError("age", "Must be at least 18 years old");
                if (a > 120) v.addFieldError("age", "Invalid age");
            });
    }
    
    public static Result<String> validateEmail(String email) {
        return Result.ok(email)
            .filter(e -> e != null && !e.isEmpty(), "email", "Email is required")
            .filter(e -> e.matches("^[^@]+@[^@]+\\.[^@]+$"), "email", "Invalid email format");
    }
    
    // Combine validators for multiple fields (accumulates ALL errors)
    public static Result<User> validateUser(String name, int age, String email) {
        return validateName(name)
            .and(validateAge(age))
            .and(validateEmail(email))
            .combine((n, a, e) -> new User(n, a, e));
    }
}
```

### Conditional Validation

```java
Result<Order> result = Result.ok(order)
    .check((o, v) -> {
        if (o.total() < 0) {
            v.addFieldError("total", "Total cannot be negative");
        }
        
        if (o.items().isEmpty()) {
            v.addRootError("Order must contain at least one item");
        }
        
        if (o.shippingAddress() == null && o.requiresShipping()) {
            v.addFieldError("shippingAddress", "Shipping address is required");
        }
    });
```

### Reusable Validation Logic
Create reusable validator components as functions that return `Result<T>`:

```java
// Simple reusable validators
public class Validators {
    
    public static Result<String> notBlank(String value, String field) {
        return Result.ok(value)
            .filter(s -> s != null && !s.isBlank(), field, "Must not be blank");
    }
    
    public static Result<String> maxLength(String value, String field, int max) {
        return Result.ok(value)
            .filter(s -> s.length() <= max, field, "Must not exceed " + max + " characters");
    }
    
    public static Result<Integer> positive(int value, String field) {
        return Result.ok(value)
            .filter(i -> i > 0, field, "Must be positive");
    }
    
    public static Result<Integer> range(int value, String field, int min, int max) {
        return Result.ok(value)
            .check((v, validation) -> {
                if (v < min) validation.addFieldError(field, "Must be at least " + min);
                if (v > max) validation.addFieldError(field, "Must be at most " + max);
            });
    }
}

// Compose validators for complex logic
public class UserValidators {
    
    // Single-field validation (fail-fast within the field)
    public static Result<String> validateEmail(String email) {
        return Result.ok(email)
            .flatMap(e -> Validators.notBlank(e, "email"))
            .flatMap(e -> Validators.maxLength(e, "email", 100))
            .filter(e -> e.contains("@"), "email", "Invalid email format");
    }
    
    // Multi-field validation (accumulates errors across fields)
    public static Result<User> validateUser(String name, int age, String email) {
        return Validators.notBlank(name, "name")
            .and(Validators.range(age, "age", 18, 120))
            .and(validateEmail(email))
            .combine((n, a, e) -> new User(n, a, e));
    }
}

// Usage
Result<String> email = UserValidators.validateEmail("user@example.com");
Result<User> user = UserValidators.validateUser("John", 25, "john@example.com");
```

**Key principle:** Use `.flatMap()` for fail-fast validation within a single field, and use `.and()` + `.combine()` to
accumulate errors across multiple fields.

## API Overview

### Result<T>

| Method                                 | Description                           |
|----------------------------------------|---------------------------------------|
| `ok(T)`                                | Create successful result              |
| `err(String)`                          | Create failed result with root error  |
| `err(String, String)`                  | Create failed result with field error |
| `map(Function)`                        | Transform success value               |
| `flatMap(Function)`                    | Chain validations                     |
| `filter(Predicate, String)`            | Conditional validation (root error)   |
| `filter(Predicate, String, String)`    | Conditional validation (field error)  |
| `check(BiConsumer)`                    | Add imperative validation logic       |
| `and(Result)`                          | Start applicative combiner chain      |
| `or(Result)` / `or(Supplier)`          | Provide fallback                      |
| `fold(Function, Function)`             | Handle both cases                     |
| `getOrThrow()`                         | Extract value or throw                |
| `getOrElse(T)` / `getOrElse(Supplier)` | Extract value or default              |
| `withPrefix(String)`                   | Namespace errors for nested objects   |

### Validation

| Method                                     | Description                              |
|--------------------------------------------|------------------------------------------|
| `create()`                                 | Create new empty validation              |
| `addRootError(String, Object...)`          | Add root-level error                     |
| `addFieldError(String, String, Object...)` | Add field-specific error                 |
| `addAll(ValidationErrors)`                 | Merge errors                             |
| `addAll(String, ValidationErrors)`         | Merge errors with prefix                 |
| `finish()`                                 | Convert to immutable ValidationErrors    |
| `asResult(T)`                              | Convert to Result                        |
| `asResult(Supplier)`                       | Convert to Result (lazy)                 |
| `check()`                                  | Throw if errors exist                    |
| `checkAndGet(Supplier)`                    | Throw if errors exist, else return value |

### ValidationErrors

| Method                          | Description                    |
|---------------------------------|--------------------------------|
| `empty()`                       | Create empty errors            |
| `of(String, Object...)`         | Create with single root error  |
| `of(String, String, Object...)` | Create with single field error |
| `mergeWith(ValidationErrors)`   | Merge two error sets           |
| `withPrefix(String)`            | Add prefix to all fields       |
| `isEmpty()` / `isNotEmpty()`    | Check if errors exist          |

## Requirements

- **Java 21+** (uses sealed types, pattern matching, records)
- **Maven 3.9+** for building

## License

This project is licensed under the MIT License.
