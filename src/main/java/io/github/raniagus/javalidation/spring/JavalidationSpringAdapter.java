package io.github.raniagus.javalidation.spring;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.format.TemplateString;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

public final class JavalidationSpringAdapter {

    private JavalidationSpringAdapter() {
    }

    public static ValidationErrors toValidationErrors(Errors errors) {
        List<TemplateString> rootErrors = errors.getGlobalErrors().stream()
                .map(JavalidationSpringAdapter::toTemplateString)
                .toList();

        Map<String, List<TemplateString>> fieldErrors = errors.getFieldErrors().stream()
                .collect(groupingBy(
                        FieldError::getField,
                        mapping(JavalidationSpringAdapter::toTemplateString, toList())
                ));

        return new ValidationErrors(rootErrors, fieldErrors);
    }

    public static TemplateString toTemplateString(DefaultMessageSourceResolvable resolvable) {
        return new TemplateString(
                Objects.requireNonNull(resolvable.getDefaultMessage()),
                Objects.requireNonNullElseGet(resolvable.getArguments(), () -> new Object[0])
        );
    }
}
