package io.github.raniagus.javalidation.spring;

import io.github.raniagus.javalidation.TemplateString;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.format.FieldKeyFormatter;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import io.github.raniagus.javalidation.validator.Validators;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class JavalidationSpringValidator implements Validator {
    private final FieldKeyFormatter fieldKeyFormatter;
    private final TemplateStringFormatter templateStringFormatter;

    public JavalidationSpringValidator(FieldKeyFormatter fieldKeyFormatter, TemplateStringFormatter templateStringFormatter) {
        this.fieldKeyFormatter = fieldKeyFormatter;
        this.templateStringFormatter = templateStringFormatter;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Validators.hasValidator(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationErrors validationErrors = Validators.validate(target);
        for (TemplateString rootError : validationErrors.rootErrors()) {
            errors.reject("", templateStringFormatter.format(rootError));
        }
        for (var entry : validationErrors.fieldErrors().entrySet()) {
            String field = fieldKeyFormatter.format(entry.getKey());
            for (TemplateString error : entry.getValue()) {
                errors.rejectValue(field, "", templateStringFormatter.format(error));
            }
        }
    }

    @Override
    public Errors validateObject(Object target) {
        return Validator.super.validateObject(target);
    }
}
