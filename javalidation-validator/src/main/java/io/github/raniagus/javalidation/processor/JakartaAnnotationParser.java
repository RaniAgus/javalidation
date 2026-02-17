package io.github.raniagus.javalidation.processor;

import jakarta.validation.constraints.*;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import javax.lang.model.AnnotatedConstruct;
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
            Map.entry("{jakarta.validation.constraints.Pattern.message}", "must match \"{regexp}\""),
            Map.entry("{jakarta.validation.constraints.Positive.message}", "must be greater than 0"),
            Map.entry("{jakarta.validation.constraints.PositiveOrZero.message}", "must be greater than or equal to 0"),
            Map.entry("{jakarta.validation.constraints.Negative.message}", "must be less than 0"),
            Map.entry("{jakarta.validation.constraints.NegativeOrZero.message}", "must be less than or equal to 0"),
            Map.entry("{jakarta.validation.constraints.AssertTrue.message}", "must be true"),
            Map.entry("{jakarta.validation.constraints.AssertFalse.message}", "must be false"),
            Map.entry("{jakarta.validation.constraints.DecimalMax.message}", "must be less than or equal to {value}"),
            Map.entry("{jakarta.validation.constraints.DecimalMin.message}", "must be greater than or equal to {value}"),
            Map.entry("{jakarta.validation.constraints.Digits.message}", "numeric value out of bounds (<{integer} digits>.<{fraction} digits> expected)"),
            Map.entry("{jakarta.validation.constraints.Future.message}", "must be a future date"),
            Map.entry("{jakarta.validation.constraints.FutureOrPresent.message}", "must be a date in the present or in the future"),
            Map.entry("{jakarta.validation.constraints.Past.message}", "must be a past date"),
            Map.entry("{jakarta.validation.constraints.PastOrPresent.message}", "must be a date in the past or in the present")
    );

    private JakartaAnnotationParser() {}

    public static ValidationWriter.@Nullable NullSafeWriter parseNullSafeWriters(AnnotatedConstruct annotated) {
        return Stream.of(
                parseNullAnnotation(annotated),
                parseNotBlankAnnotation(annotated),
                parseNotEmptyAnnotation(annotated),
                parseNotNullAnnotation(annotated)
        ).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public static Stream<ValidationWriter.NullUnsafeWriter> parseNullUnsafeWriters(AnnotatedConstruct annotated) {
        return Stream.<ValidationWriter.NullUnsafeWriter>of(
                parseSizeAnnotation(annotated),
                parseMinAnnotation(annotated),
                parseMaxAnnotation(annotated),
                parsePositiveAnnotation(annotated),
                parsePositiveOrZeroAnnotation(annotated),
                parseNegativeAnnotation(annotated),
                parseNegativeOrZeroAnnotation(annotated),
                parseEmailAnnotation(annotated),
                parsePatternAnnotation(annotated),
                parseAssertTrueAnnotation(annotated),
                parseAssertFalseAnnotation(annotated),
                parseDecimalMaxAnnotation(annotated),
                parseDecimalMinAnnotation(annotated),
                parseDigitsAnnotation(annotated),
                parseFutureAnnotation(annotated),
                parseFutureOrPresentAnnotation(annotated),
                paresPastAnnotation(annotated),
                parsePastOrPresentAnnotation(annotated)
        ).filter(Objects::nonNull);
    }

    public static ValidationWriter.@Nullable NullSafeWriter parseNotNullAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, NotNull.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        return new ValidationWriter.NotNull(resolveMessage(message));
    }

    public static ValidationWriter.@Nullable NullSafeWriter parseNotEmptyAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, NotEmpty.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        return new ValidationWriter.NullSafeCondition("isEmpty", resolveMessage(message));
    }

    public static ValidationWriter.@Nullable NullSafeWriter parseNullAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, Null.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        return new ValidationWriter.Null(resolveMessage(message));
    }

    public static ValidationWriter.@Nullable NullSafeWriter parseNotBlankAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, NotBlank.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        return new ValidationWriter.NullSafeCondition("isBlank", resolveMessage(message));
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseSizeAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, Size.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        int min = getAnnotationIntValue(annotationMirror, "min", 0);
        int max = getAnnotationIntValue(annotationMirror, "max", Integer.MAX_VALUE);

        return new ValidationWriter.Size(
                "length", // TODO: Add Collection support
                resolveMessage(message, "{min}", "{max}"),
                min,
                max
        );
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseMinAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, Min.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        long value = getAnnotationLongValue(annotationMirror, "value", 0);

        return new ValidationWriter.MoreThanOrEqual(
                resolveMessage(message, "{value}"),
                value
        );
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseMaxAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, Max.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        long value = getAnnotationLongValue(annotationMirror, "value", 0);

        return new ValidationWriter.LessThanOrEqual(
                resolveMessage(message, "{value}"),
                value
        );
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parsePositiveAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, Positive.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        return new ValidationWriter.MoreThan(resolveMessage(message), 0);
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parsePositiveOrZeroAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, PositiveOrZero.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        return new ValidationWriter.MoreThanOrEqual(resolveMessage(message), 0);
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseNegativeAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, Negative.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        return new ValidationWriter.LessThan(resolveMessage(message), 0);
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseNegativeOrZeroAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, NegativeOrZero.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        return new ValidationWriter.LessThanOrEqual(resolveMessage(message), 0);
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseEmailAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, Email.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        return new ValidationWriter.Pattern(
                "^[^@]+@[^@]+\\\\.[^@]+$", // TODO: check email validation regex
                resolveMessage(message)
        );
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parsePatternAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, Pattern.class);
        if (annotationMirror == null) {
            return null;
        }

        String regexp = getAnnotationStringValue(annotationMirror, "regexp", "");
        String message = getAnnotationMessage(annotationMirror);

        return new ValidationWriter.Pattern(
                regexp.replace("\\", "\\\\"), // TODO: Check how to prevent escaping
                resolveMessage(message, "{regexp}")
        );
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseAssertTrueAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, AssertTrue.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        return new ValidationWriter.EqualTo("true", resolveMessage(message));
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseAssertFalseAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, AssertFalse.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        return new ValidationWriter.EqualTo("false", resolveMessage(message));
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseDecimalMaxAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, DecimalMax.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        String valueStr = getAnnotationStringValue(annotationMirror, "value", "0");

        try {
            long value = Long.parseLong(valueStr);
            return new ValidationWriter.LessThanOrEqual(
                    resolveMessage(message, "{value}"),
                    value
            );
        } catch (NumberFormatException e) {
            // TODO: For non-integer decimals, return null (not supported yet)
            return null;
        }
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseDecimalMinAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, DecimalMin.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        String valueStr = getAnnotationStringValue(annotationMirror, "value", "0");

        try {
            long value = Long.parseLong(valueStr);
            return new ValidationWriter.MoreThanOrEqual(
                    resolveMessage(message, "{value}"),
                    value
            );
        } catch (NumberFormatException e) {
            // TODO: For non-integer decimals, return null (not supported yet)
            return null;
        }
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseDigitsAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, Digits.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        int integer = getAnnotationIntValue(annotationMirror, "integer", 0);
        int fraction = getAnnotationIntValue(annotationMirror, "fraction", 0);

        // Build regex pattern: optional minus, up to 'integer' digits, optional decimal point and up to 'fraction' digits
        String pattern = "^-?\\\\d{0," + integer + "}(\\\\.\\\\d{0," + fraction + "})?$";

        return new ValidationWriter.Pattern(
                pattern,
                resolveMessage(message, "{integer}", "{fraction}")
        );
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseFutureAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, Future.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        // TODO: Return null for now - temporal validation requires more complex logic
        return null;
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseFutureOrPresentAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, FutureOrPresent.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        // TODO: Return null for now - temporal validation requires more complex logic
        return null;
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter paresPastAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, Past.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        // TODO: Return null for now - temporal validation requires more complex logic
        return null;
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parsePastOrPresentAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, PastOrPresent.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationMessage(annotationMirror);
        // TODO: Return null for now - temporal validation requires more complex logic
        return null;
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
     * Get an AnnotationMirror for the specified annotation class.
     * This works for both declaration annotations and type-use annotations (TypeCompound).
     */
    private static @Nullable AnnotationMirror getAnnotationMirror(
            AnnotatedConstruct annotated,
            Class<? extends Annotation> annotationClass
    ) {
        String annotationName = annotationClass.getName();
        for (var annotationMirror : annotated.getAnnotationMirrors()) {
            var annotationType = annotationMirror.getAnnotationType();
            if (annotationType.toString().equals(annotationName)) {
                return annotationMirror;
            }
        }
        return null;
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

    private static @Nullable Object getAnnotationValue(AnnotationMirror mirror, String attributeName) {
        for (var entry : mirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals(attributeName)) {
                return entry.getValue().getValue();
            }
        }
        return null;
    }

}
