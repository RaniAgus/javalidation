package io.github.raniagus.javalidation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import org.jspecify.annotations.Nullable;

public sealed abstract class ResultCollector<T extends @Nullable Object, R, SELF> {
    private final List<T> values = new ArrayList<>();
    private final Validation validation = Validation.create();
    private int index = 0;

    void add(Result<T> result) {
        switch (result) {
            case Result.Ok<T>(T value) -> values.add(value);
            case Result.Err<T>(ValidationErrors validationErrors) ->
                    validation.addAll("[" + index++ + "]", validationErrors);
        }
    }

    SELF combine(SELF other) {
        throw new UnsupportedOperationException("Result collectors are not concurrent");
    }

    R finish() {
        return finish(values, validation);
    }

    protected abstract R finish(List<T> values, Validation validation);

    public static <T extends @Nullable Object> Collector<Result<T>, ToList<T>, List<T>> toList() {
        return Collector.of(ToList::new, ToList::add, ToList::combine, ToList::finish);
    }

    public static <T extends @Nullable Object> Collector<Result<T>, ToResultList<T>, Result<List<T>>> toResultList() {
        return Collector.of(ToResultList::new, ToResultList::add, ToResultList::combine, ToResultList::finish);
    }

    public static <T extends @Nullable Object> Collector<Result<T>, ToPartitioned<T>, PartitionedResult<List<T>>> toPartitioned() {
        return Collector.of(ToPartitioned::new, ToPartitioned::add, ToPartitioned::combine, ToPartitioned::finish);
    }

    public static final class ToList<T extends @Nullable Object> extends ResultCollector<T, List<T>, ToList<T>> {
        @Override
        protected List<T> finish(List<T> values, Validation validation) {
            //noinspection DataFlowIssue
            return validation.checkAndGet(() -> List.copyOf(values));
        }
    }

    public static final class ToResultList<T extends @Nullable Object> extends ResultCollector<T, Result<List<T>>, ToResultList<T>> {
        @Override
        protected Result<List<T>> finish(List<T> values, Validation validation) {
            return validation.asResult(() -> List.copyOf(values));
        }
    }

    public static final class ToPartitioned<T extends @Nullable Object> extends ResultCollector<T, PartitionedResult<List<T>>, ToPartitioned<T>> {
        @Override
        protected PartitionedResult<List<T>> finish(List<T> values, Validation validation) {
            return new PartitionedResult<>(List.copyOf(values), validation.finish());
        }
    }
}
