package io.github.raniagus.javalidation.validator;

import io.github.raniagus.javalidation.FieldKey;
import jakarta.validation.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SimplePath implements Path {
    private final List<Node> nodes;
    private final String formatted;

    public SimplePath(FieldKey key, String formatted) {
        this.nodes = Arrays.stream(key.parts())
                .<Node>map(SimpleNode::new)
                .toList();
        this.formatted = formatted;
    }

    @Override
    public Iterator<Node> iterator() {
        return nodes.iterator();
    }

    @Override
    public String toString() {
        return formatted;
    }
}
