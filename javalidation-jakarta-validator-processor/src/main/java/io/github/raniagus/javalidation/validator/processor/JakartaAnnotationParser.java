package io.github.raniagus.javalidation.validator.processor;

import jakarta.validation.constraints.*;
import java.util.Objects;
import java.util.stream.Stream;
import javax.lang.model.element.AnnotationMirror;
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
        return Stream.<NullUnsafeWriter>of(
                parseSizeAnnotation(type),
                parseMinAnnotation(type),
                parseMaxAnnotation(type),
                parsePositiveAnnotation(type),
                parsePositiveOrZeroAnnotation(type),
                parseNegativeAnnotation(type),
                parseNegativeOrZeroAnnotation(type),
                parseEmailAnnotation(type),
                parsePatternAnnotation(type),
                parseAssertTrueAnnotation(type),
                parseAssertFalseAnnotation(type),
                parseDecimalMaxAnnotation(type),
                parseDecimalMinAnnotation(type),
                parseDigitsAnnotation(type),
                parseFutureAnnotation(type),
                parseFutureOrPresentAnnotation(type),
                paresPastAnnotation(type),
                parsePastOrPresentAnnotation(type)
        ).filter(Objects::nonNull);
    }

    public static @Nullable NullSafeWriter parseNotNullAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(NotNull.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.NotNull.message");
        return new NullSafeWriter.NotNull(resolveMessage(message));
    }

    public static @Nullable NullSafeWriter parseNotEmptyAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(NotEmpty.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.NotEmpty.message");
        return new NullSafeWriter.NullSafeAccessor("isEmpty", resolveMessage(message));
    }

    public static @Nullable NullSafeWriter parseNullAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(Null.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.Null.message");
        return new NullSafeWriter.Null(resolveMessage(message));
    }

    public static @Nullable NullSafeWriter parseNotBlankAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(NotBlank.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.NotBlank.message");
        return new NullSafeWriter.NullSafeAccessor("isBlank", resolveMessage(message));
    }

    public static @Nullable NullUnsafeWriter parseSizeAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(Size.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.Size.message");
        int min = getAnnotationIntValue(annotationMirror, "min", 0);
        int max = getAnnotationIntValue(annotationMirror, "max", Integer.MAX_VALUE);

        return new NullUnsafeWriter.Size(
                type.isCollection() || type.isOfType("java.util.Map") ? "size" : "length",
                resolveMessage(message, "{min}", "{max}"),
                min,
                max
        );
    }

    public static @Nullable NullUnsafeWriter parseMinAnnotation(TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }

        var annotationMirror = type.getAnnotationMirror(Min.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.Min.message");
        long value = getAnnotationLongValue(annotationMirror, "value", 0);

        return new NullUnsafeWriter.NumericCompare(
                ">=",
                value,
                numericKind,
                resolveMessage(message, "{value}"),
                true
        );
    }

    public static @Nullable NullUnsafeWriter parseMaxAnnotation(TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }

        var annotationMirror = type.getAnnotationMirror(Max.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.Max.message");
        long value = getAnnotationLongValue(annotationMirror, "value", 0);

        return new NullUnsafeWriter.NumericCompare(
                "<=",
                value,
                numericKind,
                resolveMessage(message, "{value}"),
                true
        );
    }

    public static @Nullable NullUnsafeWriter parsePositiveAnnotation(TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }

        var annotationMirror = type.getAnnotationMirror(Positive.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.Positive.message");
        return new NullUnsafeWriter.NumericCompare(
                ">",
                0,
                numericKind,
                resolveMessage(message),
                false
        );
    }

    public static @Nullable NullUnsafeWriter parsePositiveOrZeroAnnotation(TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }

        var annotationMirror = type.getAnnotationMirror(PositiveOrZero.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.PositiveOrZero.message");
        return new NullUnsafeWriter.NumericCompare(
                ">=",
                0,
                numericKind,
                resolveMessage(message),
                false
        );
    }

    public static @Nullable NullUnsafeWriter parseNegativeAnnotation(TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }

        var annotationMirror = type.getAnnotationMirror(Negative.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.Negative.message");
        return new NullUnsafeWriter.NumericCompare(
                "<",
                0,
                numericKind,
                resolveMessage(message),
                false
        );
    }

    public static @Nullable NullUnsafeWriter parseNegativeOrZeroAnnotation(TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }

        var annotationMirror = type.getAnnotationMirror(NegativeOrZero.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.NegativeOrZero.message");
        return new NullUnsafeWriter.NumericCompare(
                "<=",
                0,
                numericKind,
                resolveMessage(message),
                false
        );
    }

    public static @Nullable NullUnsafeWriter parseEmailAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(Email.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.Email.message");
        return new NullUnsafeWriter.Pattern(
                "^[^@]+@[^@]+\\\\.[^@]+$", // TODO: check email validation regex
                resolveMessage(message)
        );
    }

    public static @Nullable NullUnsafeWriter parsePatternAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(Pattern.class);
        if (annotationMirror == null) {
            return null;
        }

        String regexp = getAnnotationStringValue(annotationMirror, "regexp", "");
        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.Pattern.message");

        return new NullUnsafeWriter.Pattern(
                regexp.replace("\\", "\\\\"),
                resolveMessage(message, "{regexp}"),
                regexp
        );
    }

    public static @Nullable NullUnsafeWriter parseAssertTrueAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(AssertTrue.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.AssertTrue.message");
        return new NullUnsafeWriter.EqualTo("true", resolveMessage(message));
    }

    public static @Nullable NullUnsafeWriter parseAssertFalseAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(AssertFalse.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.AssertFalse.message");
        return new NullUnsafeWriter.EqualTo("false", resolveMessage(message));
    }

    public static @Nullable NullUnsafeWriter parseDecimalMaxAnnotation(TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }

        var annotationMirror = type.getAnnotationMirror(DecimalMax.class);
        if (annotationMirror == null) {
            return null;
        }

        String value = getAnnotationStringValue(annotationMirror, "value", "0");
        boolean inclusive = getAnnotationBooleanValue(annotationMirror, "inclusive", true);
        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.DecimalMax%s.message".formatted(inclusive ? "" : ".exclusive"));

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

    public static @Nullable NullUnsafeWriter parseDecimalMinAnnotation(TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }

        var annotationMirror = type.getAnnotationMirror(DecimalMin.class);
        if (annotationMirror == null) {
            return null;
        }

        String value = getAnnotationStringValue(annotationMirror, "value", "0");
        boolean inclusive = getAnnotationBooleanValue(annotationMirror, "inclusive", true);
        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.DecimalMin%s.message".formatted(inclusive ? "" : ".exclusive"));

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

    public static @Nullable NullUnsafeWriter parseDigitsAnnotation(TypeAdapter type) {
        NumericKind numericKind = type.getNumericKind();
        if (numericKind == null) {
            return null;
        }

        var annotationMirror = type.getAnnotationMirror(Digits.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.Digits.message");
        int integer = getAnnotationIntValue(annotationMirror, "integer", 0);
        int fraction = getAnnotationIntValue(annotationMirror, "fraction", 0);

        return new NullUnsafeWriter.Digits(
                integer,
                fraction,
                numericKind,
                resolveMessage(message, "{integer}", "{fraction}")
        );
    }

    public static @Nullable NullUnsafeWriter parseFutureAnnotation(TypeAdapter type) {
        TemporalKind temporalKind = type.getTemporalKind();
        if (temporalKind == null) {
            return null;
        }

        var annotationMirror = type.getAnnotationMirror(Future.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.Future.message");
        return new NullUnsafeWriter.TemporalCompare(
                "isAfter", true, temporalKind, resolveMessage(message));
    }

    public static @Nullable NullUnsafeWriter parseFutureOrPresentAnnotation(TypeAdapter type) {
        TemporalKind temporalKind = type.getTemporalKind();
        if (temporalKind == null) {
            return null;
        }

        var annotationMirror = type.getAnnotationMirror(FutureOrPresent.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.FutureOrPresent.message");
        return new NullUnsafeWriter.TemporalCompare(
                "isBefore", false, temporalKind, resolveMessage(message));
    }

    public static @Nullable NullUnsafeWriter paresPastAnnotation(TypeAdapter type) {
        TemporalKind temporalKind = type.getTemporalKind();
        if (temporalKind == null) {
            return null;
        }

        var annotationMirror = type.getAnnotationMirror(Past.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.Past.message");
        return new NullUnsafeWriter.TemporalCompare(
                "isBefore", true, temporalKind, resolveMessage(message));
    }

    public static @Nullable NullUnsafeWriter parsePastOrPresentAnnotation(TypeAdapter type) {
        TemporalKind temporalKind = type.getTemporalKind();
        if (temporalKind == null) {
            return null;
        }

        var annotationMirror = type.getAnnotationMirror(PastOrPresent.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "io.github.raniagus.javalidation.constraints.PastOrPresent.message");
        return new NullUnsafeWriter.TemporalCompare(
                "isAfter", false, temporalKind, resolveMessage(message));
    }

    private static String resolveMessage(String message, String... params) {
        // Replace named placeholders with positional ones
        for (int i = 0; i < params.length; i++) {
            message = message.replace(params[i], "{" + i + "}");
        }

        return message;
    }

    /**
     * Extract a string value from an annotation mirror.
     */
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

    /**
     * Extract a long value from an annotation mirror.
     */
    private static long getAnnotationLongValue(AnnotationMirror mirror, String attributeName, long defaultValue) {
        Object value = getAnnotationValue(mirror, attributeName);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return defaultValue;
    }

    /**
     * Extract an int value from an annotation mirror.
     */
    private static int getAnnotationIntValue(AnnotationMirror mirror, String attributeName, int defaultValue) {
        Object value = getAnnotationValue(mirror, attributeName);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return defaultValue;
    }

    /**
     * Extract a boolean value from an annotation mirror.
     */
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

}
