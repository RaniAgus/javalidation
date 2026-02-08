package io.github.raniagus.javalidation.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.raniagus.javalidation.format.TemplateString;
import java.util.List;
import java.util.Map;

public abstract class ValidationErrorsMixIn {

    abstract List<TemplateString> rootErrors();

    abstract Map<String, List<TemplateString>> fieldErrors();

    @JsonIgnore
    abstract boolean isEmpty();

    @JsonIgnore
    abstract boolean isNotEmpty();
}
