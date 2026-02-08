package io.github.raniagus.javalidation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import org.jspecify.annotations.Nullable;

public sealed abstract class ResultCollector<T extends @Nullable Object, R, SELF> {
    private final List<T> values;
    private final Validation validation = Validation.create();
    private final String prefix;
    private int index = 0;

    ResultCollector(String prefix) {
        this.prefix = prefix;
        this.values = new ArrayList<>();
    }

    ResultCollector(String prefix, int initialCapacity) {
        this.prefix = prefix;
        this.values = new ArrayList<>(initialCapacity);
    }

    void add(Result<T> result) {
        switch (result) {
            case Result.Ok<T>(T value) -> values.add(value);
            case Result.Err<T>(ValidationErrors validationErrors) ->
                    validation.addAll(prefix + "[" + index++ + "]", validationErrors);
        }
    }

    SELF combine(SELF other) {
        throw new UnsupportedOperationException("Result collectors are not concurrent");
    }

    R finish() {
        return finish(values, validation);
    }

    protected abstract R finish(List<T> values, Validation validation);

    public static final class ToList<T extends @Nullable Object> extends ResultCollector<T, List<T>, ToList<T>> {
        private ToList(String prefix) {
            super(prefix);
        }

        private ToList(String prefix, int initialCapacity) {
            super(prefix, initialCapacity);
        }

        @Override
        protected List<T> finish(List<T> values, Validation validation) {
            return validation.checkAndGet(() -> List.copyOf(values));
        }
    }

    public static final class ToResultList<T extends @Nullable Object> extends ResultCollector<T, Result<List<T>>, ToResultList<T>> {
        private ToResultList(String prefix) {
            super(prefix);
        }

        private ToResultList(String prefix, int initialCapacity) {
            super(prefix, initialCapacity);
        }

        @Override
        protected Result<List<T>> finish(List<T> values, Validation validation) {
            return validation.asResult(() -> List.copyOf(values));
        }
    }

    public static final class ToPartitioned<T extends @Nullable Object> extends ResultCollector<T, PartitionedResult<List<T>>, ToPartitioned<T>> {
        private ToPartitioned(String prefix) {
            super(prefix);
        }

        private ToPartitioned(String prefix, int initialCapacity) {
            super(prefix, initialCapacity);
        }

        @Override
        protected PartitionedResult<List<T>> finish(List<T> values, Validation validation) {
            return new PartitionedResult<>(List.copyOf(values), validation.finish());
        }
    }

    /**
     * Returns a {@link Collector} that accumulates {@link Result} elements into a {@link List},
     * with a custom prefix prepended to error field paths.
     * <p>
     * If all results are {@link Result.Ok}, returns a list containing all success values.
     * If any result is {@link Result.Err}, throws {@link JavalidationException} with accumulated errors.
     * <p>
     * Errors are indexed as {@code prefix[0]}, {@code prefix[1]}, etc., enabling validation of
     * nested collections or named lists:
     * <pre>{@code
     * // Validate items in an order
     * List<Item> items = order.getItems().stream()
     *     .map(item -> validateItem(item))
     *     .collect(ResultCollector.toList("order.items"));
     * // Errors appear as: "order.items[0].field", "order.items[1].field", etc.
     * }</pre>
     *
     * @param prefix the prefix to prepend to error field paths (e.g., "items", "order.lines")
     * @param <T> the type of the success values
     * @return a collector that produces a list or throws on errors
     * @throws JavalidationException if any result is {@link Result.Err}
     * @see #toList(String, int)
     */
    public static <T extends @Nullable Object> Collector<Result<T>, ToList<T>, List<T>> toList(String prefix) {
        return Collector.of(() -> new ToList<>(prefix), ToList::add, ToList::combine, ToList::finish);
    }

    /**
     * Returns a {@link Collector} that accumulates {@link Result} elements into a {@link List},
     * with a custom prefix prepended to error field paths and an initial capacity hint.
     * <p>
     * If all results are {@link Result.Ok}, returns a list containing all success values.
     * If any result is {@link Result.Err}, throws {@link JavalidationException} with accumulated errors.
     * <p>
     * The initial capacity parameter helps avoid ArrayList resizing for better performance when
     * the expected collection size is known:
     * <pre>{@code
     * // Validate items in an order with known size
     * List<Item> items = order.getItems().stream()
     *     .map(item -> validateItem(item))
     *     .collect(ResultCollector.toList("order.items", order.getItems().size()));
     * // Errors appear as: "order.items[0].field", "order.items[1].field", etc.
     * }</pre>
     *
     * @param prefix the prefix to prepend to error field paths (e.g., "items", "order.lines")
     * @param initialCapacity the initial capacity for the result list (performance hint)
     * @param <T> the type of the success values
     * @return a collector that produces a list or throws on errors
     * @throws JavalidationException if any result is {@link Result.Err}
     * @see #toList(String)
     */
    public static <T extends @Nullable Object> Collector<Result<T>, ToList<T>, List<T>> toList(String prefix, int initialCapacity) {
        return Collector.of(() -> new ToList<>(prefix, initialCapacity), ToList::add, ToList::combine, ToList::finish);
    }

    /**
     * Returns a {@link Collector} that accumulates {@link Result} elements into a {@link Result} of {@link List},
     * with a custom prefix prepended to error field paths.
     * <p>
     * If all results are {@link Result.Ok}, returns {@link Result.Ok} containing a list of all success values.
     * If any result is {@link Result.Err}, returns {@link Result.Err} with accumulated errors.
     * <p>
     * Errors are indexed as {@code prefix[0]}, {@code prefix[1]}, etc.:
     * <pre>{@code
     * Result<Order> order = validateOrderItems(order.getItems())
     *     .collect(ResultCollector.toResultList("items"))
     *     .map(items -> new Order(order.getId(), items));
     * // Errors appear as: "items[0].field", "items[1].field", etc.
     * }</pre>
     *
     * @param prefix the prefix to prepend to error field paths (e.g., "items", "order.lines")
     * @param <T> the type of the success values
     * @return a collector that produces a result containing a list
     * @see #toResultList(String, int)
     */
    public static <T extends @Nullable Object> Collector<Result<T>, ToResultList<T>, Result<List<T>>> toResultList(String prefix) {
        return Collector.of(() -> new ToResultList<>(prefix), ToResultList::add, ToResultList::combine, ToResultList::finish);
    }

    /**
     * Returns a {@link Collector} that accumulates {@link Result} elements into a {@link Result} of {@link List},
     * with a custom prefix prepended to error field paths and an initial capacity hint.
     * <p>
     * If all results are {@link Result.Ok}, returns {@link Result.Ok} containing a list of all success values.
     * If any result is {@link Result.Err}, returns {@link Result.Err} with accumulated errors.
     * <p>
     * The initial capacity parameter helps avoid ArrayList resizing for better performance:
     * <pre>{@code
     * Result<Order> order = validateOrderItems(order.getItems())
     *     .collect(ResultCollector.toResultList("items", order.getItems().size()))
     *     .map(items -> new Order(order.getId(), items));
     * // Errors appear as: "items[0].field", "items[1].field", etc.
     * }</pre>
     *
     * @param prefix the prefix to prepend to error field paths (e.g., "items", "order.lines")
     * @param initialCapacity the initial capacity for the result list (performance hint)
     * @param <T> the type of the success values
     * @return a collector that produces a result containing a list
     * @see #toResultList(String)
     */
    public static <T extends @Nullable Object> Collector<Result<T>, ToResultList<T>, Result<List<T>>> toResultList(String prefix, int initialCapacity) {
        return Collector.of(() -> new ToResultList<>(prefix, initialCapacity), ToResultList::add, ToResultList::combine, ToResultList::finish);
    }

    /**
     * Returns a {@link Collector} that partitions {@link Result} elements into success values and errors,
     * with a custom prefix prepended to error field paths.
     * <p>
     * Always returns a {@link PartitionedResult} containing:
     * <ul>
     *   <li>A list of all success values from {@link Result.Ok} elements</li>
     *   <li>Accumulated {@link ValidationErrors} from {@link Result.Err} elements</li>
     * </ul>
     * <p>
     * Errors are indexed as {@code prefix[0]}, {@code prefix[1]}, etc.:
     * <pre>{@code
     * PartitionedResult<List<Item>> partitioned = order.getItems().stream()
     *     .map(item -> validateItem(item))
     *     .collect(ResultCollector.toPartitioned("order.items"));
     *
     * List<Item> validItems = partitioned.value();
     * ValidationErrors errors = partitioned.errors();
     * // Errors appear as: "order.items[0].field", "order.items[1].field", etc.
     * }</pre>
     *
     * @param prefix the prefix to prepend to error field paths (e.g., "items", "order.lines")
     * @param <T> the type of the success values
     * @return a collector that produces a partitioned result
     * @see #toPartitioned(String, int)
     */
    public static <T extends @Nullable Object> Collector<Result<T>, ToPartitioned<T>, PartitionedResult<List<T>>> toPartitioned(String prefix) {
        return Collector.of(() -> new ToPartitioned<>(prefix), ToPartitioned::add, ToPartitioned::combine, ToPartitioned::finish);
    }

    /**
     * Returns a {@link Collector} that partitions {@link Result} elements into success values and errors,
     * with a custom prefix prepended to error field paths and an initial capacity hint.
     * <p>
     * Always returns a {@link PartitionedResult} containing:
     * <ul>
     *   <li>A list of all success values from {@link Result.Ok} elements</li>
     *   <li>Accumulated {@link ValidationErrors} from {@link Result.Err} elements</li>
     * </ul>
     * <p>
     * The initial capacity parameter helps avoid ArrayList resizing for better performance:
     * <pre>{@code
     * PartitionedResult<List<Item>> partitioned = order.getItems().stream()
     *     .map(item -> validateItem(item))
     *     .collect(ResultCollector.toPartitioned("order.items", order.getItems().size()));
     *
     * List<Item> validItems = partitioned.value();
     * ValidationErrors errors = partitioned.errors();
     * // Errors appear as: "order.items[0].field", "order.items[1].field", etc.
     * }</pre>
     *
     * @param prefix the prefix to prepend to error field paths (e.g., "items", "order.lines")
     * @param initialCapacity the initial capacity for the result list (performance hint)
     * @param <T> the type of the success values
     * @return a collector that produces a partitioned result
     * @see #toPartitioned(String)
     */
    public static <T extends @Nullable Object> Collector<Result<T>, ToPartitioned<T>, PartitionedResult<List<T>>> toPartitioned(String prefix, int initialCapacity) {
        return Collector.of(() -> new ToPartitioned<>(prefix, initialCapacity), ToPartitioned::add, ToPartitioned::combine, ToPartitioned::finish);
    }
}
