package io.github.raniagus.javalidation;

import org.jspecify.annotations.Nullable;

public record PartitionedResult<T extends @Nullable Object>(T value, ValidationErrors errors) {
}
