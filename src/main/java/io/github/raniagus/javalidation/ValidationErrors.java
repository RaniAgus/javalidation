package io.github.raniagus.javalidation;

import io.github.raniagus.javalidation.template.TemplateString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ValidationErrors(
        List<TemplateString> rootErrors,
        Map<String, List<TemplateString>> fieldErrors
) {
    public ValidationErrors {
        rootErrors = List.copyOf(rootErrors);
        fieldErrors = Map.copyOf(fieldErrors.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> List.copyOf(e.getValue()))));
    }

    public static ValidationErrors empty() {
        return new ValidationErrors(List.of(), Map.of());
    }

    public static ValidationErrors of(String message, Object... args) {
        return new ValidationErrors(List.of(new TemplateString(message, args)), Map.of());
    }

    public static ValidationErrors of(String field, String message, Object... args) {
        return new ValidationErrors(List.of(), Map.of(field, List.of(new TemplateString(message, args))));
    }

    public ValidationErrors withPrefix(String prefix) {
        Map<String, List<TemplateString>> prefixedFieldErrors = new HashMap<>(fieldErrors.size() + 1);
        if (!rootErrors.isEmpty()) {
            prefixedFieldErrors.put(prefix, rootErrors);
        }
        for (Map.Entry<String, List<TemplateString>> entry : fieldErrors.entrySet()) {
            prefixedFieldErrors.put(prefix + entry.getKey(), entry.getValue());
        }
        return new ValidationErrors(new ArrayList<>(), prefixedFieldErrors);
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
