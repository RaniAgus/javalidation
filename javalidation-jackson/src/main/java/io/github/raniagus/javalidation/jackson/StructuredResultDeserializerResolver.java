package io.github.raniagus.javalidation.jackson;

import io.github.raniagus.javalidation.Result;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.Deserializers;

class StructuredResultDeserializerResolver extends Deserializers.Base {
    private final Function<JavaType, ValueDeserializer<Result<?>>> deserializerFactory;

    StructuredResultDeserializerResolver(Function<JavaType, ValueDeserializer<Result<?>>> deserializerFactory) {
        this.deserializerFactory = deserializerFactory;
    }
    
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

        return deserializerFactory.apply(valueType);
    }
}
