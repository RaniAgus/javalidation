package io.github.raniagus.javalidation.validator.processor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import javax.lang.model.element.AnnotationMirror;
import org.jspecify.annotations.Nullable;

public final class JakartaAnnotationParser {
    private static final Map<String, String> DEFAULT_MESSAGES = Map.ofEntries(
            Map.entry("{jakarta.validation.constraints.NotNull.message}", "must not be null"),
            Map.entry("{jakarta.validation.constraints.NotBlank.message}", "must not be blank"),
            Map.entry("{jakarta.validation.constraints.NotEmpty.message}", "must not be empty"),
            Map.entry("{jakarta.validation.constraints.Size.message}", "size must be between {min} and {max}"),
            Map.entry("{jakarta.validation.constraints.Min.message}", "must be greater than or equal to {value}"),
            Map.entry("{jakarta.validation.constraints.Max.message}", "must be less than or equal to {value}"),
            Map.entry("{jakarta.validation.constraints.Email.message}", "must be a well-formed email address"),
            Map.entry("{jakarta.validation.constraints.Pattern.message}", "must match \\\"{regexp}\\\""),
            Map.entry("{jakarta.validation.constraints.Positive.message}", "must be greater than 0"),
            Map.entry("{jakarta.validation.constraints.PositiveOrZero.message}", "must be greater than or equal to 0"),
            Map.entry("{jakarta.validation.constraints.Negative.message}", "must be less than 0"),
            Map.entry("{jakarta.validation.constraints.NegativeOrZero.message}", "must be less than or equal to 0"),
            Map.entry("{jakarta.validation.constraints.AssertTrue.message}", "must be true"),
            Map.entry("{jakarta.validation.constraints.AssertFalse.message}", "must be false"),
            Map.entry("{jakarta.validation.constraints.DecimalMax.message}", "must be less than {inclusive}{value}"),
            Map.entry("{jakarta.validation.constraints.DecimalMin.message}", "must be greater than {inclusive}{value}"),
            Map.entry("{jakarta.validation.constraints.Digits.message}", "numeric value out of bounds ({integer} digits, {fraction} decimal digits expected)"),
            Map.entry("{jakarta.validation.constraints.Future.message}", "must be a future date"),
            Map.entry("{jakarta.validation.constraints.FutureOrPresent.message}", "must be a date in the present or in the future"),
            Map.entry("{jakarta.validation.constraints.Past.message}", "must be a past date"),
            Map.entry("{jakarta.validation.constraints.PastOrPresent.message}", "must be a date in the past or in the present")
    );

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

        String message = getAnnotationMessage(annotationMirror);
        return new NullSafeWriter.NotNull(resolveMessage(message));
    }

    public static @Nullable NullSafeWriter parseNotEmptyAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(NotEmpty.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        return new NullSafeWriter.NullSafeAccessor("isEmpty", resolveMessage(message));
    }

    public static @Nullable NullSafeWriter parseNullAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(Null.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        return new NullSafeWriter.Null(resolveMessage(message));
    }

    public static @Nullable NullSafeWriter parseNotBlankAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(NotBlank.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        return new NullSafeWriter.NullSafeAccessor("isBlank", resolveMessage(message));
    }

    public static @Nullable NullUnsafeWriter parseSizeAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(Size.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
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

        String message = getAnnotationMessage(annotationMirror);
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

        String message = getAnnotationMessage(annotationMirror);
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

        String message = getAnnotationMessage(annotationMirror);
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

        String message = getAnnotationMessage(annotationMirror);
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

        String message = getAnnotationMessage(annotationMirror);
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

        String message = getAnnotationMessage(annotationMirror);
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

        String message = getAnnotationMessage(annotationMirror);
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
        String message = getAnnotationMessage(annotationMirror);

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

        String message = getAnnotationMessage(annotationMirror);
        return new NullUnsafeWriter.EqualTo("true", resolveMessage(message));
    }

    public static @Nullable NullUnsafeWriter parseAssertFalseAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(AssertFalse.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
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

        String message = getAnnotationMessage(annotationMirror);
        String value = getAnnotationStringValue(annotationMirror, "value", "0");
        boolean inclusive = getAnnotationBooleanValue(annotationMirror, "inclusive", true);

        try {
            return new NullUnsafeWriter.NumericCompare(
                    inclusive ? "<=" : "<",
                    value,
                    numericKind,
                    resolveMessage(message, "{value}").replace("{inclusive}", inclusive ? "or equal to " : ""),
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

        String message = getAnnotationMessage(annotationMirror);
        String value = getAnnotationStringValue(annotationMirror, "value", "0");
        boolean inclusive = getAnnotationBooleanValue(annotationMirror, "inclusive", true);

        try {
            return new NullUnsafeWriter.NumericCompare(
                    inclusive ? ">=" : ">",
                    value,
                    numericKind,
                    resolveMessage(message, "{value}").replace("{inclusive}", inclusive ? "or equal to " : ""),
                    true
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static @Nullable NullUnsafeWriter parseDigitsAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(Digits.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        int integer = getAnnotationIntValue(annotationMirror, "integer", 0);
        int fraction = getAnnotationIntValue(annotationMirror, "fraction", 0);

        // Build regex pattern: optional minus, up to 'integer' digits, optional decimal point and up to 'fraction' digits
        String pattern = "^-?\\\\d{0," + integer + "}(\\\\.\\\\d{0," + fraction + "})?$";

        return new NullUnsafeWriter.Pattern(
                pattern,
                resolveMessage(message, "{integer}", "{fraction}"),
                integer,
                fraction
        );
    }

    public static @Nullable NullUnsafeWriter parseFutureAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(Future.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        return new NullUnsafeWriter.TemporalCompare("isAfter", true, resolveMessage(message));
    }

    public static @Nullable NullUnsafeWriter parseFutureOrPresentAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(FutureOrPresent.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        return new NullUnsafeWriter.TemporalCompare("isBefore", false, resolveMessage(message));
    }

    public static @Nullable NullUnsafeWriter paresPastAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(Past.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        return new NullUnsafeWriter.TemporalCompare("isBefore", true, resolveMessage(message));
    }

    public static @Nullable NullUnsafeWriter parsePastOrPresentAnnotation(TypeAdapter type) {
        var annotationMirror = type.getAnnotationMirror(PastOrPresent.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        return new NullUnsafeWriter.TemporalCompare("isAfter", false, resolveMessage(message));
    }

    private static String resolveMessage(String message, String... params) {
        // First, resolve default message if it's a key reference
        String resolved = DEFAULT_MESSAGES.getOrDefault(message, message);

        // Replace named placeholders with positional ones
        for (int i = 0; i < params.length; i++) {
            resolved = resolved.replace(params[i], "{" + i + "}");
        }

        return resolved;
    }

    /**
     * Extract the message from an annotation mirror.
     */
    private static String getAnnotationMessage(AnnotationMirror mirror) {
        return getAnnotationStringValue(mirror, "message", "{" + mirror.getAnnotationType() + ".message}");
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
