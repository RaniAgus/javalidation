package io.github.raniagus.javalidation.validator;

import io.github.raniagus.javalidation.TemplateString;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.ValidationException;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.jspecify.annotations.Nullable;

public class SimpleConstraintViolation<T> implements ConstraintViolation<T> {
    private final SimplePath path;
    private final String message;
    private final TemplateString templateString;
    private final T rootBean;

    public SimpleConstraintViolation(
            SimplePath path,
            String message,
            TemplateString templateString,
            T rootBean
    ) {
        this.path = path;
        this.message = message;
        this.templateString = templateString;
        this.rootBean = rootBean;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getMessageTemplate() {
        return templateString.message();
    }

    @Override
    public T getRootBean() {
        return rootBean;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getRootBeanClass() {
        return (Class<T>) rootBean.getClass();
    }

    @Override
    public @Nullable Object getLeafBean() {
        return rootBean;
    }

    @Override
    public Object[] getExecutableParameters() {
        return new Object[0];
    }

    @Override
    public @Nullable Object getExecutableReturnValue() {
        return null;
    }

    @Override
    public Path getPropertyPath() {
        return path;
    }

    @Override
    public @Nullable Object getInvalidValue() {
        return null;
    }

    @Override
    public @Nullable ConstraintDescriptor<?> getConstraintDescriptor() {
        return null;
    }

    @Override
    public <U> @Nullable U unwrap(Class<U> type) {
        if (type.isAssignableFrom(getClass())) {
            return type.cast(this);
        }
        throw new ValidationException("Cannot unwrap to " + type);
    }
}
