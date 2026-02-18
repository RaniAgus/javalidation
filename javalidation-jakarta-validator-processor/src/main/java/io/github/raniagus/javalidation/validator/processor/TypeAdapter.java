package io.github.raniagus.javalidation.validator.processor;

import java.lang.annotation.Annotation;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.jspecify.annotations.Nullable;

public record TypeAdapter(TypeMirror type, ProcessingEnvironment processingEnv) {
    /**
     * Check if the type is a decimal type (BigDecimal, BigInteger, CharSequence, Number, or primitive numeric types).
     */
    public @Nullable NumericKind getNumericKind() {
        if (isOfType("java.math.BigDecimal")) return NumericKind.BIG_DECIMAL;
        if (isOfType("java.math.BigInteger")) return NumericKind.BIG_INTEGER;
        if (isOfType("java.lang.Byte") || type.getKind() == TypeKind.BYTE) return NumericKind.BYTE;
        if (isOfType("java.lang.Short") || type.getKind() == TypeKind.SHORT) return NumericKind.SHORT;
        if (isOfType("java.lang.Integer") || type.getKind() == TypeKind.INT) return NumericKind.INTEGER;
        if (isOfType("java.lang.Long") || type.getKind() == TypeKind.LONG) return NumericKind.LONG;
        if (isOfType("java.lang.Number")) return NumericKind.NUMBER;
        if (isOfType("java.lang.CharSequence")) return NumericKind.CHAR_SEQUENCE;
        return null;
    }

    /**
     * Check if the type is a collection type
     */
    public boolean isCollection() {
        return isOfType("java.util.Collection");
    }

    /**
     * Check if the type is a map type
     */
    public boolean isOfType(String typeName) {
        Elements elements = processingEnv.getElementUtils();
        Types types = processingEnv.getTypeUtils();

        TypeElement collectionElement = elements.getTypeElement(typeName);
        if (collectionElement == null) return false;

        return types.isAssignable(types.erasure(type), types.erasure(collectionElement.asType()));
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
