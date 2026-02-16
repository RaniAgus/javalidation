package io.github.raniagus.javalidation.validator;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.format.FieldKeyFormatter;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.executable.ExecutableValidator;
import jakarta.validation.metadata.BeanDescriptor;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

public class JakartaValidatorAdapter implements Validator {
    private final FieldKeyFormatter fieldKeyFormatter;
    private final TemplateStringFormatter templateStringFormatter;

    public JakartaValidatorAdapter(FieldKeyFormatter fieldKeyFormatter, TemplateStringFormatter templateStringFormatter) {
        this.fieldKeyFormatter = fieldKeyFormatter;
        this.templateStringFormatter = templateStringFormatter;
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validate(@Nullable T object, Class<?>... groups) {
        if (object == null) {
            return Set.of();
        }
        return toConstraintViolations(object, Validators.validate(object));
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
        throw new UnsupportedOperationException(
                "validateProperty is not supported by generated validators"
        );
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
        throw new UnsupportedOperationException(
                "validateValue is not supported by generated validators"
        );
    }

    @Override
    public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
        throw new UnsupportedOperationException("Constraint metadata not available");
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        if (type.isAssignableFrom(getClass())) {
            return type.cast(this);
        }
        throw new ValidationException("Cannot unwrap to " + type);
    }

    @Override
    public ExecutableValidator forExecutables() {
        throw new UnsupportedOperationException(
                "Executable validation not supported"
        );
    }

    public <T> Set<ConstraintViolation<T>> toConstraintViolations(T root, ValidationErrors errors) {
        return Stream.concat(
                errors.rootErrors().stream()
                        .map(templateString -> Map.entry(FieldKey.of(), templateString)),
                errors.fieldErrors().entrySet().stream()
                        .flatMap(entry -> entry.getValue().stream()
                                .map(templateString -> Map.entry(entry.getKey(), templateString))))
                .map(entry -> new SimpleConstraintViolation<>(
                        new SimplePath(entry.getKey(), fieldKeyFormatter.format(entry.getKey())),
                        templateStringFormatter.format(entry.getValue()),
                        entry.getValue(),
                        root
                ))
                .collect(Collectors.toSet());
    }
}
