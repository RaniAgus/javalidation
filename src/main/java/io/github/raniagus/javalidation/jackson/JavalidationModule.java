package io.github.raniagus.javalidation.jackson;

import io.github.raniagus.javalidation.format.TemplateString;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import tools.jackson.databind.module.SimpleModule;

public class JavalidationModule extends SimpleModule {
    public JavalidationModule() {
        this(TemplateStringFormatter.getDefault());
    }

    public JavalidationModule(TemplateStringFormatter formatter) {
        super(JavalidationModule.class.getSimpleName());
        addSerializer(TemplateString.class, new TemplateStringSerializer(formatter));
    }
}
