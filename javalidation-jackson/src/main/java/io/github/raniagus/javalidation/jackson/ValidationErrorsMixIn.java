package io.github.raniagus.javalidation.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.raniagus.javalidation.TemplateString;
import java.util.List;
import java.util.Map;

public interface ValidationErrorsMixIn {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<TemplateString> rootErrors();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Map<String, List<TemplateString>> fieldErrors();

    @JsonIgnore
    boolean isEmpty();

    @JsonIgnore
    boolean isNotEmpty();
}
