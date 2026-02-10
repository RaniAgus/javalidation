package io.github.raniagus.javalidation;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

public sealed abstract class ResultCollector<T extends @Nullable Object, R> {
    private final List<T> values;
    protected final Validation validation = Validation.create();

    ResultCollector() {
        this.values = new ArrayList<>();
    }

    ResultCollector(int initialCapacity) {
        this.values = new ArrayList<>(initialCapacity);
    }

    void add(Result<T> result) {
        switch (result) {
            case Result.Ok<T>(T value) -> values.add(value);
            case Result.Err<T>(ValidationErrors validationErrors) ->
                    validation.addAll(validationErrors);
        }
    }

    void add(Result<T> result, StringBuilder prefix) {
        switch (result) {
            case Result.Ok<T>(T value) -> values.add(value);
            case Result.Err<T>(ValidationErrors validationErrors) ->
                    validation.addAll(validationErrors, prefix);
        }
    }

    R finish() {
        return finish(values, validation);
    }

    protected abstract R finish(List<T> values, Validation validation);

    public static final class ToList<T extends @Nullable Object> extends ResultCollector<T, List<T>> {
        ToList() {
            super();
        }

        ToList(int initialCapacity) {
            super(initialCapacity);
        }

        ToList<T> combine(ToList<T> other) {
            this.validation.addAll(other.validation.finish());
            return this;
        }

        @Override
        protected List<T> finish(List<T> values, Validation validation) {
            return validation.checkAndGet(() -> List.copyOf(values));
        }
    }

    public static final class ToResultList<T extends @Nullable Object> extends ResultCollector<T, Result<List<T>>> {
        ToResultList() {
            super();
        }

        ToResultList(int initialCapacity) {
            super(initialCapacity);
        }

        ToResultList<T> combine(ToResultList<T> other) {
            this.validation.addAll(other.validation.finish());
            return this;
        }

        @Override
        protected Result<List<T>> finish(List<T> values, Validation validation) {
            return validation.asResult(() -> List.copyOf(values));
        }
    }

    public static final class ToPartitioned<T extends @Nullable Object> extends ResultCollector<T, PartitionedResult<List<T>>> {
        ToPartitioned() {
            super();
        }

        ToPartitioned(int initialCapacity) {
            super(initialCapacity);
        }

        ToPartitioned<T> combine(ToPartitioned<T> other) {
            this.validation.addAll(other.validation.finish());
            return this;
        }

        @Override
        protected PartitionedResult<List<T>> finish(List<T> values, Validation validation) {
            return new PartitionedResult<>(List.copyOf(values), validation.finish());
        }
    }
}
