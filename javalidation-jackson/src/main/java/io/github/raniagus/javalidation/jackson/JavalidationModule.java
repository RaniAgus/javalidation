package io.github.raniagus.javalidation.jackson;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.TemplateString;
import io.github.raniagus.javalidation.format.BracketNotationFormatter;
import io.github.raniagus.javalidation.format.DotNotationFormatter;
import io.github.raniagus.javalidation.format.FieldKeyFormatter;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.deser.Deserializers;
import tools.jackson.databind.module.SimpleModule;

/**
 * Jackson module for serializing javalidation types.
 * <p>
 * By default, uses structured format with code/args preservation for {@link Result},
 * and formats {@link TemplateString} using {@link TemplateStringFormatter}.
 * <p>
 * <b>Basic usage:</b>
 * <pre>{@code
 * ObjectMapper mapper = JsonMapper.builder()
 *     .addModule(JavalidationModule.getDefault())
 *     .build();
 * }</pre>
 * <p>
 * <b>Custom formatter for i18n:</b>
 * <pre>{@code
 * JavalidationModule module = JavalidationModule.builder()
 *     .withTemplateStringFormatter(myFormatter)
 *     .build();
 * }</pre>
 * <p>
 * <b>Flattened errors:</b>
 * <pre>{@code
 * JavalidationModule module = JavalidationModule.builder()
 *     .withFlattenedErrors()
 *     .build();
 * }</pre>
 *
 * @see StructuredResultSerializer
 * @see StructuredResultDeserializer
 */
public class JavalidationModule extends SimpleModule {
    private final Deserializers resultDeserializer;
    private final boolean useDefaultErrorSerializer;

    private JavalidationModule(
            ValueSerializer<FieldKey> fieldKeySerializer,
            ValueSerializer<TemplateString> templateStringSerializer,
            @Nullable ValueSerializer<ValidationErrors> validationErrorsSerializer,
            ValueSerializer<Result<?>> resultSerializer,
            Function<JavaType, ValueDeserializer<Result<?>>> resultDeserializerFactory
    ) {
        super(JavalidationModule.class.getSimpleName());

        addKeySerializer(FieldKey.class, fieldKeySerializer);
        addSerializer(TemplateString.class, templateStringSerializer);

        if (validationErrorsSerializer != null) {
            addSerializer(ValidationErrors.class, validationErrorsSerializer);
            this.useDefaultErrorSerializer = false;
        } else {
            this.useDefaultErrorSerializer = true;
        }

        addSerializer(resultSerializer);
        this.resultDeserializer = new StructuredResultDeserializerResolver(resultDeserializerFactory);
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        if (useDefaultErrorSerializer) {
            context.setMixIn(ValidationErrors.class, ValidationErrorsMixIn.class);
        }
        context.addDeserializers(resultDeserializer);
    }

    /**
     * Returns a module instance with default serializers.
     * <p>
     * Equivalent to {@code JavalidationModule.builder().build()}.
     *
     * @return a new module with default configuration
     */
    public static JavalidationModule getDefault() {
        return builder().build();
    }

    /**
     * Returns a new builder for customizing the module configuration.
     *
     * @return a new {@link Builder} instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for configuring {@link JavalidationModule}.
     *
     * @see #withTemplateStringFormatter(TemplateStringFormatter)
     * @see #withFlattenedErrors()
     */
    public static class Builder {
        private ValueSerializer<FieldKey> fieldKeySerializer = new FieldKeySerializer();
        private ValueSerializer<TemplateString> templateStringSerializer = new TemplateStringSerializer();
        private @Nullable ValueSerializer<ValidationErrors> validationErrorsSerializer;
        private ValueSerializer<Result<?>> resultSerializer = new StructuredResultSerializer();
        private Function<JavaType, ValueDeserializer<Result<?>>> resultDeserializerFactory = StructuredResultDeserializer::new;

        /**
         * Creates a new builder with default serializers.
         */
        public Builder() {
        }

        /**
         * Configures dot notation for {@link FieldKey}.
         * <p>
         * Changes {@code users[0].name} to {@code users.0.name}.
         *
         * @return this builder
         */
        public Builder withDotNotation() {
            return withFieldKeyFormatter(new DotNotationFormatter());
        }

        /**
         * Configures bracket notation for {@link FieldKey}.
         * <p>
         * Changes {@code users[0].name} to {@code users[0][name]}.
         *
         * @return this builder
         */
        public Builder withBracketNotation() {
            return withFieldKeyFormatter(new BracketNotationFormatter());
        }

        /**
         * Configures flat error serialization.
         * <p>
         * Changes {@code {rootErrors: [...], fieldErrors: {...}}} to {@code {"": [...], "field": [...]}}.
         *
         * @return this builder
         */
        public Builder withFlattenedErrors() {
            return withValidationErrorsSerializer(new FlattenedErrorsSerializer());
        }

        /**
         * Configures custom {@link FieldKey} formatter.
         *
         * @param formatter the formatter
         * @return this builder
         */
        public Builder withFieldKeyFormatter(FieldKeyFormatter formatter) {
            return withFieldKeySerializer(new FieldKeySerializer(formatter));
        }

        /**
         * Configures custom {@link TemplateString} formatter for i18n.
         *
         * @param formatter the formatter
         * @return this builder
         */
        public Builder withTemplateStringFormatter(TemplateStringFormatter formatter) {
            withTemplateStringSerializer(new TemplateStringSerializer(formatter));
            withResultSerializer(new StructuredResultSerializer(formatter));
            return this;
        }

        /**
         * Configures custom {@link FieldKey} serializer.
         *
         * @param fieldKeySerializer the serializer
         * @return this builder
         */
        public Builder withFieldKeySerializer(ValueSerializer<FieldKey> fieldKeySerializer) {
            this.fieldKeySerializer = fieldKeySerializer;
            return this;
        }

        /**
         * Configures custom {@link TemplateString} serializer.
         *
         * @param templateStringSerializer the serializer
         * @return this builder
         */
        public Builder withTemplateStringSerializer(ValueSerializer<TemplateString> templateStringSerializer) {
            this.templateStringSerializer = templateStringSerializer;
            return this;
        }

        /**
         * Configures custom {@link ValidationErrors} serializer.
         *
         * @param validationErrorsSerializer the serializer
         * @return this builder
         */
        public Builder withValidationErrorsSerializer(ValueSerializer<ValidationErrors> validationErrorsSerializer) {
            this.validationErrorsSerializer = validationErrorsSerializer;
            return this;
        }

        /**
         * Configures custom {@link Result} serializer.
         * <p>
         * <b>WARNING:</b> Must also configure matching deserializer factory via
         * {@link #withResultDeserializerFactory(Function)} for round-trip serialization.
         *
         * @param resultSerializer the serializer
         * @return this builder
         * @see #withResultDeserializerFactory(Function)
         * @see #withTemplateStringFormatter(TemplateStringFormatter)
         */
        public Builder withResultSerializer(ValueSerializer<Result<?>> resultSerializer) {
            this.resultSerializer = resultSerializer;
            return this;
        }

        /**
         * Configures custom {@link Result} deserializer factory.
         * <p>
         * Factory receives {@link JavaType} (e.g., {@code Person} from {@code Result<Person>})
         * and returns a deserializer.
         * <p>
         * <b>WARNING:</b> Must also configure matching serializer via
         * {@link #withResultSerializer(ValueSerializer)} for round-trip serialization.
         *
         * @param resultDeserializerFactory the factory function
         * @return this builder
         * @see #withResultSerializer(ValueSerializer)
         * @see #withTemplateStringFormatter(TemplateStringFormatter)
         */
        public Builder withResultDeserializerFactory(Function<JavaType, ValueDeserializer<Result<?>>> resultDeserializerFactory) {
            this.resultDeserializerFactory = resultDeserializerFactory;
            return this;
        }

        /**
         * Builds the configured module.
         *
         * @return a new module instance
         */
        public JavalidationModule build() {
            return new JavalidationModule(
                    fieldKeySerializer,
                    templateStringSerializer,
                    validationErrorsSerializer,
                    resultSerializer,
                    resultDeserializerFactory
            );
        }
    }
}
