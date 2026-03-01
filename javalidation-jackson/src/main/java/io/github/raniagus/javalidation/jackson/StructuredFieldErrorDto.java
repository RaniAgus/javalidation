package io.github.raniagus.javalidation.jackson;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

record StructuredFieldErrorDto(Object[] key, List<StructuredErrorDto> errors) {
    public StructuredFieldErrorDto {
        key = Arrays.copyOf(key, key.length);
        errors = List.copyOf(errors);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StructuredFieldErrorDto(Object[] k, List<StructuredErrorDto> e))) return false;
        return Arrays.equals(key, k) && Objects.equals(errors, e);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(key);
        result = 31 * result + Objects.hashCode(errors);
        return result;
    }

    @Override
    public String toString() {
        return "StructuredFieldErrorDto{" +
                "key=" + Arrays.toString(key) +
                ", errors=" + errors +
                '}';
    }
}
