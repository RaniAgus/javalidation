package io.github.raniagus.javalidation;

import java.util.List;
import java.util.stream.Collector;
import org.jspecify.annotations.Nullable;

/**
 * Factory methods for creating collectors that accumulate {@link Result} elements from streams.
 * <p>
 * This class provides three types of collectors:
 * <ul>
 *   <li>{@link #toListOrThrow()} - Collects into a {@link List}, throwing on errors</li>
 *   <li>{@link #toResultList()} - Collects into a {@link Result} of {@link List}</li>
 *   <li>{@link #toPartitioned()} - Collects valid items and errors separately</li>
 * </ul>
 * <p>
 * By default, collectors do not automatically index errors. Use the {@link #withIndex(Collector)} wrapper
 * to add automatic index prefixes like {@code [0]}, {@code [1]}, etc. to errors based on item position
 * in the stream.
 *
 * <h2>Basic Usage (No Indexing)</h2>
 * <pre>{@code
 * Result<List<User>> result = users.stream()
 *     .map(this::validateUser)
 *     .collect(toResultList());
 * // Errors: "field": ["Error message"]
 * }</pre>
 *
 * <h2>With Automatic Indexing</h2>
 * <pre>{@code
 * Result<List<User>> result = users.stream()
 *     .map(this::validateUser)
 *     .collect(withIndex(toResultList()));
 * // Errors: "[0].field": ["Error message"], "[2].field": ["Another error"]
 * }</pre>
 *
 * <h2>With Custom Prefix</h2>
 * <pre>{@code
 * Result<List<Item>> items = order.getItems().stream()
 *     .map(this::validateItem)
 *     .collect(withPrefix("items", withIndex(toResultList())));
 * // Errors: "items[0].price": ["Must be positive"]
 * }</pre>
 *
 * @see Result
 * @see ValidationErrors
 * @see PartitionedResult
 */
public interface ResultCollector<T extends @Nullable Object, R, SELF extends ResultCollector<T, R, SELF>> {

    void add(Result<T> result);

    void add(Result<T> result, StringBuilder prefix);

    SELF combine(SELF other);

    R finish();

    /**
     * Wraps a collector to automatically add index prefixes to validation errors.
     * <p>
     * Each item in the stream is assigned an index starting from 0. Errors from that item
     * are prefixed with {@code [index]}, for example: {@code [0].field}, {@code [1].field}, etc.
     * <p>
     * This wrapper is essential for identifying which items in a collection failed validation.
     * <p>
     * <strong>Example without indexing:</strong>
     * <pre>{@code
     * // Without indexing
     * Result<List<Item>> result = stream.collect(toResultList());
     * // Errors: "field": ["Error"]
     *
     * // With indexing
     * Result<List<Item>> result = stream.collect(
     *     withIndex(toResultList())
     * );
     * // Errors: "[0].field": ["Error"], "[2].field": ["Another error"]
     * }</pre>
     *
     * @param collector the collector to wrap
     * @param <T>       the type of the success values
     * @param <R>       the result type of the collector
     * @param <C>       the accumulator type
     * @return a collector that adds automatic index prefixes to errors
     * @see #withIndex(Collector)
     * @see #withPrefix(String, Collector)
     */
    static <T extends @Nullable Object, R, C extends ListResultCollector<T, R, C>> Collector<Result<T>, ResultCollectorWrapper.WithIndex<T, R, C>, R> withIndex(
            Collector<Result<T>, C, R> collector
    ) {
        return Collector.of(
                () -> new ResultCollectorWrapper.WithIndex<>(collector),
                ResultCollectorWrapper.WithIndex::add,
                ResultCollectorWrapper.WithIndex::combine,
                ResultCollectorWrapper.WithIndex::finish
        );
    }

    /**
     * Wraps a collector to automatically add a custom prefix to validation errors.
     * <p>
     * This wrapper adds a field name prefix to all errors from that collector.
     * For example, if errors have a field named "price", they become "prefix.price".
     * <p>
     * This is particularly useful for nested structures where you want to namespace errors
     * within a parent context.
     * <p>
     * <strong>Example with custom prefix:</strong>
     * <pre>{@code
     * Result<List<Item>> items = order.getItems().stream()
     *     .map(this::validateItem)
     *     .collect(withPrefix("order.items", toResultList()));
     * // Errors: "order.items.price": ["Must be positive"]
     *
     * try {
     *     List<Address> addresses = user.getAddresses().stream()
     *         .map(this::validateAddress)
     *         .collect(withPrefix("addresses", toListOrThrow()));
     * } catch (JavalidationException e) {
     *     // Errors: "addresses.street": ["Required"], "addresses.zipCode": ["Invalid format"]
     * }
     * }</pre>
     *
     * @param collector the collector to wrap
     * @param prefix    the prefix to add to all field errors (e.g., "address", "order.items")
     * @param <T>       the type of the success values
     * @param <R>       the result type of the collector
     * @param <C>       the accumulator type
     * @return a collector that adds a prefix to all field errors
     * @see #withIndex(Collector)
     * @see #withPrefix(String, Collector)
     */
    static <T extends @Nullable Object, R, C extends ResultCollector<T, R, C>> Collector<Result<T>, ResultCollectorWrapper.WithPrefix<T, R, C>, R> withPrefix(
            String prefix,
            Collector<Result<T>, C, R> collector
    ) {
        return Collector.of(
                () -> new ResultCollectorWrapper.WithPrefix<>(collector, prefix),
                ResultCollectorWrapper.WithPrefix::add,
                ResultCollectorWrapper.WithPrefix::combine,
                ResultCollectorWrapper.WithPrefix::finish
        );
    }

    /**
     * Returns a {@link Collector} that accumulates {@link Result} elements into a {@link List}.
     * <p>
     * This collector accumulates all validation errors before throwing. If any {@link Result.Err}
     * is encountered, a {@link JavalidationException} is thrown containing all accumulated errors.
     * Otherwise, returns a list of all success values.
     * <p>
     * <b>Note:</b> By default, errors are not indexed. Use {@link #withIndex(Collector)} to add
     * automatic index prefixes like {@code [0]}, {@code [1]}, etc.
     * <p>
     * <strong>Basic usage without indexing:</strong>
     * <pre>{@code
     * try {
     *     List<User> users = items.stream()
     *         .map(this::validateUser)
     *         .collect(toListOrThrow());
     *     processUsers(users);
     * } catch (JavalidationException e) {
     *     // Contains ALL errors (without indexes)
     *     logErrors(e.getErrors());
     * }
     * }</pre>
     * <p>
     * <strong>With automatic indexing:</strong>
     * <pre>{@code
     * try {
     *     List<User> users = items.stream()
     *         .map(this::validateUser)
     *         .collect(withIndex(toListOrThrow()));
     *     processUsers(users);
     * } catch (JavalidationException e) {
     *     // Errors: "[0].email": ["Invalid"], "[2].age": ["Too young"]
     *     logErrors(e.getErrors());
     * }
     * }</pre>
     *
     * @param <T> the type of the success values
     * @return a collector that produces a list or throws on errors
     * @throws JavalidationException if any result is {@link Result.Err}
     * @see #toListOrThrow(int)
     * @see #withIndex(Collector)
     * @see #withPrefix(String, Collector)
     */
    static <T extends @Nullable Object> Collector<Result<T>, ListResultCollector.ToList<T>, List<T>> toListOrThrow() {
        return Collector.of(
                ListResultCollector.ToList::new,
                ListResultCollector.ToList::add,
                ListResultCollector.ToList::combine,
                ListResultCollector.ToList::finish);
    }

    /**
     * Returns a {@link Collector} that accumulates {@link Result} elements into a {@link List},
     * with an initial capacity hint for performance optimization.
     * <p>
     * This collector accumulates all validation errors before throwing. If any {@link Result.Err}
     * is encountered, a {@link JavalidationException} is thrown containing all accumulated errors.
     * Otherwise, returns a list of all success values.
     * <p>
     * The {@code initialCapacity} parameter is a performance hint to avoid ArrayList resizing
     * when the collection size is known upfront. This is particularly useful for large streams.
     * <p>
     * <b>Note:</b> By default, errors are not indexed. Use {@link #withIndex(Collector)} to add
     * automatic index prefixes like {@code [0]}, {@code [1]}, etc.
     * <p>
     * <strong>Example with size hint and automatic indexing:</strong>
     * <pre>{@code
     * List<Item> items = getItems(); // size = 1000
     * try {
     *     List<Item> validated = items.stream()
     *         .map(this::validateItem)
     *         .collect(withIndex(
     *             toListOrThrow(items.size())
     *         ));
     * } catch (JavalidationException e) {
     *     logErrors(e.getErrors());
     * }
     * }</pre>
     *
     * @param initialCapacity the initial capacity for the result list (performance hint)
     * @param <T>             the type of the success values
     * @return a collector that produces a list or throws on errors
     * @throws JavalidationException if any result is {@link Result.Err}
     * @see #toListOrThrow()
     * @see #withIndex(Collector)
     * @see #withPrefix(String, Collector)
     */
    static <T extends @Nullable Object> Collector<Result<T>, ListResultCollector.ToList<T>, List<T>> toListOrThrow(
            int initialCapacity
    ) {
        return Collector.of(
                () -> new ListResultCollector.ToList<>(initialCapacity),
                ListResultCollector.ToList::add,
                ListResultCollector.ToList::combine,
                ListResultCollector.ToList::finish);
    }

    /**
     * Returns a {@link Collector} that accumulates {@link Result} elements into a {@link Result} of {@link List}.
     * <p>
     * This collector accumulates all validation errors and returns them in a {@link Result}.
     * If all results are {@link Result.Ok}, returns {@code Ok(List<T>)}. If any {@link Result.Err}
     * is encountered, returns {@code Err(ValidationErrors)} with all accumulated errors.
     * <p>
     * This is the functional alternative to {@link #toListOrThrow()}, returning a {@link Result} instead
     * of throwing an exception.
     * <p>
     * <b>Note:</b> By default, errors are not indexed. Use {@link #withIndex(Collector)} to add
     * automatic index prefixes like {@code [0]}, {@code [1]}, etc.
     * <p>
     * <strong>Basic usage without indexing:</strong>
     * <pre>{@code
     * Result<List<User>> result = items.stream()
     *     .map(this::validateUser)
     *     .collect(toResultList());
     *
     * switch (result) {
     *     case Result.Ok(List<User> users) -> processUsers(users);
     *     case Result.Err(ValidationErrors errors) -> logErrors(errors);
     * }
     * }</pre>
     * <p>
     * <strong>With automatic indexing:</strong>
     * <pre>{@code
     * Result<List<User>> result = items.stream()
     *     .map(this::validateUser)
     *     .collect(withIndex(toResultList()));
     *
     * // Errors include index prefixes: "[0].email", "[2].age", etc.
     * }</pre>
     *
     * @param <T> the type of the success values
     * @return a collector that produces a result containing a list
     * @see #toResultList(int)
     * @see #withIndex(Collector)
     * @see #withPrefix(String, Collector)
     */
    static <T extends @Nullable Object> Collector<Result<T>, ListResultCollector.ToResultList<T>, Result<List<T>>> toResultList() {
        return Collector.of(
                ListResultCollector.ToResultList::new,
                ListResultCollector.ToResultList::add,
                ListResultCollector.ToResultList::combine,
                ListResultCollector.ToResultList::finish);
    }

    /**
     * Returns a {@link Collector} that accumulates {@link Result} elements into a {@link Result} of {@link List},
     * with an initial capacity hint for performance optimization.
     * <p>
     * This collector accumulates all validation errors and returns them in a {@link Result}.
     * If all results are {@link Result.Ok}, returns {@code Ok(List<T>)}. If any {@link Result.Err}
     * is encountered, returns {@code Err(ValidationErrors)} with all accumulated errors.
     * <p>
     * The {@code initialCapacity} parameter is a performance hint to avoid ArrayList resizing
     * when the collection size is known upfront. This is particularly useful for large streams.
     * <p>
     * <b>Note:</b> By default, errors are not indexed. Use {@link #withIndex(Collector)} to add
     * automatic index prefixes.
     * <p>
     * <strong>Example with size hint and custom prefix:</strong>
     * <pre>{@code
     * List<Item> items = order.getItems(); // size = 50
     * Result<List<Item>> result = items.stream()
     *     .map(this::validateItem)
     *     .collect(withPrefix("order.items", withIndex(toResultList(items.size()))));
     *
     * // Errors: "order.items[0].price": ["Must be positive"]
     * }</pre>
     *
     * @param initialCapacity the initial capacity for the result list (performance hint)
     * @param <T>             the type of the success values
     * @return a collector that produces a result containing a list
     * @see #toResultList()
     * @see #withIndex(Collector)
     * @see #withPrefix(String, Collector)
     */
    static <T extends @Nullable Object> Collector<Result<T>, ListResultCollector.ToResultList<T>, Result<List<T>>> toResultList(
            int initialCapacity
    ) {
        return Collector.of(
                () -> new ListResultCollector.ToResultList<>(initialCapacity),
                ListResultCollector.ToResultList::add,
                ListResultCollector.ToResultList::combine,
                ListResultCollector.ToResultList::finish
        );
    }

    /**
     * Returns a {@link Collector} that partitions {@link Result} elements into success values and errors.
     * <p>
     * This collector returns a {@link PartitionedResult} containing both:
     * <ul>
     *   <li>A list of all successfully validated values (from {@link Result.Ok})</li>
     *   <li>All accumulated validation errors (from {@link Result.Err})</li>
     * </ul>
     * <p>
     * Unlike {@link #toListOrThrow()} and {@link #toResultList()}, this collector allows you to process
     * valid items even when some items fail validation. This is useful for partial success scenarios
     * where you want to proceed with valid data and log/report the failures.
     * <p>
     * <b>Note:</b> By default, errors are not indexed. Use {@link #withIndex(Collector)} to add
     * automatic index prefixes like {@code [0]}, {@code [1]}, etc.
     * <p>
     * <strong>Basic usage without indexing:</strong>
     * <pre>{@code
     * var partitioned = items.stream()
     *     .map(this::validateUser)
     *     .collect(toPartitioned());
     *
     * // Process valid items even if some failed
     * List<User> validUsers = partitioned.value();
     * ValidationErrors errors = partitioned.errors();
     *
     * if (errors.isNotEmpty()) {
     *     logger.warn("Processed {} valid, {} failed", validUsers.size(), errors.count());
     * }
     * processUsers(validUsers);
     * }</pre>
     * <p>
     * <strong>With automatic indexing:</strong>
     * <pre>{@code
     * var partitioned = items.stream()
     *     .map(this::validateUser)
     *     .collect(withIndex(toPartitioned()));
     *
     * // Errors include index prefixes: "[0].email", "[3].age", etc.
     * }</pre>
     *
     * @param <T> the type of the success values
     * @return a collector that produces a partitioned result
     * @see #toPartitioned(int)
     * @see #withIndex(Collector)
     * @see #withPrefix(String, Collector)
     * @see PartitionedResult
     */
    static <T extends @Nullable Object> Collector<Result<T>, ListResultCollector.ToPartitioned<T>, PartitionedResult<List<T>>> toPartitioned() {
        return Collector.of(
                ListResultCollector.ToPartitioned::new,
                ListResultCollector.ToPartitioned::add,
                ListResultCollector.ToPartitioned::combine,
                ListResultCollector.ToPartitioned::finish);
    }

    /**
     * Returns a {@link Collector} that partitions {@link Result} elements into success values and errors,
     * with an initial capacity hint for performance optimization.
     * <p>
     * This collector returns a {@link PartitionedResult} containing both:
     * <ul>
     *   <li>A list of all successfully validated values (from {@link Result.Ok})</li>
     *   <li>All accumulated validation errors (from {@link Result.Err})</li>
     * </ul>
     * <p>
     * The {@code initialCapacity} parameter is a performance hint to avoid ArrayList resizing
     * when the collection size is known upfront. This is particularly useful for large streams.
     * <p>
     * <b>Note:</b> By default, errors are not indexed. Use {@link #withIndex(Collector)} to add
     * automatic index prefixes.
     * <p>
     * <strong>Example with size hint and custom prefix:</strong>
     * <pre>{@code
     * List<Order> orders = getOrders(); // size = 200
     * var partitioned = orders.stream()
     *     .map(this::validateOrder)
     *     .collect(withPrefix("orders", withIndex(toPartitioned(orders.size()))));
     *
     * // Process valid orders even if some failed
     * processValidOrders(partitioned.value());
     *
     * if (partitioned.errors().isNotEmpty()) {
     *     // Errors: "orders[0].total", "orders[5].items", etc.
     *     notifyFailures(partitioned.errors());
     * }
     * }</pre>
     *
     * @param initialCapacity the initial capacity for the result list (performance hint)
     * @param <T>             the type of the success values
     * @return a collector that produces a partitioned result
     * @see #toPartitioned()
     * @see #withIndex(Collector)
     * @see #withPrefix(String, Collector)
     * @see PartitionedResult
     */
    static <T extends @Nullable Object> Collector<Result<T>, ListResultCollector.ToPartitioned<T>, PartitionedResult<List<T>>> toPartitioned(
            int initialCapacity
    ) {
        return Collector.of(
                () -> new ListResultCollector.ToPartitioned<>(initialCapacity),
                ListResultCollector.ToPartitioned::add,
                ListResultCollector.ToPartitioned::combine,
                ListResultCollector.ToPartitioned::finish);
    }
}
