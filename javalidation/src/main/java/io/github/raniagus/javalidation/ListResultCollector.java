package io.github.raniagus.javalidation;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

public abstract class ListResultCollector<T extends @Nullable Object, R, SELF extends ResultCollector<T, R, SELF>> implements ResultCollector<T, R, SELF> {
    protected final List<T> values;
    protected final Validation validation = Validation.create();

    ListResultCollector() {
        this.values = new ArrayList<>();
    }

    ListResultCollector(int initialCapacity) {
        this.values = new ArrayList<>(initialCapacity);
    }

    @Override
    public void add(Result<T> result) {
        switch (result) {
            case Result.Ok<T>(T value) -> values.add(value);
            case Result.Err<T>(ValidationErrors validationErrors) ->
                    validation.addAll(validationErrors);
        }
    }

    @Override
    public void add(Result<T> result, Object[] prefix) {
        switch (result) {
            case Result.Ok<T>(T value) -> values.add(value);
            case Result.Err<T>(ValidationErrors validationErrors) ->
                    validation.addAll(validationErrors, prefix);
        }
    }

    @Override
    public R finish() {
        return finish(values, validation);
    }

    protected abstract R finish(List<T> values, Validation validation);

    public static final class ToList<T extends @Nullable Object> extends ListResultCollector<T, List<T>, ToList<T>> {
        ToList() {
            super();
        }

        ToList(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public ToList<T> combine(ToList<T> other) {
            this.values.addAll(other.values);
            this.validation.addAll(other.validation);
            return this;
        }

        @Override
        protected List<T> finish(List<T> values, Validation validation) {
            return validation.checkAndGet(() -> List.copyOf(values));
        }
    }

    public static final class ToResultList<T extends @Nullable Object> extends ListResultCollector<T, Result<List<T>>, ToResultList<T>> {
        ToResultList() {
            super();
        }

        ToResultList(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public ToResultList<T> combine(ToResultList<T> other) {
            this.values.addAll(other.values);
            this.validation.addAll(other.validation);
            return this;
        }

        @Override
        protected Result<List<T>> finish(List<T> values, Validation validation) {
            return validation.asResult(() -> List.copyOf(values));
        }
    }

    public static final class ToPartitioned<T extends @Nullable Object> extends ListResultCollector<T, PartitionedResult<List<T>>, ToPartitioned<T>> {
        ToPartitioned() {
            super();
        }

        ToPartitioned(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public ToPartitioned<T> combine(ToPartitioned<T> other) {
            this.values.addAll(other.values);
            this.validation.addAll(other.validation);
            return this;
        }

        @Override
        protected PartitionedResult<List<T>> finish(List<T> values, Validation validation) {
            return new PartitionedResult<>(List.copyOf(values), validation.finish());
        }
    }
}
