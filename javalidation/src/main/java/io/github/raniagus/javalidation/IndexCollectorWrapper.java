package io.github.raniagus.javalidation;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import org.jspecify.annotations.Nullable;

public class IndexCollectorWrapper<T extends @Nullable Object, R, C extends ResultCollector<T, R>> implements Collector<Result<T>, C, R> {
    private final Collector<Result<T>, C, R> collector;
    private final String prefix;
    private int index = 0;

    public IndexCollectorWrapper(Collector<Result<T>, C, R> collector) {
        this(collector, "");
    }

    public IndexCollectorWrapper(Collector<Result<T>, C, R> collector, String prefix) {
        this.collector = collector;
        this.prefix = prefix;
    }

    @Override
    public Supplier<C> supplier() {
        return () -> collector.supplier().get();
    }

    @Override
    public BiConsumer<C, Result<T>> accumulator() {
        return (c, result) -> c.add(result.withPrefix(prefix, "[", index++, "]"));
    }

    @Override
    public BinaryOperator<C> combiner() {
        return (c1, c2) -> {
            throw new UnsupportedOperationException();
        };
    }

    @Override
    public Function<C, R> finisher() {
        return collector.finisher();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }
}
