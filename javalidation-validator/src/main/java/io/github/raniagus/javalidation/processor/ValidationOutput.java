package io.github.raniagus.javalidation.processor;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.format.FieldKeyFormatter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ValidationOutput {
    private static final int INDENT_SIZE = 4;

    private final Writer writer;
    private final FieldKeyFormatter fieldKeyFormatter;
    private final List<String> variableNames = new ArrayList<>();
    private final List<Object> keyPrefix = new ArrayList<>();

    private int indentLevel = 0;
    private String indent = "";


    public ValidationOutput(Writer writer, FieldKeyFormatter fieldKeyFormatter) {
        this.writer = writer;
        this.fieldKeyFormatter = fieldKeyFormatter;
    }

    public void write(String... lines) {
        try {
            for (var line : lines) {
                writer.write(indent);
                writer.write(line);
                writer.write('\n');
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void incrementIndentationLevel() {
        indentLevel++;
        indent = " ".repeat(INDENT_SIZE * indentLevel);
    }

    public void decrementIndentationLevel() {
        indentLevel--;
        indent = " ".repeat(INDENT_SIZE * indentLevel);
    }

    public void createVariable() {
        variableNames.add("obj" + variableNames.size());
    }

    public String getVariable() {
        return variableNames.getLast();
    }

    public void removeVariable() {
        variableNames.removeLast();
    }

    public void addKeyPrefix(Object key) {
        keyPrefix.add(key);
    }

    public String getFullKey(String key) {
        Object[] fullKey = Arrays.copyOf(keyPrefix.toArray(), keyPrefix.size() + 1);
        fullKey[keyPrefix.size()] = key;
        return fieldKeyFormatter.format(FieldKey.of(fullKey));
    }
}
