package io.github.raniagus.javalidation.format;

@FunctionalInterface
public interface TemplateStringFormatter {
    String format(TemplateString templateString);

    static TemplateStringFormatter getDefault() {
        return new MessageFormatTemplateStringFormatter();
    }
}
