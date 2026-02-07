package io.github.raniagus.javalidation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import io.github.raniagus.javalidation.format.TemplateString;

public class Validation {
    private final List<TemplateString> rootErrors = new ArrayList<>();
    private final Map<String, List<TemplateString>> fieldErrors = new HashMap<>();

    private Validation() {}

    public Validation addRootError(String message, Object... args) {
        Objects.requireNonNull(message);
        rootErrors.add(new TemplateString(message, args));
        return this;
    }

    private void addRootErrors(List<TemplateString> messages) {
        Objects.requireNonNull(messages);
        rootErrors.addAll(messages);
    }

    public Validation addFieldError(String field, String message, Object... args) {
        Objects.requireNonNull(field);
        Objects.requireNonNull(message);
        fieldErrors.computeIfAbsent(field, k -> new ArrayList<>(1))
                .add(new TemplateString(message, args));
        return this;
    }

    private void addFieldErrors(String field, List<TemplateString> messages) {
        Objects.requireNonNull(field);
        Objects.requireNonNull(messages);
        if (!messages.isEmpty()) {
            fieldErrors.computeIfAbsent(field, k -> new ArrayList<>(messages.size()))
                    .addAll(messages);
        }
    }

    private void addFieldErrors(Map<String, List<TemplateString>> fieldErrors) {
        Objects.requireNonNull(fieldErrors);
        fieldErrors.forEach(this::addFieldErrors);
    }

    public Validation addAll(ValidationErrors errors) {
        Objects.requireNonNull(errors);
        addRootErrors(errors.rootErrors());
        addFieldErrors(errors.fieldErrors());
        return this;
    }

    public Validation addAll(String prefix, ValidationErrors errors) {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(errors);
        if (!errors.rootErrors().isEmpty()) {
            addFieldErrors(prefix, errors.rootErrors());
        }
        StringBuilder prefixBuilder = new StringBuilder(prefix).append(".");
        int prefixLength = prefixBuilder.length();
        for (Map.Entry<String, List<TemplateString>> entry : errors.fieldErrors().entrySet()) {
            prefixBuilder.append(entry.getKey());
            addFieldErrors(prefixBuilder.toString(), entry.getValue());
            prefixBuilder.setLength(prefixLength);
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

    public static Validation create() {
        return new Validation();
    }
}
