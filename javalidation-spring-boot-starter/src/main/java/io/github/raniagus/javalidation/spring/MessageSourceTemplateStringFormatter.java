package io.github.raniagus.javalidation.spring;

import io.github.raniagus.javalidation.format.TemplateString;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class MessageSourceTemplateStringFormatter implements TemplateStringFormatter {
    private final MessageSource messageSource;
    private final TemplateStringFormatter fallbackFormatter;

    public MessageSourceTemplateStringFormatter(MessageSource messageSource,
                                                TemplateStringFormatter fallbackFormatter) {
        this.messageSource = messageSource;
        this.fallbackFormatter = fallbackFormatter;
    }

    @Override
    public String format(TemplateString template) {
        String message = messageSource.getMessage(
                template.message(),
                template.args(),
                null,
                LocaleContextHolder.getLocale()
        );

        if (message == null) {
            return fallbackFormatter.format(template);
        }

        return message;
    }
}
