package io.github.raniagus.javalidation.validator.processor;

import jakarta.validation.constraints.*;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
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
                parseRepeatableAnnotation(type, Null.class, Null.List.class, (mirror, i) -> parseNullAnnotation(mirror)),
                parseRepeatableAnnotation(type, NotBlank.class, NotBlank.List.class, (mirror, i) -> parseNotBlankAnnotation(mirror)),
                parseRepeatableAnnotation(type, NotEmpty.class, NotEmpty.List.class, (mirror, i) -> parseNotEmptyAnnotation(mirror)),
                parseRepeatableAnnotation(type, NotNull.class, NotNull.List.class, (mirror, i) -> parseNotNullAnnotation(mirror))
        ).flatMap(Function.identity())
                .findFirst()
                .orElse(null);
    }

    public static Stream<NullUnsafeWriter> parseNullUnsafeWriters(TypeAdapter type) {
        return Stream.of(
                parseRepeatableAnnotation(type, Size.class, Size.List.class, (mirror, i) -> parseSizeAnnotation(mirror, type)),
                parseRepeatableAnnotation(type, Min.class, Min.List.class, (mirror, i) -> parseMinAnnotation(mirror, type)),
                parseRepeatableAnnotation(type, Max.class, Max.List.class, (mirror, i) -> parseMaxAnnotation(mirror, type)),
                parseRepeatableAnnotation(type, Positive.class, Positive.List.class, (mirror, i) -> parsePositiveAnnotation(mirror, type)),
                parseRepeatableAnnotation(type, PositiveOrZero.class, PositiveOrZero.List.class, (mirror, i) -> parsePositiveOrZeroAnnotation(mirror, type)),
                parseRepeatableAnnotation(type, Negative.class, Negative.List.class, (mirror, i) -> parseNegativeAnnotation(mirror, type)),
                parseRepeatableAnnotation(type, NegativeOrZero.class, NegativeOrZero.List.class, (mirror, i) -> parseNegativeOrZeroAnnotation(mirror, type)),
                parseRepeatableAnnotation(type, Email.class, Email.List.class, (mirror, i) -> parseEmailAnnotation(mirror)),
                parseRepeatableAnnotation(type, Pattern.class, Pattern.List.class, (mirror, i) -> parsePatternAnnotation(mirror, i + 1)),
                parseRepeatableAnnotation(type, AssertTrue.class, AssertTrue.List.class, (mirror, i) -> parseAssertTrueAnnotation(mirror)),
                parseRepeatableAnnotation(type, AssertFalse.class, AssertFalse.List.class, (mirror, i) -> parseAssertFalseAnnotation(mirror)),
                parseRepeatableAnnotation(type, DecimalMax.class, DecimalMax.List.class, (mirror, i) -> parseDecimalMaxAnnotation(mirror, type)),
                parseRepeatableAnnotation(type, DecimalMin.class, DecimalMin.List.class, (mirror, i) -> parseDecimalMinAnnotation(mirror, type)),
                parseRepeatableAnnotation(type, Digits.class, Digits.List.class, (mirror, i) -> parseDigitsAnnotation(mirror, type)),
                parseRepeatableAnnotation(type, Future.class, Future.List.class, (mirror, i) -> parseFutureAnnotation(mirror, type)),
                parseRepeatableAnnotation(type, FutureOrPresent.class, FutureOrPresent.List.class, (mirror, i) -> parseFutureOrPresentAnnotation(mirror, type)),
                parseRepeatableAnnotation(type, Past.class, Past.List.class, (mirror, i) -> parsePastAnnotation(mirror, type)),
                parseRepeatableAnnotation(type, PastOrPresent.class, PastOrPresent.List.class, (mirror, i) -> parsePastOrPresentAnnotation(mirror, type))
        ).flatMap(Function.identity());
    }

    private static <T> Stream<T> parseRepeatableAnnotation(
            TypeAdapter type,
            Class<? extends Annotation> annotationClass,
            Class<? extends Annotation> listClass,
            BiFunction<AnnotationMirror, Integer, @Nullable T> parser) {
        var single = type.getAnnotationMirror(annotationClass);
        if (single != null) {
            warnIfGroupsPresent(single, type);
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
                .mapToObj(i -> {
                    warnIfGroupsPresent(mirrors.get(i), type);
                    return parser.apply(mirrors.get(i), i);
                })
                .filter(Objects::nonNull);
    }

    public static NullSafeWriter parseNotNullAnnotation(AnnotationMirror annotationMirror) {
        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.NotNull.message");
        return new NullSafeWriter.NotNull(resolveMessage(message));
    }

    public static NullSafeWriter parseNotEmptyAnnotation(AnnotationMirror annotationMirror) {
        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.NotEmpty.message");
        return new NullSafeWriter.NullSafeAccessor("isEmpty", resolveMessage(message));
    }

    public static NullSafeWriter parseNullAnnotation(AnnotationMirror annotationMirror) {
        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.Null.message");
        return new NullSafeWriter.Null(resolveMessage(message));
    }

    public static NullSafeWriter parseNotBlankAnnotation(AnnotationMirror annotationMirror) {
        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.NotBlank.message");
        return new NullSafeWriter.NullSafeAccessor("isBlank", resolveMessage(message));
    }

    private static NullUnsafeWriter parseSizeAnnotation(AnnotationMirror mirror, TypeAdapter type) {
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

    private static @Nullable NullUnsafeWriter parseMinAnnotation(AnnotationMirror mirror, TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }

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

    private static @Nullable NullUnsafeWriter parseMaxAnnotation(AnnotationMirror mirror, TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }

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

    private static @Nullable NullUnsafeWriter parsePositiveAnnotation(AnnotationMirror mirror, TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.Positive.message");
        return new NullUnsafeWriter.NumericCompare(
                ">",
                0,
                numericKind,
                resolveMessage(message),
                false
        );
    }

    private static @Nullable NullUnsafeWriter parsePositiveOrZeroAnnotation(AnnotationMirror mirror, TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.PositiveOrZero.message");
        return new NullUnsafeWriter.NumericCompare(
                ">=",
                0,
                numericKind,
                resolveMessage(message),
                false
        );
    }

    private static @Nullable NullUnsafeWriter parseNegativeAnnotation(AnnotationMirror mirror, TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.Negative.message");
        return new NullUnsafeWriter.NumericCompare(
                "<",
                0,
                numericKind,
                resolveMessage(message),
                false
        );
    }

    private static @Nullable NullUnsafeWriter parseNegativeOrZeroAnnotation(AnnotationMirror mirror, TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.NegativeOrZero.message");
        return new NullUnsafeWriter.NumericCompare(
                "<=",
                0,
                numericKind,
                resolveMessage(message),
                false
        );
    }

    private static NullUnsafeWriter parseEmailAnnotation(AnnotationMirror mirror) {
        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.Email.message");
        String regexp = getAnnotationStringValue(mirror, "regexp", ".*");
        List<String> flags = getAnnotationEnumValues(mirror, "flags");
        return new NullUnsafeWriter.EmailPattern(
                regexp.equals(".*") ? null : regexp,
                flags,
                resolveMessage(message)
        );
    }

    private static NullUnsafeWriter parsePatternAnnotation(AnnotationMirror mirror, int index) {
        String regexp = getAnnotationStringValue(mirror, "regexp", ".*");
        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.Pattern.message");
        List<String> flags = getAnnotationEnumValues(mirror, "flags");

        return new NullUnsafeWriter.Pattern(
                index,
                regexp.replace("\\", "\\\\"),
                flags,
                resolveMessage(message, "{regexp}"),
                regexp
        );
    }

    private static NullUnsafeWriter parseAssertTrueAnnotation(AnnotationMirror mirror) {
        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.AssertTrue.message");
        return new NullUnsafeWriter.EqualTo("true", resolveMessage(message));
    }

    private static NullUnsafeWriter parseAssertFalseAnnotation(AnnotationMirror mirror) {
        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.AssertFalse.message");
        return new NullUnsafeWriter.EqualTo("false", resolveMessage(message));
    }

    private static @Nullable NullUnsafeWriter parseDecimalMaxAnnotation(AnnotationMirror mirror, TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }

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

    private static @Nullable NullUnsafeWriter parseDecimalMinAnnotation(AnnotationMirror mirror, TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }

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

    private static @Nullable NullUnsafeWriter parseDigitsAnnotation(AnnotationMirror mirror, TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }

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

    private static @Nullable NullUnsafeWriter parseFutureAnnotation(AnnotationMirror mirror, TypeAdapter type) {
        TemporalKind temporalKind = type.getTemporalKind();
        if (temporalKind == null) {
            return null;
        }

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.Future.message");
        return new NullUnsafeWriter.TemporalCompare(
                "isAfter", true, temporalKind, resolveMessage(message));
    }

    private static @Nullable NullUnsafeWriter parseFutureOrPresentAnnotation(AnnotationMirror mirror, TypeAdapter type) {
        TemporalKind temporalKind = type.getTemporalKind();
        if (temporalKind == null) {
            return null;
        }

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.FutureOrPresent.message");
        return new NullUnsafeWriter.TemporalCompare(
                "isBefore", false, temporalKind, resolveMessage(message));
    }

    private static @Nullable NullUnsafeWriter parsePastAnnotation(AnnotationMirror mirror, TypeAdapter type) {
        TemporalKind temporalKind = type.getTemporalKind();
        if (temporalKind == null) {
            return null;
        }

        String message = getAnnotationStringValue(mirror, "message", "io.github.raniagus.javalidation.constraints.Past.message");
        return new NullUnsafeWriter.TemporalCompare(
                "isBefore", true, temporalKind, resolveMessage(message));
    }

    private static @Nullable NullUnsafeWriter parsePastOrPresentAnnotation(AnnotationMirror mirror, TypeAdapter type) {
        TemporalKind temporalKind = type.getTemporalKind();
        if (temporalKind == null) {
            return null;
        }

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

    private static List<String> getAnnotationEnumValues(AnnotationMirror mirror, String attributeName) {
        Object value = getAnnotationValue(mirror, attributeName);
        if (!(value instanceof List<?> list) || list.isEmpty()) {
            return List.of();
        }

        return list.stream()
                .flatMap(obj -> obj instanceof AnnotationValue av
                        && av.getValue() instanceof VariableElement ve ? Stream.of(ve) : Stream.empty())
                .map(ve -> ve.getSimpleName().toString())
                .toList();
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
