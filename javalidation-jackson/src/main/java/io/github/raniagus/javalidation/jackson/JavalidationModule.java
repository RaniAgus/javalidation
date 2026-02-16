package io.github.raniagus.javalidation.jackson;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.TemplateString;
import io.github.raniagus.javalidation.format.BracketNotationFormatter;
import io.github.raniagus.javalidation.format.DotNotationFormatter;
import io.github.raniagus.javalidation.format.FieldKeyFormatter;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * Jackson module for serializing javalidation types ({@link Result}, {@link TemplateString}, and {@link ValidationErrors}).
 * <p>
 * This module registers custom serializers to control how validation types are serialized to JSON.
 * By default, it uses:
 * <ul>
 *   <li>{@link ResultMixIn} for {@link Result} - discriminated union with {@code ok} string discriminator field</li>
 *   <li>{@link TemplateStringSerializer} for {@link TemplateString} - formats templates using {@link TemplateStringFormatter}</li>
 *   <li>{@link ValidationErrorsMixIn} for {@link ValidationErrors} - structures errors as {@code {root: [...], fields: {...}}}</li>
 * </ul>
 * <p>
 * <b>Basic usage (default configuration):</b>
 * <pre>{@code
 * ObjectMapper mapper = JsonMapper.builder()
 *     .addModule(JavalidationModule.getDefault())
 *     .build();
 * }</pre>
 * <p>
 * <b>Custom formatter (for internationalization):</b>
 * <pre>{@code
 * JavalidationModule module = JavalidationModule.builder()
 *     .withTemplateStringFormatter(myCustomFormatter)
 *     .build();
 * }</pre>
 * <p>
 * <b>Flattened errors (flat array instead of nested structure):</b>
 * <pre>{@code
 * JavalidationModule module = JavalidationModule.builder()
 *     .withFlattenedErrors()
 *     .build();
 * }</pre>
 *
 * @see TemplateStringSerializer
 * @see FlattenedErrorsSerializer
 * @see ValidationErrorsMixIn
 */
public class JavalidationModule extends SimpleModule {
    private final boolean useDefaultResultSerializer;
    private final boolean useDefaultErrorSerializer;

    private JavalidationModule(
            ValueSerializer<FieldKey> fieldKeySerializer,
            @Nullable ValueSerializer<Result<?>> resultSerializer,
            ValueSerializer<TemplateString> templateStringSerializer,
            @Nullable ValueSerializer<ValidationErrors> validationErrorsSerializer
    ) {
        super(JavalidationModule.class.getSimpleName());

        addKeySerializer(FieldKey.class, fieldKeySerializer);

        if (resultSerializer != null) {
            addSerializer(resultSerializer);
            this.useDefaultResultSerializer = false;
        } else {
            this.useDefaultResultSerializer = true;
        }

        addSerializer(TemplateString.class, templateStringSerializer);

        if (validationErrorsSerializer != null) {
            addSerializer(ValidationErrors.class, validationErrorsSerializer);
            this.useDefaultErrorSerializer = false;
        } else {
            this.useDefaultErrorSerializer = true;
        }
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        if (useDefaultResultSerializer) {
            context.setMixIn(Result.class, ResultMixIn.class);
            context.setMixIn(Result.Ok.class, ResultMixIn.OkMixin.class);
            context.setMixIn(Result.Err.class, ResultMixIn.ErrMixin.class);
        }
        if (useDefaultErrorSerializer) {
            context.setMixIn(ValidationErrors.class, ValidationErrorsMixIn.class);
        }
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
     * Builder for configuring {@link JavalidationModule} with custom serializers.
     * <p>
     * Use this builder to customize how {@link TemplateString} and {@link ValidationErrors}
     * are serialized to JSON.
     *
     * @see #withTemplateStringFormatter(TemplateStringFormatter)
     * @see #withFlattenedErrors()
     */
    public static class Builder {
        private ValueSerializer<FieldKey> fieldKeySerializer = new FieldKeySerializer();
        private @Nullable ValueSerializer<Result<?>> resultSerializer;
        private ValueSerializer<TemplateString> templateStringSerializer = new TemplateStringSerializer();
        private @Nullable ValueSerializer<ValidationErrors> validationErrorsSerializer;

        /**
         * Creates a new builder with default serializers.
         */
        public Builder() {
        }

        /**
         * Configures the module to serialize {@link FieldKey} using dot notation.
         * <p>
         * By default, {@link FieldKey} is serialized using square brackets for numbers, and dots for strings:
         * {@code users[0].name}. This method changes the serialization to dot notation: {@code users.0.name}.
         *
         * @return this builder for method chaining
         * @see DotNotationFormatter
         */
        public Builder withDotNotation() {
            return withFieldKeyFormatter(new DotNotationFormatter());
        }

        /**
         * Configures the module to serialize {@link FieldKey} using bracket notation.
         * <p>
         * By default, {@link FieldKey} is serialized using square brackets for numbers, and dots for strings:
         * {@code users[0].name}. This method changes the serialization to bracket notation: {@code users[0][name]}.
         *
         * @return this builder for method chaining
         * @see BracketNotationFormatter
         */
        public Builder withBracketNotation() {
            return withFieldKeyFormatter(new BracketNotationFormatter());
        }

        /**
         * Configures the module to serialize {@link ValidationErrors} as a flat array of error strings.
         * <p>
         * By default, errors are serialized with nested structure: {@code {root: [...], fields: {...}}}.
         * This method changes the serialization to a simple flat array: {@code ["error1", "error2", ...]}.
         * <p>
         * Example output:
         * <pre>{@code
         * ["Name is required", "Age must be positive"]
         * }</pre>
         *
         * @return this builder for method chaining
         * @see FlattenedErrorsSerializer
         */
        public Builder withFlattenedErrors() {
            return withValidationErrorsSerializer(new FlattenedErrorsSerializer());
        }

        /**
         * Configures a custom formatter for {@link FieldKey} serialization.
         * <p>
         * This is a convenience method that wraps the formatter in a {@link FieldKeySerializer}.
         * Use this to customize how field keys are formatted in error messages (e.g., using dots or brackets).
         * <p>
         * Example:
         * <pre>{@code
         * builder.withTemplateStringFormatter(new MessageSourceTemplateStringFormatter(messageSource, locale))
         * }</pre>
         *
         * @param formatter the formatter to use (must not be null)
         * @return this builder for method chaining
         * @see #withFieldKeyFormatter(FieldKeyFormatter)
         */
        public Builder withFieldKeyFormatter(FieldKeyFormatter formatter) {
            return withFieldKeySerializer(new FieldKeySerializer(formatter));
        }

        /**
         * Configures a custom formatter for {@link TemplateString} serialization.
         * <p>
         * This is a convenience method that wraps the formatter in a {@link TemplateStringSerializer}.
         * Use this to customize how template messages are formatted (e.g., for i18n).
         * <p>
         * Example:
         * <pre>{@code
         * builder.withTemplateStringFormatter(new MessageSourceTemplateStringFormatter(messageSource, locale))
         * }</pre>
         *
         * @param formatter the formatter to use (must not be null)
         * @return this builder for method chaining
         * @see #withTemplateStringSerializer(ValueSerializer)
         */
        public Builder withTemplateStringFormatter(TemplateStringFormatter formatter) {
            return withTemplateStringSerializer(new TemplateStringSerializer(formatter));
        }

        /**
         * Configures a custom serializer for {@link FieldKey}.
         * <p>
         * Use this for full control over error structure serialization. For most use cases,
         * {@link #withFieldKeyFormatter(FieldKeyFormatter)} is simpler.
         *
         * @param fieldKeySerializer the custom serializer (must not be null)
         * @return this builder for method chaining
         */
        public Builder withFieldKeySerializer(ValueSerializer<FieldKey> fieldKeySerializer) {
            this.fieldKeySerializer = fieldKeySerializer;
            return this;
        }

        /**
         * Configures a custom serializer for {@link Result}.
         * <p>
         * Use this for full control over error structure serialization.
         *
         * @param resultSerializer the custom serializer (must not be null)
         * @return this builder for method chaining
         */
        public Builder withResultSerializer(ValueSerializer<Result<?>> resultSerializer) {
            this.resultSerializer = resultSerializer;
            return this;
        }

        /**
         * Configures a custom serializer for {@link TemplateString}.
         * <p>
         * Use this for full control over template serialization. For most use cases,
         * {@link #withTemplateStringFormatter(TemplateStringFormatter)} is simpler.
         *
         * @param templateStringSerializer the custom serializer (must not be null)
         * @return this builder for method chaining
         */
        public Builder withTemplateStringSerializer(ValueSerializer<TemplateString> templateStringSerializer) {
            this.templateStringSerializer = templateStringSerializer;
            return this;
        }

        /**
         * Configures a custom serializer for {@link ValidationErrors}.
         * <p>
         * Use this for full control over error structure serialization. For flattened output,
         * {@link #withFlattenedErrors()} is simpler.
         *
         * @param validationErrorsSerializer the custom serializer (must not be null)
         * @return this builder for method chaining
         */
        public Builder withValidationErrorsSerializer(ValueSerializer<ValidationErrors> validationErrorsSerializer) {
            this.validationErrorsSerializer = validationErrorsSerializer;
            return this;
        }

        /**
         * Builds the configured {@link JavalidationModule}.
         *
         * @return a new module instance with the configured serializers
         */
        public JavalidationModule build() {
            return new JavalidationModule(
                    fieldKeySerializer,
                    resultSerializer,
                    templateStringSerializer,
                    validationErrorsSerializer
            );
        }
    }
}
