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

    public @Nullable TemporalKind getTemporalKind() {
        if (isOfType("java.time.Instant")) return TemporalKind.INSTANT;
        if (isOfType("java.time.LocalDate")) return TemporalKind.LOCAL_DATE;
        if (isOfType("java.time.LocalTime")) return TemporalKind.LOCAL_TIME;
        if (isOfType("java.time.LocalDateTime")) return TemporalKind.LOCAL_DATE_TIME;
        if (isOfType("java.time.OffsetDateTime")) return TemporalKind.OFFSET_DATE_TIME;
        if (isOfType("java.time.OffsetTime")) return TemporalKind.OFFSET_TIME;
        if (isOfType("java.time.ZonedDateTime")) return TemporalKind.ZONED_DATE_TIME;
        if (isOfType("java.time.Year")) return TemporalKind.YEAR;
        if (isOfType("java.time.YearMonth")) return TemporalKind.YEAR_MONTH;
        if (isOfType("java.time.MonthDay")) return TemporalKind.MONTH_DAY;
        if (isOfType("java.util.Date")) return TemporalKind.DATE;
        if (isOfType("java.util.Calendar")) return TemporalKind.CALENDAR;
        if (isOfType("java.time.chrono.HijrahDate")) return TemporalKind.HIJRAH_DATE;
        if (isOfType("java.time.chrono.JapaneseDate")) return TemporalKind.JAPANESE_DATE;
        if (isOfType("java.time.chrono.MinguoDate")) return TemporalKind.MINGUO_DATE;
        if (isOfType("java.time.chrono.ThaiBuddhistDate")) return TemporalKind.THAI_BUDDHIST_DATE;
        if (isOfType("java.lang.Long") || type.getKind() == TypeKind.LONG) return TemporalKind.LONG;
        if (isOfType("java.lang.Integer") || type.getKind() == TypeKind.INT) return TemporalKind.INTEGER;
        if (isOfType("java.lang.Short") || type.getKind() == TypeKind.SHORT) return TemporalKind.SHORT;
        if (isOfType("java.lang.Byte") || type.getKind() == TypeKind.BYTE) return TemporalKind.BYTE;
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
