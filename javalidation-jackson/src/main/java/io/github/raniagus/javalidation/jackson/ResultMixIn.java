package io.github.raniagus.javalidation.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.ValidationErrors;
import org.jspecify.annotations.Nullable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "ok"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Result.Ok.class, name = "true"),
        @JsonSubTypes.Type(value = Result.Err.class, name = "false")
})
public interface ResultMixIn {

    interface OkMixin<T extends @Nullable Object> {
        T value();

        @JsonIgnore
        ValidationErrors getErrors();

        @JsonIgnore
        T getOrThrow();
    }

    interface ErrMixin {
        ValidationErrors errors();

        @JsonIgnore
        Object getOrThrow();
    }
}
