package io.github.raniagus.javalidation.jackson;

import static tools.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.ValidationErrors;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Jackson serializer for {@link Result} types.
 * <p>
 * Serializes {@link Result} as a discriminated union with an {@code "ok"} boolean field:
 * <ul>
 *   <li>{@link Result.Ok} is serialized as {@code {"ok": true, "value": ...}}</li>
 *   <li>{@link Result.Err} is serialized as {@code {"ok": false, "errors": ...}}</li>
 * </ul>
 * <p>
 * The {@code value} field is serialized using the configured Jackson serializer for type {@code T}.
 * The {@code errors} field is serialized using the configured {@link ValidationErrors} serializer.
 * <p>
 * Example output:
 * <pre>{@code
 * // Result.ok("hello")
 * {"ok": true, "value": "hello"}
 *
 * // Result.err("Invalid input")
 * {"ok": false, "errors": {"rootErrors": ["Invalid input"], "fieldErrors": {}}}
 * }</pre>
 *
 * @see Result
 * @see JavalidationModule
 */
class ResultSerializer extends ValueSerializer<Result<?>> {

    @Override
    public Class<?> handledType() {
        return Result.class;
    }

    @Override
    public void serialize(Result result, JsonGenerator gen, SerializationContext context) {
        gen.writeStartObject();

        switch (result) {
            case Result.Ok<?>(Object value) -> {
                context.defaultSerializeProperty("ok", true, gen);
                context.defaultSerializeProperty("value", value, gen);
            }
            case Result.Err<?>(ValidationErrors errors) -> {
                if (context.hasSerializationFeatures(ORDER_MAP_ENTRIES_BY_KEYS.getMask())) {
                    context.defaultSerializeProperty("errors", errors, gen);
                    context.defaultSerializeProperty("ok", false, gen);
                } else {
                    context.defaultSerializeProperty("ok", false, gen);
                    context.defaultSerializeProperty("errors", errors, gen);
                }
            }
        }

        gen.writeEndObject();
    }
}
