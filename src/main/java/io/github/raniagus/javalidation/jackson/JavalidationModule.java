package io.github.raniagus.javalidation.jackson;

import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.format.TemplateString;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

public class JavalidationModule extends SimpleModule {
    private final boolean useDefaultSerializer;

    private JavalidationModule(
            ValueSerializer<TemplateString> templateStringSerializer,
            @Nullable ValueSerializer<ValidationErrors> validationErrorsSerializer
    ) {
        super(JavalidationModule.class.getSimpleName());

        addSerializer(TemplateString.class, templateStringSerializer);

        if (validationErrorsSerializer != null) {
            addSerializer(ValidationErrors.class, validationErrorsSerializer);
            this.useDefaultSerializer = false;
        } else {
            this.useDefaultSerializer = true;
        }
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        if (useDefaultSerializer) {
            context.setMixIn(ValidationErrors.class, ValidationErrorsMixIn.class);
        }
    }

    public static JavalidationModule getDefault() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ValueSerializer<TemplateString> templateStringSerializer = new TemplateStringSerializer();
        private @Nullable ValueSerializer<ValidationErrors> validationErrorsSerializer;

        public Builder() {
        }

        public Builder withTemplateStringFormatter(TemplateStringFormatter formatter) {
            return withTemplateStringSerializer(new TemplateStringSerializer(formatter));
        }

        public Builder withFlattenedErrors() {
            return withValidationErrorsSerializer(new FlattenedErrorsSerializer());
        }

        public Builder withTemplateStringSerializer(ValueSerializer<TemplateString> templateStringSerializer) {
            this.templateStringSerializer = templateStringSerializer;
            return this;
        }

        public Builder withValidationErrorsSerializer(ValueSerializer<ValidationErrors> validationErrorsSerializer) {
            this.validationErrorsSerializer = validationErrorsSerializer;
            return this;
        }

        public JavalidationModule build() {
            return new JavalidationModule(templateStringSerializer, validationErrorsSerializer);
        }
    }
}
