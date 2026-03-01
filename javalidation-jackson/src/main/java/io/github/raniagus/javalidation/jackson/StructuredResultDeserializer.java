package io.github.raniagus.javalidation.jackson;

import io.github.raniagus.javalidation.Result;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class StructuredResultDeserializer extends ValueDeserializer<Result<?>> {
    private final JavaType valueType;

    public StructuredResultDeserializer(JavaType valueType) {
        this.valueType = valueType;
    }

    @Override
    public @Nullable Result<@Nullable Object> deserialize(JsonParser parser, DeserializationContext context) {
        JsonNode node = parser.readValueAsTree();

        // Read the "ok" discriminator field
        JsonNode okNode = node.get("ok");
        if (okNode == null) {
            context.reportInputMismatch(Result.class,
                    "Missing required 'ok' field in Result JSON");
            return null;
        }

        boolean isOk = okNode.asBoolean();

        if (isOk) {
            // Deserialize Ok result
            JsonNode valueNode = node.get("value");
            if (valueNode == null) {
                context.reportInputMismatch(Result.class,
                        "Missing required 'value' field in Result.Ok JSON");
            }

            Object value = valueType.hasRawClass(Void.class) ? null : context.readTreeAsValue(valueNode, valueType);
            return Result.ok(value);
        } else {
            // Deserialize Err result
            JsonNode errorsNode = node.get("errors");
            if (errorsNode == null) {
                context.reportInputMismatch(Result.class,
                        "Missing required 'errors' field in Result.Err JSON");
            }

            StructuredValidationErrorsDto errorsDto = context.readTreeAsValue(
                    errorsNode, StructuredValidationErrorsDto.class);

            return Result.error(errorsDto.toValidationErrors());
        }
    }
}
