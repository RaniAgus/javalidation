package io.github.raniagus.javalidation.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.raniagus.javalidation.format.TemplateString;
import java.util.List;
import java.util.Map;

public interface ValidationErrorsMixIn {

    List<TemplateString> rootErrors();

    Map<String, List<TemplateString>> fieldErrors();

    @JsonIgnore
    boolean isEmpty();

    @JsonIgnore
    boolean isNotEmpty();
}
