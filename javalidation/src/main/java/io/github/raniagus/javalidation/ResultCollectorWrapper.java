package io.github.raniagus.javalidation;

import java.util.Arrays;
import java.util.stream.Collector;
import org.jspecify.annotations.Nullable;

public abstract class ResultCollectorWrapper<T extends @Nullable Object, R, C extends ResultCollector<T, R, C>, SELF extends ResultCollector<T, R, SELF>> implements ResultCollector<T, R, SELF> {
    protected final C resultCollector;

    public ResultCollectorWrapper(C resultCollector) {
        this.resultCollector = resultCollector;
    }

    public ResultCollectorWrapper(Collector<Result<T>, C, R> collector) {
        this(collector.supplier().get());
    }

    @Override
    public void add(Result<T> result) {
        resultCollector.add(result);
    }

    @Override
    public void add(Result<T> result, Object[] prefix) {
        resultCollector.add(result, prefix);
    }

    @Override
    public R finish() {
        return resultCollector.finish();
    }

    public static class WithIndex<T extends @Nullable Object, R, C extends ResultCollector<T, R, C>> extends ResultCollectorWrapper<T, R, C, WithIndex<T, R, C>> {
        private int index = 0;

        public WithIndex(Collector<Result<T>, C, R> collector) {
            super(collector);
        }

        @Override
        public void add(Result<T> result) {
            super.add(result, new Object[]{index++});
        }

        @Override
        public void add(Result<T> result, Object[] parts) {
            Object[] newPrefix = Arrays.copyOf(parts, parts.length + 1);
            newPrefix[parts.length] = index++;
            super.add(result, newPrefix);
        }

        @Override
        public WithIndex<T, R, C> combine(WithIndex<T, R, C> other) {
            throw new UnsupportedOperationException("Cannot combine index-prefixed collectors");
        }
    }

    public static class WithPrefix<T extends @Nullable Object, R, C extends ResultCollector<T, R, C>> extends ResultCollectorWrapper<T, R, C, WithPrefix<T, R, C>> {
        private final Object prefix;

        public WithPrefix(C collector, Object prefix) {
            super(collector);
            this.prefix = prefix;
        }

        public WithPrefix(Collector<Result<T>, C, R> collector, Object prefix) {
            super(collector);
            this.prefix = prefix;
        }

        @Override
        public void add(Result<T> result) {
            super.add(result, new Object[]{prefix});
        }

        @Override
        public void add(Result<T> result, Object[] parts) {
            Object[] newPrefix = Arrays.copyOf(parts, parts.length + 1);
            newPrefix[parts.length] = prefix;
            super.add(result, newPrefix);
        }

        @Override
        public WithPrefix<T, R, C> combine(WithPrefix<T, R, C> other) {
            return new WithPrefix<>(resultCollector.combine(other.resultCollector), prefix);
        }
    }
}
