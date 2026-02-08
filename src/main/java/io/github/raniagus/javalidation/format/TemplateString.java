package io.github.raniagus.javalidation.format;

import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

public record TemplateString(String message, @NonNull Object[] args) {
    public TemplateString {
        args = Arrays.copyOf(args, args.length);
    }

    public static TemplateString of(String message, Object... args) {
        return new TemplateString(message, args);
    }

    @Override
    public @NonNull String toString() {
        return "TemplateString{" +
                "message='" + message + '\'' +
                ", args=" + Arrays.toString(args) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TemplateString(String template1, Object[] values1))) return false;
        return Objects.equals(message(), template1) && Arrays.equals(args(), values1);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(message());
        result = 31 * result + Arrays.hashCode(args());
        return result;
    }
}
