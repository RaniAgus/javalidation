package io.github.raniagus.javalidation.validator.processor;

import jakarta.validation.constraints.*;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import org.jspecify.annotations.Nullable;

public final class JakartaAnnotationParser {
    private JakartaAnnotationParser() {}

    public static @Nullable NullSafeWriter parseNullSafeWriter(TypeAdapter type) {
        return Stream.of(
                parseNullAnnotation(type),
                parseNotBlankAnnotation(type),
                parseNotEmptyAnnotation(type),
                parseNotNullAnnotation(type)
        ).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public static Stream<NullUnsafeWriter> parseNullUnsafeWriters(TypeAdapter type) {
        return Stream.of(
                parseRepeatableAnnotation(type, Size.class, Size.List.class,
                        (mirror, i) -> parseSizeAnnotationMirror(mirror, type)),
                parseRepeatableAnnotation(type, Min.class, Min.List.class,
                        (mirror, i) -> parseMinAnnotationMirror(mirror, type)),
                parseRepeatableAnnotation(type, Max.class, Max.List.class,
                        (mirror, i) -> parseMaxAnnotationMirror(mirror, type)),
                parseRepeatableAnnotation(type, Positive.class, Positive.List.class,
                        (mirror, i) -> parsePositiveAnnotationMirror(mirror, type)),
                parseRepeatableAnnotation(type, PositiveOrZero.class, PositiveOrZero.List.class,
                        (mirror, i) -> parsePositiveOrZeroAnnotationMirror(mirror, type)),
                parseRepeatableAnnotation(type, Negative.class, Negative.List.class,
                        (mirror, i) -> parseNegativeAnnotationMirror(mirror, type)),
                parseRepeatableAnnotation(type, NegativeOrZero.class, NegativeOrZero.List.class,
                        (mirror, i) -> parseNegativeOrZeroAnnotationMirror(mirror, type)),
                parseRepeatableAnnotation(type, Email.class, Email.List.class,
                        (mirror, i) -> parseEmailAnnotationMirror(mirror, type)),
                parseRepeatableAnnotation(type, Pattern.class, Pattern.List.class,
                        (mirror, i) -> parsePatternAnnotationMirror(mirror, type, i + 1)),
                parseRepeatableAnnotation(type, AssertTrue.class, AssertTrue.List.class,
                        (mirror, i) -> parseAssertTrueAnnotationMirror(mirror, type)),
                parseRepeatableAnnotation(type, AssertFalse.class, AssertFalse.List.class,
                        (mirror, i) -> parseAssertFalseAnnotationMirror(mirror, type)),
                parseRepeatableAnnotation(type, DecimalMax.class, DecimalMax.List.class,
                        (mirror, i) -> parseDecimalMaxAnnotationMirror(mirror, type)),
                parseRepeatableAnnotation(type, DecimalMin.class, DecimalMin.List.class,
                        (mirror, i) -> parseDecimalMinAnnotationMirror(mirror, type)),
                parseRepeatableAnnotation(type, Digits.class, Digits.List.class,
                        (mirror, i) -> parseDigitsAnnotationMirror(mirror, type)),
                parseRepeatableAnnotation(type, Future.class, Future.List.class,
                        (mirror, i) -> parseFutureAnnotationMirror(mirror, type)),
                parseRepeatableAnnotation(type, FutureOrPresent.class, FutureOrPresent.List.class,
                        (mirror, i) -> parseFutureOrPresentAnnotationMirror(mirror, type)),
                parseRepeatableAnnotation(type, Past.class, Past.List.class,
                        (mirror, i) -> parsePastAnnotationMirror(mirror, type)),
                parseRepeatableAnnotation(type, PastOrPresent.class, PastOrPresent.List.class,
                        (mirror, i) -> parsePastOrPresentAnnotationMirror(mirror, type))
        ).flatMap(s -> s);
    }

    private static Stream<NullUnsafeWriter> parseRepeatableAnnotation(
            TypeAdapter type,
            Class<? extends Annotation> annotationClass,
            Class<? extends Annotation> listClass,
            BiFunction<AnnotationMirror, Integer, @Nullable NullUnsafeWriter> parser) {
        var single = type.getAnnotationMirror(annotationClass);
        if (single != null) {
            return Stream.ofNullable(parser.apply(single, 0));
        }
        var listMirror = type.getElementAnnotationMirror(listClass);
        if (listMirror == null) {
            return Stream.empty();
        }
        Object value = getAnnotationValue(listMirror, "value");
        if (!(value instanceof List<?> list)) {
            return Stream.empty();
        }
        var mirrors = list.stream()
                .flatMap(obj -> obj instanceof AnnotationValue av ? Stream.of(av.getValue()) : Stream.empty())
                .flatMap(av -> av instanceof AnnotationMirror am ? Stream.of(am) : Stream.empty())
                .toList();
        return IntStream.range(0, mirrors.size())
                .mapToObj(i -> parser.apply(mirrors.get(i), i))
                .filter(Objects::nonNull);
    }

    public static @Nullable NullSafeWriter parseNotNullAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(NotNull.class);
        if (annotationMirror == null) {
            return null;
        }
        warnIfGroupsPresent(annotationMirror, type);

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.NotNull.message");
        return new NullSafeWriter.NotNull(resolveMessage(message));
    }

    public static @Nullable NullSafeWriter parseNotEmptyAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(NotEmpty.class);
        if (annotationMirror == null) {
            return null;
        }
        warnIfGroupsPresent(annotationMirror, type);

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.NotEmpty.message");
        return new NullSafeWriter.NullSafeAccessor("isEmpty", resolveMessage(message));
    }

    public static @Nullable NullSafeWriter parseNullAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(Null.class);
        if (annotationMirror == null) {
            return null;
        }
        warnIfGroupsPresent(annotationMirror, type);

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.Null.message");
        return new NullSafeWriter.Null(resolveMessage(message));
    }

    public static @Nullable NullSafeWriter parseNotBlankAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(NotBlank.class);
        if (annotationMirror == null) {
            return null;
        }
        warnIfGroupsPresent(annotationMirror, type);

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.NotBlank.message");
        return new NullSafeWriter.NullSafeAccessor("isBlank", resolveMessage(message));
    }

    private static @Nullable NullUnsafeWriter parseSizeAnnotationMirror(AnnotationMirror mirror, TypeAdapter type) {
        warnIfGroupsPresent(mirror, type);

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.Size.message");
        int min = getAnnotationIntValue(mirror, "min", 0);
        int max = getAnnotationIntValue(mirror, "max", Integer.MAX_VALUE);

        return new NullUnsafeWriter.Size(
                type.isCollection() || type.isOfType("java.util.Map") ? "size" : "length",
                resolveMessage(message, "{min}", "{max}"),
                min,
                max
        );
    }

    private static @Nullable NullUnsafeWriter parseMinAnnotationMirror(AnnotationMirror mirror, TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }
        warnIfGroupsPresent(mirror, type);

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.Min.message");
        long value = getAnnotationLongValue(mirror, "value", 0);

        return new NullUnsafeWriter.NumericCompare(
                ">=",
                value,
                numericKind,
                resolveMessage(message, "{value}"),
                true
        );
    }

    private static @Nullable NullUnsafeWriter parseMaxAnnotationMirror(AnnotationMirror mirror, TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }
        warnIfGroupsPresent(mirror, type);

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.Max.message");
        long value = getAnnotationLongValue(mirror, "value", 0);

        return new NullUnsafeWriter.NumericCompare(
                "<=",
                value,
                numericKind,
                resolveMessage(message, "{value}"),
                true
        );
    }

    private static @Nullable NullUnsafeWriter parsePositiveAnnotationMirror(AnnotationMirror mirror, TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }
        warnIfGroupsPresent(mirror, type);

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.Positive.message");
        return new NullUnsafeWriter.NumericCompare(
                ">",
                0,
                numericKind,
                resolveMessage(message),
                false
        );
    }

    private static @Nullable NullUnsafeWriter parsePositiveOrZeroAnnotationMirror(AnnotationMirror mirror, TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }
        warnIfGroupsPresent(mirror, type);

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.PositiveOrZero.message");
        return new NullUnsafeWriter.NumericCompare(
                ">=",
                0,
                numericKind,
                resolveMessage(message),
                false
        );
    }

    private static @Nullable NullUnsafeWriter parseNegativeAnnotationMirror(AnnotationMirror mirror, TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }
        warnIfGroupsPresent(mirror, type);

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.Negative.message");
        return new NullUnsafeWriter.NumericCompare(
                "<",
                0,
                numericKind,
                resolveMessage(message),
                false
        );
    }

    private static @Nullable NullUnsafeWriter parseNegativeOrZeroAnnotationMirror(AnnotationMirror mirror, TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }
        warnIfGroupsPresent(mirror, type);

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.NegativeOrZero.message");
        return new NullUnsafeWriter.NumericCompare(
                "<=",
                0,
                numericKind,
                resolveMessage(message),
                false
        );
    }

    private static @Nullable NullUnsafeWriter parseEmailAnnotationMirror(AnnotationMirror mirror, TypeAdapter type) {
        warnIfGroupsPresent(mirror, type);

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.Email.message");
        String regexp = getAnnotationStringValue(mirror, "regexp", ".*");
        List<String> flags = getAnnotationFlagsValue(mirror);
        return new NullUnsafeWriter.EmailPattern(
                regexp.equals(".*") ? null : regexp,
                flags,
                resolveMessage(message)
        );
    }

    private static @Nullable NullUnsafeWriter parsePatternAnnotationMirror(AnnotationMirror mirror, TypeAdapter type, int index) {
        warnIfGroupsPresent(mirror, type);

        String regexp = getAnnotationStringValue(mirror, "regexp", ".*");
        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.Pattern.message");
        List<String> flags = getAnnotationFlagsValue(mirror);

        return new NullUnsafeWriter.Pattern(
                index,
                regexp.replace("\\", "\\\\"),
                flags,
                resolveMessage(message, "{regexp}"),
                regexp
        );
    }

    private static @Nullable NullUnsafeWriter parseAssertTrueAnnotationMirror(AnnotationMirror mirror, TypeAdapter type) {
        warnIfGroupsPresent(mirror, type);

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.AssertTrue.message");
        return new NullUnsafeWriter.EqualTo("true", resolveMessage(message));
    }

    private static @Nullable NullUnsafeWriter parseAssertFalseAnnotationMirror(AnnotationMirror mirror, TypeAdapter type) {
        warnIfGroupsPresent(mirror, type);

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.AssertFalse.message");
        return new NullUnsafeWriter.EqualTo("false", resolveMessage(message));
    }

    private static @Nullable NullUnsafeWriter parseDecimalMaxAnnotationMirror(AnnotationMirror mirror, TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }
        warnIfGroupsPresent(mirror, type);

        String value = getAnnotationStringValue(mirror, "value", "0");
        boolean inclusive = getAnnotationBooleanValue(mirror, "inclusive", true);
        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.DecimalMax%s.message".formatted(inclusive ? "" : ".exclusive"));

        try {
            return new NullUnsafeWriter.NumericCompare(
                    inclusive ? "<=" : "<",
                    value,
                    numericKind,
                    resolveMessage(message, "{value}"),
                    true
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static @Nullable NullUnsafeWriter parseDecimalMinAnnotationMirror(AnnotationMirror mirror, TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }
        warnIfGroupsPresent(mirror, type);

        String value = getAnnotationStringValue(mirror, "value", "0");
        boolean inclusive = getAnnotationBooleanValue(mirror, "inclusive", true);
        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.DecimalMin%s.message".formatted(inclusive ? "" : ".exclusive"));

        try {
            return new NullUnsafeWriter.NumericCompare(
                    inclusive ? ">=" : ">",
                    value,
                    numericKind,
                    resolveMessage(message, "{value}"),
                    true
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static @Nullable NullUnsafeWriter parseDigitsAnnotationMirror(AnnotationMirror mirror, TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }
        warnIfGroupsPresent(mirror, type);

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.Digits.message");
        int integer = getAnnotationIntValue(mirror, "integer", 0);
        int fraction = getAnnotationIntValue(mirror, "fraction", 0);

        return new NullUnsafeWriter.Digits(
                integer,
                fraction,
                numericKind,
                resolveMessage(message, "{integer}", "{fraction}")
        );
    }

    private static @Nullable NullUnsafeWriter parseFutureAnnotationMirror(AnnotationMirror mirror, TypeAdapter type) {
        TemporalKind temporalKind = type.getTemporalKind();
        if (temporalKind == null) {
            return null;
        }
        warnIfGroupsPresent(mirror, type);

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.Future.message");
        return new NullUnsafeWriter.TemporalCompare(
                "isAfter", true, temporalKind, resolveMessage(message));
    }

    private static @Nullable NullUnsafeWriter parseFutureOrPresentAnnotationMirror(AnnotationMirror mirror, TypeAdapter type) {
        TemporalKind temporalKind = type.getTemporalKind();
        if (temporalKind == null) {
            return null;
        }
        warnIfGroupsPresent(mirror, type);

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.FutureOrPresent.message");
        return new NullUnsafeWriter.TemporalCompare(
                "isBefore", false, temporalKind, resolveMessage(message));
    }

    private static @Nullable NullUnsafeWriter parsePastAnnotationMirror(AnnotationMirror mirror, TypeAdapter type) {
        TemporalKind temporalKind = type.getTemporalKind();
        if (temporalKind == null) {
            return null;
        }
        warnIfGroupsPresent(mirror, type);

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.Past.message");
        return new NullUnsafeWriter.TemporalCompare(
                "isBefore", true, temporalKind, resolveMessage(message));
    }

    private static @Nullable NullUnsafeWriter parsePastOrPresentAnnotationMirror(AnnotationMirror mirror, TypeAdapter type) {
        TemporalKind temporalKind = type.getTemporalKind();
        if (temporalKind == null) {
            return null;
        }
        warnIfGroupsPresent(mirror, type);

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.PastOrPresent.message");
        return new NullUnsafeWriter.TemporalCompare(
                "isAfter", false, temporalKind, resolveMessage(message));
    }

    private static String resolveMessage(String message, String... params) {
        for (int i = 0; i < params.length; i++) {
            message = message.replace(params[i], "{" + i + "}");
        }
        return message;
    }

    private static List<String> getAnnotationFlagsValue(AnnotationMirror mirror) {
        Object value = getAnnotationValue(mirror, "flags");
        if (value instanceof List<?> list && !list.isEmpty()) {
            return list.stream()
                    .flatMap(obj -> obj instanceof AnnotationValue av ? Stream.of(av.getValue()) : Stream.empty())
                    .flatMap(av -> av instanceof VariableElement ve ? Stream.of(ve) : Stream.empty())
                    .map(ve -> ve.getSimpleName().toString())
                    .toList();
        }
        return List.of();
    }

    private static String getAnnotationStringValue(AnnotationMirror mirror, String attributeName, String defaultValue) {
        Object value = getAnnotationValue(mirror, attributeName);
        if (value instanceof String string) {
            if (string.startsWith("\"") && string.endsWith("\"")) {
                return string.substring(1, string.length() - 1);
            }
            return string;
        }
        return defaultValue;
    }

    private static long getAnnotationLongValue(AnnotationMirror mirror, String attributeName, long defaultValue) {
        Object value = getAnnotationValue(mirror, attributeName);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return defaultValue;
    }

    private static int getAnnotationIntValue(AnnotationMirror mirror, String attributeName, int defaultValue) {
        Object value = getAnnotationValue(mirror, attributeName);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return defaultValue;
    }

    private static boolean getAnnotationBooleanValue(AnnotationMirror mirror, String attributeName, boolean defaultValue) {
        Object value = getAnnotationValue(mirror, attributeName);
        if (value instanceof Boolean bool) {
            return bool;
        }
        return defaultValue;
    }

    private static @Nullable Object getAnnotationValue(AnnotationMirror mirror, String attributeName) {
        for (var entry : mirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals(attributeName)) {
                return entry.getValue().getValue();
            }
        }
        return null;
    }

    static void warnIfGroupsPresent(AnnotationMirror annotation, TypeAdapter type) {
        Object value = getAnnotationValue(annotation, "groups");
        if (value instanceof List<?> list && !list.isEmpty()) {
            type.printMessage(
                    javax.tools.Diagnostic.Kind.WARNING,
                    "groups attribute on @" + annotation.getAnnotationType().asElement().getSimpleName()
                            + " is not supported and will be ignored",
                    annotation
            );
        }
    }

}
