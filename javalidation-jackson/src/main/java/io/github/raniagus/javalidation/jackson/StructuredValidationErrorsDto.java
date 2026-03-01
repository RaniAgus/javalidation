package io.github.raniagus.javalidation.jackson;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import java.util.List;
import java.util.stream.Collectors;

record StructuredValidationErrorsDto(
        List<StructuredErrorDto> rootErrors,
        List<StructuredFieldErrorDto> fieldErrors
) {
    public StructuredValidationErrorsDto {
        rootErrors = List.copyOf(rootErrors);
        fieldErrors = List.copyOf(fieldErrors);
    }

    static StructuredValidationErrorsDto from(ValidationErrors errors, TemplateStringFormatter formatter) {
        var rootDtos = errors.rootErrors().stream()
                .map(ts -> StructuredErrorDto.from(ts, formatter))
                .toList();

        var fieldDtos = errors.fieldErrors().entrySet().stream()
                .map(entry -> new StructuredFieldErrorDto(
                        entry.getKey().parts(),
                        entry.getValue().stream()
                                .map(ts -> StructuredErrorDto.from(ts, formatter))
                                .toList()
                ))
                .toList();

        return new StructuredValidationErrorsDto(rootDtos, fieldDtos);
    }

    ValidationErrors toValidationErrors() {
        var roots = rootErrors.stream()
                .map(StructuredErrorDto::toTemplateString)
                .toList();

        var fields = fieldErrors.stream()
                .collect(Collectors.toMap(
                        fe -> FieldKey.of(fe.key()),
                        fe -> fe.errors().stream()
                                .map(StructuredErrorDto::toTemplateString)
                                .toList()
                ));

        return new ValidationErrors(roots, fields);
    }
}
