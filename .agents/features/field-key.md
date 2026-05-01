# Feature: FieldKey — Internal Path Representation

`FieldKey` is the internal representation of a field path used as a map key in `ValidationErrors`.
It is a **record wrapping a `FieldKeyPart[]` array** — understanding this is essential for
reasoning about equality, prefix operations, and how paths are constructed across all APIs.

**Sources:**
- `javalidation/src/main/java/io/github/raniagus/javalidation/FieldKey.java`
- `javalidation/src/main/java/io/github/raniagus/javalidation/FieldKeyPart.java`
- `javalidation/src/main/java/io/github/raniagus/javalidation/ValidationErrors.java` (holds `Map<FieldKey, …>`)
- `javalidation/src/main/java/io/github/raniagus/javalidation/Validation.java` (prefix stack)
- `javalidation/src/main/java/io/github/raniagus/javalidation/format/` (rendering)

---

## Internal Structure

```
FieldKey
  └─ FieldKeyPart[]  (ordered array of segments)
        ├─ FieldKeyPart.StringKey(String key)   — a named field: "address", "items"
        └─ FieldKeyPart.IntKey(int key)          — a numeric index: 0, 1, 2
```

`FieldKey` is a `record`, but **`equals` and `hashCode` are manually overridden** to use
`Arrays.equals` / `Arrays.hashCode` on the parts array. Java's record default would use
object identity for arrays, which would break map lookups.

```java
// Correct — two independently constructed keys with same segments are equal:
FieldKey.of("items", 0, "price").equals(FieldKey.of("items", 0, "price")); // true
```

---

## Construction

```java
// String segments only
FieldKey.of("address", "street")           // [StringKey("address"), StringKey("street")]

// Numeric segments only
FieldKey.of(0, 1)                          // [IntKey(0), IntKey(1)]

// Mixed: Number → IntKey, everything else → StringKey via toString()
FieldKey.of("items", 0, "price")           // [StringKey("items"), IntKey(0), StringKey("price")]

// Explicit parts
FieldKey.of(new FieldKeyPart.StringKey("x"), new FieldKeyPart.IntKey(0))

// From prefix collection + extra parts (used internally)
FieldKey.of(deque, extraParts)
```

---

## Ordering (used in `TreeMap`-like contexts)

`FieldKey` implements `Comparable<FieldKey>`. Comparison is **lexicographic segment by segment**:
- At the same position, `StringKey < IntKey` always.
- Among two `StringKey` values: alphabetical string comparison.
- Among two `IntKey` values: numeric comparison.
- Shorter key is less than a longer key with the same prefix.

This ordering is relevant for the `Comparable` implementation but the `Map<FieldKey, …>` in
`ValidationErrors` is a `HashMap`, so ordering is not guaranteed for iteration.

---

## Prefixing — The Core Operation

`FieldKey.withPrefix(FieldKeyPart... prefix)` **creates a new array** by copying the prefix
segments first, then the existing parts. This is an O(n+m) array allocation — it never mutates.

```
FieldKey.of("street").withPrefix(StringKey("address"))
  → new FieldKeyPart[]{ StringKey("address"), StringKey("street") }
  → FieldKey representing "address.street"
```

---

## How FieldKey is Used Across APIs

### Functional Style — `Result.withPrefix` and `ValidationErrors.withPrefix`

Both delegate to `ValidationErrors.withPrefix(FieldKeyPart... prefix)`, which:
1. Converts each root error: root error list → becomes field errors at exactly the prefix key
2. Converts each field error key: calls `key.withPrefix(prefix)` → prepends segments to existing array

```java
// Before:
// root=["invalid"], fieldErrors={"street": ["req"], "zip": ["invalid"]}

ValidationErrors prefixed = errors.withPrefix("address");

// After:
// root=[], fieldErrors={
//   "address": ["invalid"],          ← root became field at prefix key
//   "address.street": ["req"],       ← original key prepended
//   "address.zip": ["invalid"]
// }
```

If the result is `Ok`, `Result.withPrefix` is a no-op (the Ok instance is returned unchanged).

### Imperative Style — `Validation` Prefix Stack

`Validation` maintains an internal `Deque<FieldKeyPart>` that acts as a **live prefix stack**.
Methods that push/pop from this stack:

| Method | Stack operation |
|--------|----------------|
| `withField(String, Runnable)` | push `StringKey(field)`, run, pop |
| `withField(Number, Runnable)` | push `IntKey(field)`, run, pop |
| `withEach(items, consumer)` | for each item: push `IntKey(index)`, run, pop |

When an error is added, the current deque contents are combined with the immediate field to build the final `FieldKey`:

```java
// prefix stack: [StringKey("person")]
validation.addErrorAt("name", "not.null");
// → FieldKey.of(prefix, StringKey("name"))
// → FieldKey([StringKey("person"), StringKey("name")])
// → renders as "person.name"
```

```java
// prefix stack: [StringKey("items"), IntKey(0)]
validation.addError("not.null");
// addError sees non-empty prefix → stores under FieldKey([StringKey("items"), IntKey(0)])
// → renders as "items[0]"
```

Nesting scopes accumulate segments:
```java
validation.withField("order", () ->               // push StringKey("order")
    validation.withField("address", () ->         // push StringKey("address")
        validation.addErrorAt("street", "req")    // → FieldKey(["order","address","street"])
    )                                             // pop StringKey("address")
);                                                // pop StringKey("order")
```

### Stream Collectors — `withIndex` and `withPrefix`

`ResultCollectorWrapper` passes an `outerPrefix: FieldKeyPart[]` to the inner collector when
adding each result:

- **`withIndex(collector)`**: for element `i`, calls `inner.add(result, new FieldKeyPart[]{ IntKey(i) })`
- **`withPrefix(String, collector)`**: always calls `inner.add(result, new FieldKeyPart[]{ StringKey(prefix) })`
- **`withPrefix(int, collector)`**: always calls `inner.add(result, new FieldKeyPart[]{ IntKey(prefix) })`

The inner collector calls `errors.withPrefix(outerPrefix)` before storing, so keys become
`[outerPrefix…][originalKey…]`.

Wrappers compose by prepending their own segment before passing down:
```java
withPrefix("order", withPrefix("items", withIndex(toResultList())))
// outer-most wraps last → errors have "order" prepended last
// effective prefix order on final keys: [StringKey("order"), StringKey("items"), IntKey(i), ...original...]
```

### `ValidationErrors` Map Key

`fieldErrors` is a `Map<FieldKey, List<TemplateString>>`. Because `FieldKey.equals` uses
`Arrays.equals`, two paths with the same segment sequence **hash and compare as equal**,
so merging from different call sites with the same field name correctly appends to the same list.

```java
// These two produce the same FieldKey and map to the same bucket:
validation.addErrorAt("email", "not.null");
validation.addErrorAt("email", "invalid.format");
// → fieldErrors = { FieldKey([StringKey("email")]): ["not.null", "invalid.format"] }
```

### AssertJ Assertions

Assertions convert string paths to `FieldKey` for map lookup:

| Assertion method | FieldKey construction |
|------------------|-----------------------|
| `hasFieldError("email", …)` | `FieldKey.of("email")` → single `StringKey` |
| `hasFieldError(0, …)` | `FieldKey.of(0)` → single `IntKey` |
| `hasFieldErrorAt("items[0].price", …)` | `PropertyPathNotationParser.parse("items[0].price")` → `[StringKey("items"), IntKey(0), StringKey("price")]` |
| `hasFieldErrorAt(FieldKey key, …)` | uses the key directly |
| `hasFieldKey(Object... path)` | `FieldKey.of(path)` — mixed |

---

## Rendering (Formatters)

`FieldKeyFormatter` converts a `FieldKey` to a string for JSON keys, error messages, etc.

| Formatter | `StringKey` | `IntKey` | Example path `items[0].price` |
|-----------|-------------|---------|-------------------------------|
| `PropertyPathNotationFormatter` (default) | `.name` prefix | `[n]` inline | `items[0].price` |
| `DotNotationFormatter` | `.name` prefix | `.n` prefix | `items.0.price` |
| `BracketNotationFormatter` | `[name]` bracket | `[n]` bracket | `[items][0][price]` |

The formatters are pure rendering functions — they do not affect how `FieldKey` is stored or compared.

---

## Non-Obvious Consequences

**Root errors always live in the root list, never in the field map.** A `FieldKey` is only ever
a map key. Root errors have no key. `withPrefix` is the only operation that moves root errors
into the field map (by creating a `FieldKey` from the prefix and placing them there).

**`addError` inside `withField` becomes a field error, not a root error.** When the prefix stack
is non-empty, `addError` adds to `fieldErrors` under the current stack key, not to `rootErrors`.

**Segment type matters for rendering, not for identity.** `FieldKey.of("0")` (StringKey) and
`FieldKey.of(0)` (IntKey) are **different keys** even though they look similar. The string `"0"`
as a field name and the integer index `0` are distinct.
