package io.github.raniagus.javalidation.jackson;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for serializing and deserializing {@link ValidationErrors} with full structure preservation.
 * <p>
 * This record maintains the complete structure needed to reconstruct {@link ValidationErrors},
 * including:
 * <ul>
 *   <li>Root errors with template, args, and formatted message</li>
 *   <li>Field errors with structured key parts (not formatted strings)</li>
 * </ul>
 * <p>
 * Example JSON:
 * <pre>{@code
 * {
 *   "rootErrors": [
 *     {
 *       "message": "User must be at least 18 years old",
 *       "template": "User must be at least {0} years old",
 *       "args": [18]
 *     }
 *   ],
 *   "fieldErrors": [
 *     {
 *       "key": ["user", "address", 0, "street"],
 *       "errors": [
 *         {"message": "Required field", "code": "Required field", "args": []}
 *       ]
 *     }
 *   ]
 * }
 * }</pre>
 *
 * @param rootErrors list of root-level errors
 * @param fieldErrors list of field-specific errors with structured keys
 */
record StructuredValidationErrorsDto(
        List<StructuredErrorDto> rootErrors,
        List<StructuredFieldErrorDto> fieldErrors
) {
    /**
     * Compact constructor with defensive copying.
     */
    public StructuredValidationErrorsDto {
        rootErrors = List.copyOf(rootErrors);
        fieldErrors = List.copyOf(fieldErrors);
    }

    /**
     * Creates a DTO from {@link ValidationErrors}, formatting messages with the provided formatter.
     *
     * @param errors the validation errors to convert
     * @param formatter the formatter to use for error messages
     * @return a new DTO with formatted messages and preserved structure
     */
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

    /**
     * Converts this DTO back to {@link ValidationErrors}.
     *
     * @return a new ValidationErrors instance
     */
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
