package io.github.raniagus.javalidation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ValidationErrors(
        List<String> rootErrors,
        Map<String, List<String>> fieldErrors
) {
    public ValidationErrors {
        rootErrors = List.copyOf(rootErrors);
        fieldErrors = Map.copyOf(fieldErrors.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> List.copyOf(e.getValue()))));
    }

    public static ValidationErrors empty() {
        return new ValidationErrors(List.of(), Map.of());
    }

    public static ValidationErrors of(String message) {
        return new ValidationErrors(List.of(message), Map.of());
    }

    public static ValidationErrors of(List<String> messages) {
        return new ValidationErrors(List.copyOf(messages), Map.of());
    }

    public static ValidationErrors of(String field, String message) {
        return new ValidationErrors(List.of(), Map.of(field, List.of(message)));
    }

    public ValidationErrors withPrefix(String prefix) {
        Validator validator = new Validator();
        validator.add(prefix, rootErrors);
        for (Map.Entry<String, List<String>> entry : fieldErrors.entrySet()) {
            validator.add(prefix + entry.getKey(), entry.getValue());
        }
        return validator.finish();
    }

    public ValidationErrors withPrefix(Object first, Object... rest) {
        var sb = new StringBuilder().append(first);
        for (Object o : rest) {
            sb.append(o);
        }
        return withPrefix(sb.toString());
    }

    public boolean isEmpty() {
        return rootErrors.isEmpty() && fieldErrors.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }
}
