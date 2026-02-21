package io.github.raniagus.javalidation.spring;

import io.github.raniagus.javalidation.TemplateString;
import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.format.FieldKeyFormatter;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import io.github.raniagus.javalidation.validator.Validators;
import java.util.Objects;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

public class JavalidationSpringValidator implements Validator {
    private final FieldKeyFormatter fieldKeyFormatter;
    private final TemplateStringFormatter templateStringFormatter;

    public JavalidationSpringValidator(FieldKeyFormatter fieldKeyFormatter, TemplateStringFormatter templateStringFormatter) {
        this.fieldKeyFormatter = fieldKeyFormatter;
        this.templateStringFormatter = templateStringFormatter;
    }

    public JavalidationSpringValidator() {
        this(FieldKeyFormatter.getDefault(), TemplateStringFormatter.getDefault());
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Validators.hasValidator(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationErrors validationErrors = Validators.validate(target);
        toErrors(validationErrors, errors);
    }

    @Override
    public Errors validateObject(Object target) {
        return Validator.super.validateObject(target);
    }

    public void toErrors(ValidationErrors validationErrors, Errors errors) {
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

    public static ValidationErrors toValidationErrors(Errors errors) {
        Validation validation = Validation.create();
        for (ObjectError error : errors.getGlobalErrors()) {
            validation.addError(Objects.toString(error.getDefaultMessage()));
        }
        for (FieldError error : errors.getFieldErrors()) {
            validation.addErrorAt(error.getField(), Objects.toString(error.getDefaultMessage()));
        }
        return validation.finish();
    }
}
