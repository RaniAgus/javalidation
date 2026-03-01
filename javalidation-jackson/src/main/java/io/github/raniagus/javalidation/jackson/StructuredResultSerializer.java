package io.github.raniagus.javalidation.jackson;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Serializer for {@link Result} that preserves full structure for reconstruction.
 * <p>
 * This serializer produces JSON with:
 * <ul>
 *   <li><b>ok</b>: Boolean field indicating success (true) or failure (false)</li>
 *   <li><b>value</b>: The success value (for Ok results)</li>
 *   <li><b>errors</b>: Structured validation errors with template/args (for Err results)</li>
 * </ul>
 * <p>
 * Unlike the mixin-based approach, this serializer includes the formatted error messages
 * alongside the template and args, enabling both immediate use and full reconstruction.
 * <p>
 * Example output for Ok:
 * <pre>{@code
 * {"ok": true, "value": "hello"}
 * }</pre>
 * <p>
 * Example output for Err:
 * <pre>{@code
 * {
 *   "ok": false,
 *   "errors": {
 *     "rootErrors": [
 *       {
 *         "message": "User must be at least 18 years old",
 *         "code": "User must be at least {0} years old",
 *         "args": [18]
 *       }
 *     ],
 *     "fieldErrors": [
 *       {
 *         "key": ["email"],
 *         "errors": [
 *           {"message": "Invalid format", "code": "Invalid format", "args": []}
 *         ]
 *       }
 *     ]
 *   }
 * }
 * }</pre>
 *
 * @see StructuredResultDeserializer
 * @see StructuredValidationErrorsDto
 */
public class StructuredResultSerializer extends ValueSerializer<Result<?>> {
    private final TemplateStringFormatter templateStringFormatter;

    /**
     * Creates a serializer with a custom template string formatter.
     *
     * @param templateStringFormatter the formatter to use for error messages
     */
    public StructuredResultSerializer(TemplateStringFormatter templateStringFormatter) {
        this.templateStringFormatter = templateStringFormatter;
    }

    /**
     * Creates a serializer with the default template string formatter.
     */
    public StructuredResultSerializer() {
        this(TemplateStringFormatter.getDefault());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Result<?>> handledType() {
        return (Class<Result<?>>) (Class<?>) Result.class;
    }

    @Override
    public void serialize(Result<?> result, JsonGenerator gen, SerializationContext context) {
        gen.writeStartObject();

        switch (result) {
            case Result.Ok<?> ok -> {
                gen.writeBooleanProperty("ok", true);
                context.defaultSerializeProperty("value", ok.value(), gen);
            }
            case Result.Err<?> err -> {
                gen.writeBooleanProperty("ok", false);
                var errorsDto = StructuredValidationErrorsDto.from(err.errors(), templateStringFormatter);
                context.defaultSerializeProperty("errors", errorsDto, gen);
            }
        }

        gen.writeEndObject();
    }
}
