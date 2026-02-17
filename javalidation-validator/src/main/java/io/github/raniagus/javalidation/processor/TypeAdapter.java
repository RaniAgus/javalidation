package io.github.raniagus.javalidation.processor;

import java.lang.annotation.Annotation;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.jspecify.annotations.Nullable;

public record TypeAdapter(TypeMirror type, ProcessingEnvironment processingEnv) {
    private static final Set<String> DECIMAL_TYPES = Set.of(
            "java.math.BigDecimal",
            "java.math.BigInteger",
            "java.lang.CharSequence",
            "java.lang.Number",
            "byte", "short", "int", "long"
    );

    public boolean isDecimalType() {
        Elements elements = processingEnv.getElementUtils();
        Types types = processingEnv.getTypeUtils();

        for (String typeName : DECIMAL_TYPES) {
            TypeElement targetElement = elements.getTypeElement(typeName);
            if (targetElement != null && types.isAssignable(type, targetElement.asType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get an AnnotationMirror for the specified annotation class.
     * This works for both declaration annotations and type-use annotations (TypeCompound).
     */
    public @Nullable AnnotationMirror getAnnotationMirror(Class<? extends Annotation> annotationClass) {
        String annotationName = annotationClass.getName();
        for (var annotationMirror : type.getAnnotationMirrors()) {
            var annotationType = annotationMirror.getAnnotationType();
            if (annotationType.toString().equals(annotationName)) {
                return annotationMirror;
            }
        }
        return null;
    }
}
