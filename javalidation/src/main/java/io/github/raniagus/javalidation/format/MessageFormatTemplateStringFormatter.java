package io.github.raniagus.javalidation.format;

import io.github.raniagus.javalidation.TemplateString;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class MessageFormatTemplateStringFormatter implements TemplateStringFormatter {
    private static final ThreadLocal<Map<String, MessageFormat>> CACHE =
        ThreadLocal.withInitial(HashMap::new);

    @Override
    public String format(TemplateString template) {
        MessageFormat mf = CACHE.get()
            .computeIfAbsent(template.message(), MessageFormat::new);
        return mf.format(template.args());
    }
}
