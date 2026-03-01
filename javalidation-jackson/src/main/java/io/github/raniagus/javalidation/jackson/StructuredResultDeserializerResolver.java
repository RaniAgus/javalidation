package io.github.raniagus.javalidation.jackson;

import io.github.raniagus.javalidation.Result;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.Deserializers;

/**
 * Custom deserializer resolver for {@link Result} types that extracts generic type information.
 * <p>
 * This resolver is used by Jackson to find the appropriate deserializer for {@link Result} instances,
 * enabling generic type-safe deserialization like {@code Result<Person>} or {@code Result<List<Item>>}.
 *
 * @see StructuredResultDeserializer
 */
class StructuredResultDeserializerResolver extends Deserializers.Base {
    
    @Override
    public boolean hasDeserializerFor(DeserializationConfig config, Class<?> valueType) {
        return Result.class.isAssignableFrom(valueType);
    }
    
    @Override
    public @Nullable ValueDeserializer<?> findBeanDeserializer(
            JavaType type,
            DeserializationConfig config,
            BeanDescription.Supplier beanDescSupplier
    ) {
        if (!Result.class.isAssignableFrom(type.getRawClass())) {
            return null;
        }

        // Extract the value type from Result<T>
        JavaType valueType = type.containedTypeCount() > 0
                ? type.containedType(0)
                : config.constructType(Object.class);

        return new StructuredResultDeserializer(valueType);
    }
}
