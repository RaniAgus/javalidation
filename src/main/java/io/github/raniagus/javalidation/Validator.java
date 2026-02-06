package io.github.raniagus.javalidation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

public class Validator {
    private final List<String> rootErrors = new ArrayList<>();
    private final Map<String, List<String>> fieldErrors = new HashMap<>();

    public Validator add(String message) {
        Objects.requireNonNull(message);
        rootErrors.add(message);
        return this;
    }

    public Validator addAll(List<String> messages) {
        Objects.requireNonNull(messages);
        rootErrors.addAll(messages);
        return this;
    }

    public Validator add(String field, String message) {
        Objects.requireNonNull(field);
        Objects.requireNonNull(message);
        fieldErrors.computeIfAbsent(field, k -> new ArrayList<>(1)).add(message);
        return this;
    }

    public Validator add(String field, List<String> messages) {
        Objects.requireNonNull(field);
        Objects.requireNonNull(messages);
        if (!messages.isEmpty()) {
            fieldErrors.computeIfAbsent(field, k -> new ArrayList<>(messages.size())).addAll(messages);
        }
        return this;
    }

    public Validator addAll(Map<String, List<String>> fieldErrors) {
        Objects.requireNonNull(fieldErrors);
        fieldErrors.forEach(this::add);
        return this;
    }

    public Validator addAll(ValidationErrors errors) {
        Objects.requireNonNull(errors);
        addAll(errors.rootErrors());
        addAll(errors.fieldErrors());
        return this;
    }

    public Validator addAll(String prefix, ValidationErrors errors) {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(errors);
        if (!errors.rootErrors().isEmpty()) {
            add(prefix, errors.rootErrors());
        }
        for (Map.Entry<String, List<String>> entry : errors.fieldErrors().entrySet()) {
            add(prefix + "." + entry.getKey(), entry.getValue());
        }
        return this;
    }

    public ValidationErrors finish() {
        return new ValidationErrors(rootErrors, fieldErrors);
    }

    public <T extends @Nullable Object> Result<T> asResult(Supplier<T> supplier) {
        ValidationErrors errors = finish();
        if (errors.isNotEmpty()) {
            return Result.err(errors);
        }
        return Result.of(supplier);
    }

    public void check() {
        ValidationErrors errors = finish();
        if (errors.isNotEmpty()) {
            throw new ValidationException(errors);
        }
    }

    public <T extends @Nullable Object> T checkAndGet(Supplier<T> supplier) {
        check();
        return supplier.get();
    }
}
