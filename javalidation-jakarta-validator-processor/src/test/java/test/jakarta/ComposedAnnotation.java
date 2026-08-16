package test.jakarta;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = {})
@NotBlank
@Pattern(regexp = "^[a-zA-Z]+$")
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ComposedAnnotation {
    String message() default "test.jakarta.ComposedAnnotation.message";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
