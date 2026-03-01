package io.github.raniagus.javalidation.jackson;

import io.github.raniagus.javalidation.Result;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Deserializer for {@link Result} that reconstructs instances from structured JSON.
 * <p>
 * This deserializer handles the structured format produced by {@link StructuredResultSerializer},
 * reconstructing {@link Result.Ok} or {@link Result.Err} based on the {@code ok} field.
 * <p>
 * The deserializer supports generic types through Jackson's type resolution system, enabling
 * type-safe deserialization like {@code Result<Person>} or {@code Result<List<Item>>}.
 * <p>
 * Expected JSON format:
 * <pre>{@code
 * // Ok result
 * {"ok": true, "value": "hello"}
 *
 * // Err result
 * {
 *   "ok": false,
 *   "errors": {
 *     "rootErrors": [...],
 *     "fieldErrors": [...]
 *   }
 * }
 * }</pre>
 *
 * @see StructuredResultSerializer
 * @see StructuredValidationErrorsDto
 */
public class StructuredResultDeserializer extends StdDeserializer<Result<@Nullable Object>> {
    private final JavaType valueType;

    /**
     * Creates a deserializer for the given value type.
     *
     * @param valueType the type of the success value
     */
    public StructuredResultDeserializer(JavaType valueType) {
        super(valueType);
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
