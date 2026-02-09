package io.github.raniagus.javalidation;

import java.util.List;
import java.util.stream.Collector;
import org.jspecify.annotations.Nullable;

/**
 * Factory methods for creating collectors that accumulate {@link Result} elements from streams.
 * <p>
 * This class provides three types of collectors:
 * <ul>
 *   <li>{@link #toList()} - Collects into a {@link List}, throwing on errors</li>
 *   <li>{@link #toResultList()} - Collects into a {@link Result} of {@link List}</li>
 *   <li>{@link #toPartitioned()} - Collects valid items and errors separately</li>
 * </ul>
 * <p>
 * By default, collectors do not automatically index errors. Use the {@link #indexed(Collector)} wrapper
 * to add automatic index prefixes like {@code [0]}, {@code [1]}, etc. to errors based on item position
 * in the stream.
 *
 * <h2>Basic Usage (No Indexing)</h2>
 * <pre>{@code
 * Result<List<User>> result = users.stream()
 *     .map(this::validateUser)
 *     .collect(ResultCollectors.toResultList());
 * // Errors: "field": ["Error message"]
 * }</pre>
 *
 * <h2>With Automatic Indexing</h2>
 * <pre>{@code
 * Result<List<User>> result = users.stream()
 *     .map(this::validateUser)
 *     .collect(ResultCollectors.indexed(ResultCollectors.toResultList()));
 * // Errors: "[0].field": ["Error message"], "[2].field": ["Another error"]
 * }</pre>
 *
 * <h2>With Custom Prefix</h2>
 * <pre>{@code
 * Result<List<Item>> items = order.getItems().stream()
 *     .map(this::validateItem)
 *     .collect(ResultCollectors.indexed(ResultCollectors.toResultList(), "items"));
 * // Errors: "items[0].price": ["Must be positive"]
 * }</pre>
 *
 * @see Result
 * @see ValidationErrors
 * @see PartitionedResult
 */
public final class ResultCollectors {

    private ResultCollectors() {}

    /**
     * Wraps a collector to automatically add index prefixes to validation errors.
     * <p>
     * Each item in the stream is assigned an index starting from 0. Errors from that item
     * are prefixed with {@code [index]}, for example: {@code [0].field}, {@code [1].field}, etc.
     * <p>
     * This wrapper is essential for identifying which items in a collection failed validation.
     *
     * <h2>Example</h2>
     * <pre>{@code
     * // Without indexing
     * Result<List<Item>> result = stream.collect(ResultCollectors.toResultList());
     * // Errors: "field": ["Error"]
     *
     * // With indexing
     * Result<List<Item>> result = stream.collect(
     *     ResultCollectors.indexed(ResultCollectors.toResultList())
     * );
     * // Errors: "[0].field": ["Error"], "[2].field": ["Another error"]
     * }</pre>
     *
     * @param collector the collector to wrap
     * @param <T> the type of the success values
     * @param <R> the result type of the collector
     * @param <C> the accumulator type
     * @return a collector that adds automatic index prefixes to errors
     * @see #indexed(Collector, String)
     */
    public static <T extends @Nullable Object, R, C extends ResultCollector<T, R>> Collector<Result<T>, C, R> indexed(Collector<Result<T>, C, R> collector) {
        return new IndexCollectorWrapper<>(collector);
    }

    /**
     * Wraps a collector to automatically add custom-prefixed index notation to validation errors.
     * <p>
     * Each item in the stream is assigned an index starting from 0. Errors from that item
     * are prefixed with {@code prefix[index]}, for example: {@code items[0].field}, {@code items[1].field}, etc.
     * <p>
     * This is particularly useful for nested structures where you want to namespace errors
     * within a parent context.
     *
     * <h2>Example</h2>
     * <pre>{@code
     * Result<List<Item>> items = order.getItems().stream()
     *     .map(this::validateItem)
     *     .collect(ResultCollectors.indexed(ResultCollectors.toResultList(), "order.items"));
     * // Errors: "order.items[0].price": ["Must be positive"]
     *
     * try {
     *     List<Address> addresses = user.getAddresses().stream()
     *         .map(this::validateAddress)
     *         .collect(ResultCollectors.indexed(ResultCollectors.toList(), "addresses"));
     * } catch (JavalidationException e) {
     *     // Errors: "addresses[0].street": ["Required"], "addresses[1].zipCode": ["Invalid format"]
     * }
     * }</pre>
     *
     * @param collector the collector to wrap
     * @param prefix the prefix to prepend before the index (e.g., "items", "order.items")
     * @param <T> the type of the success values
     * @param <R> the result type of the collector
     * @param <C> the accumulator type
     * @return a collector that adds custom-prefixed index notation to errors
     * @see #indexed(Collector)
     */
    public static <T extends @Nullable Object, R, C extends ResultCollector<T, R>> Collector<Result<T>, C, R> indexed(Collector<Result<T>, C, R> collector, String prefix) {
        return new IndexCollectorWrapper<>(collector, prefix);
    }

    /**
     * Returns a {@link Collector} that accumulates {@link Result} elements into a {@link List}.
     * <p>
     * This collector accumulates all validation errors before throwing. If any {@link Result.Err}
     * is encountered, a {@link JavalidationException} is thrown containing all accumulated errors.
     * Otherwise, returns a list of all success values.
     * <p>
     * <b>Note:</b> By default, errors are not indexed. Use {@link #indexed(Collector)} to add
     * automatic index prefixes like {@code [0]}, {@code [1]}, etc.
     *
     * <h2>Basic Usage</h2>
     * <pre>{@code
     * try {
     *     List<User> users = items.stream()
     *         .map(this::validateUser)
     *         .collect(ResultCollectors.toList());
     *     processUsers(users);
     * } catch (JavalidationException e) {
     *     // Contains ALL errors (without indexes)
     *     logErrors(e.getErrors());
     * }
     * }</pre>
     *
     * <h2>With Automatic Indexing</h2>
     * <pre>{@code
     * try {
     *     List<User> users = items.stream()
     *         .map(this::validateUser)
     *         .collect(ResultCollectors.indexed(ResultCollectors.toList()));
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
     * @see #toList(int)
     * @see #indexed(Collector)
     */
    public static <T extends @Nullable Object> Collector<Result<T>, ResultCollector.ToList<T>, List<T>> toList() {
        return Collector.of(
                ResultCollector.ToList::new,
                ResultCollector.ToList::add,
                ResultCollector.ToList::combine,
                ResultCollector.ToList::finish);
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
     * <b>Note:</b> By default, errors are not indexed. Use {@link #indexed(Collector)} to add
     * automatic index prefixes.
     *
     * <h2>Example with Size Hint</h2>
     * <pre>{@code
     * List<Item> items = getItems(); // size = 1000
     * try {
     *     List<Item> validated = items.stream()
     *         .map(this::validateItem)
     *         .collect(ResultCollectors.indexed(
     *             ResultCollectors.toList(items.size())
     *         ));
     * } catch (JavalidationException e) {
     *     logErrors(e.getErrors());
     * }
     * }</pre>
     *
     * @param initialCapacity the initial capacity for the result list (performance hint)
     * @param <T> the type of the success values
     * @return a collector that produces a list or throws on errors
     * @throws JavalidationException if any result is {@link Result.Err}
     * @see #toList()
     * @see #indexed(Collector, String)
     */
    public static <T extends @Nullable Object> Collector<Result<T>, ResultCollector.ToList<T>, List<T>> toList(int initialCapacity) {
        return Collector.of(
                () -> new ResultCollector.ToList<>(initialCapacity),
                ResultCollector.ToList::add,
                ResultCollector.ToList::combine,
                ResultCollector.ToList::finish);
    }

    /**
     * Returns a {@link Collector} that accumulates {@link Result} elements into a {@link Result} of {@link List}.
     * <p>
     * This collector accumulates all validation errors and returns them in a {@link Result}.
     * If all results are {@link Result.Ok}, returns {@code Ok(List<T>)}. If any {@link Result.Err}
     * is encountered, returns {@code Err(ValidationErrors)} with all accumulated errors.
     * <p>
     * This is the functional alternative to {@link #toList()}, returning a {@link Result} instead
     * of throwing an exception.
     * <p>
     * <b>Note:</b> By default, errors are not indexed. Use {@link #indexed(Collector)} to add
     * automatic index prefixes like {@code [0]}, {@code [1]}, etc.
     *
     * <h2>Basic Usage</h2>
     * <pre>{@code
     * Result<List<User>> result = items.stream()
     *     .map(this::validateUser)
     *     .collect(ResultCollectors.toResultList());
     *
     * switch (result) {
     *     case Result.Ok(List<User> users) -> processUsers(users);
     *     case Result.Err(ValidationErrors errors) -> logErrors(errors);
     * }
     * }</pre>
     *
     * <h2>With Automatic Indexing</h2>
     * <pre>{@code
     * Result<List<User>> result = items.stream()
     *     .map(this::validateUser)
     *     .collect(ResultCollectors.indexed(ResultCollectors.toResultList()));
     *
     * // Errors include index prefixes: "[0].email", "[2].age", etc.
     * }</pre>
     *
     * @param <T> the type of the success values
     * @return a collector that produces a result containing a list
     * @see #toResultList(int)
     * @see #indexed(Collector)
     */
    public static <T extends @Nullable Object> Collector<Result<T>, ResultCollector.ToResultList<T>, Result<List<T>>> toResultList() {
        return Collector.of(
                ResultCollector.ToResultList::new,
                ResultCollector.ToResultList::add,
                ResultCollector.ToResultList::combine,
                ResultCollector.ToResultList::finish);
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
     * <b>Note:</b> By default, errors are not indexed. Use {@link #indexed(Collector, String)} to add
     * automatic index prefixes with a custom namespace.
     *
     * <h2>Example with Size Hint and Prefix</h2>
     * <pre>{@code
     * List<Item> items = order.getItems(); // size = 50
     * Result<List<Item>> result = items.stream()
     *     .map(this::validateItem)
     *     .collect(ResultCollectors.indexed(
     *         ResultCollectors.toResultList(items.size()),
     *         "order.items"
     *     ));
     *
     * // Errors: "order.items[0].price": ["Must be positive"]
     * }</pre>
     *
     * @param initialCapacity the initial capacity for the result list (performance hint)
     * @param <T> the type of the success values
     * @return a collector that produces a result containing a list
     * @see #toResultList()
     * @see #indexed(Collector, String)
     */
    public static <T extends @Nullable Object> Collector<Result<T>, ResultCollector.ToResultList<T>, Result<List<T>>> toResultList(int initialCapacity) {
        return Collector.of(
                () -> new ResultCollector.ToResultList<>(initialCapacity),
                ResultCollector.ToResultList::add,
                ResultCollector.ToResultList::combine,
                ResultCollector.ToResultList::finish
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
     * Unlike {@link #toList()} and {@link #toResultList()}, this collector allows you to process
     * valid items even when some items fail validation. This is useful for partial success scenarios
     * where you want to proceed with valid data and log/report the failures.
     * <p>
     * <b>Note:</b> By default, errors are not indexed. Use {@link #indexed(Collector)} to add
     * automatic index prefixes like {@code [0]}, {@code [1]}, etc.
     *
     * <h2>Basic Usage</h2>
     * <pre>{@code
     * var partitioned = items.stream()
     *     .map(this::validateUser)
     *     .collect(ResultCollectors.toPartitioned());
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
     *
     * <h2>With Automatic Indexing</h2>
     * <pre>{@code
     * var partitioned = items.stream()
     *     .map(this::validateUser)
     *     .collect(ResultCollectors.indexed(ResultCollectors.toPartitioned()));
     *
     * // Errors include index prefixes: "[0].email", "[3].age", etc.
     * }</pre>
     *
     * @param <T> the type of the success values
     * @return a collector that produces a partitioned result
     * @see #toPartitioned(int)
     * @see #indexed(Collector)
     * @see PartitionedResult
     */
    public static <T extends @Nullable Object> Collector<Result<T>, ResultCollector.ToPartitioned<T>, PartitionedResult<List<T>>> toPartitioned() {
        return Collector.of(
                ResultCollector.ToPartitioned::new,
                ResultCollector.ToPartitioned::add,
                ResultCollector.ToPartitioned::combine,
                ResultCollector.ToPartitioned::finish);
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
     * <b>Note:</b> By default, errors are not indexed. Use {@link #indexed(Collector, String)} to add
     * automatic index prefixes with a custom namespace.
     *
     * <h2>Example with Size Hint and Prefix</h2>
     * <pre>{@code
     * List<Order> orders = getOrders(); // size = 200
     * var partitioned = orders.stream()
     *     .map(this::validateOrder)
     *     .collect(ResultCollectors.indexed(
     *         ResultCollectors.toPartitioned(orders.size()),
     *         "orders"
     *     ));
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
     * @param <T> the type of the success values
     * @return a collector that produces a partitioned result
     * @see #toPartitioned()
     * @see #indexed(Collector, String)
     * @see PartitionedResult
     */
    public static <T extends @Nullable Object> Collector<Result<T>, ResultCollector.ToPartitioned<T>, PartitionedResult<List<T>>> toPartitioned(int initialCapacity) {
        return Collector.of(
                () -> new ResultCollector.ToPartitioned<>(initialCapacity),
                ResultCollector.ToPartitioned::add,
                ResultCollector.ToPartitioned::combine,
                ResultCollector.ToPartitioned::finish);
    }
}
