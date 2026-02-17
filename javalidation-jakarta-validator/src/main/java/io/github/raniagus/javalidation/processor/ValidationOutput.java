package io.github.raniagus.javalidation.processor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class ValidationOutput {
    private static final int INDENT_SIZE = 4;

    private final Writer writer;
    private final List<String> variableNames = new ArrayList<>();
    private final List<Object> keys = new ArrayList<>();

    private int indentLevel = 0;
    private String indent = "";


    public ValidationOutput(Writer writer) {
        this.writer = writer;
    }

    public void write(String line) {
        try {
            writer.write(indent);
            writer.write(line);
            writer.write('\n');
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

    public void registerVariable(String name) {
        variableNames.add(name);
    }

    public String getVariable() {
        if (variableNames.isEmpty()) {
            return "root";
        }
        return variableNames.getLast();
    }

    public void removeVariable() {
        variableNames.removeLast();
    }
}
