package io.github.raniagus.javalidation.format;

import java.text.MessageFormat;

public class MessageFormatTemplateStringFormatter implements TemplateStringFormatter {
    @Override
    public String format(TemplateString template) {
        return MessageFormat.format(template.message(), template.args());
    }
}
