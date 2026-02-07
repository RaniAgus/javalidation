package io.github.raniagus.javalidation.template;

import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

public record TemplateString(String template, Object... values) {
    public TemplateString {
        values = Arrays.copyOf(values, values.length);
    }

    @Override
    public @NonNull String toString() {
        return "TemplateString{" +
                "template='" + template + '\'' +
                ", values=" + Arrays.toString(values) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TemplateString(String template1, Object[] values1))) return false;
        return Objects.equals(template(), template1) && Arrays.equals(values(), values1);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(template());
        result = 31 * result + Arrays.hashCode(values());
        return result;
    }
}
