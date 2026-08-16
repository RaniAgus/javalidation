package test.jakarta;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = {})
@ComposedAnnotation
@Size(min = 2, max = 50)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DoubleComposedAnnotation {
    String message() default "test.jakarta.DoubleComposedAnnotation.message";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
