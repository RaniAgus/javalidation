package io.github.raniagus.javalidation.validator;

import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import org.jspecify.annotations.Nullable;

public class SimpleNode implements Path.Node {
    private final Object part;

    public SimpleNode(Object part) {
        this.part = part;
    }

    @Override
    public @Nullable String getName() {
        return part instanceof String p ? p : null;
    }

    @Override
    public boolean isInIterable() {
        return part instanceof Integer;
    }

    @Override
    public @Nullable Integer getIndex() {
        return part instanceof Integer p ? p : null;
    }

    @Override
    public @Nullable Object getKey() {
        return null;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.PROPERTY;
    }

    @Override
    public <T extends Path.Node> T as(Class<T> nodeType) {
        throw new IllegalArgumentException();
    }
}
