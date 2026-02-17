package io.github.raniagus.javalidation.processor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import javax.lang.model.AnnotatedConstruct;
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
            Map.entry("{jakarta.validation.constraints.NegativeOrZero.message}", "must be less than or equal to 0")
    );

    private JakartaAnnotationParser() {}

    public static ValidationWriter.@Nullable NullSafeWriter parseNullSafeWriters(AnnotatedConstruct annotated) {
        return Stream.of(
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
                parsePatternAnnotation(annotated)
        ).filter(Objects::nonNull);
    }

    public static ValidationWriter.@Nullable NullSafeWriter parseNotNullAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, NotNull.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "{jakarta.validation.constraints.NotNull.message}");
        return new ValidationWriter.NotNull(resolveMessage(message));
    }

    public static ValidationWriter.@Nullable NullSafeWriter parseNotEmptyAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, NotEmpty.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "{jakarta.validation.constraints.NotEmpty.message}");
        return new ValidationWriter.NullSafeCondition("isEmpty", resolveMessage(message));
    }

    public static ValidationWriter.@Nullable NullSafeWriter parseNotBlankAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, NotBlank.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "{jakarta.validation.constraints.NotBlank.message}");
        return new ValidationWriter.NullSafeCondition("isBlank", resolveMessage(message));
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseSizeAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, Size.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "{jakarta.validation.constraints.Size.message}");
        int min = getAnnotationIntValue(annotationMirror, "min", 0);
        int max = getAnnotationIntValue(annotationMirror, "max", Integer.MAX_VALUE);

        return new ValidationWriter.Size(
                "length", // TODO: Add iterables support
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

        String message = getAnnotationStringValue(annotationMirror, "message", "{jakarta.validation.constraints.Min.message}");
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

        String message = getAnnotationStringValue(annotationMirror, "message", "{jakarta.validation.constraints.Max.message}");
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

        String message = getAnnotationStringValue(annotationMirror, "message", "{jakarta.validation.constraints.Positive.message}");
        return new ValidationWriter.MoreThan(resolveMessage(message), 0);
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parsePositiveOrZeroAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, PositiveOrZero.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "{jakarta.validation.constraints.PositiveOrZero.message}");
        return new ValidationWriter.MoreThanOrEqual(resolveMessage(message), 0);
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseNegativeAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, Negative.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "{jakarta.validation.constraints.Negative.message}");
        return new ValidationWriter.LessThan(resolveMessage(message), 0);
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseNegativeOrZeroAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, NegativeOrZero.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "{jakarta.validation.constraints.NegativeOrZero.message}");
        return new ValidationWriter.LessThanOrEqual(resolveMessage(message), 0);
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseEmailAnnotation(AnnotatedConstruct annotated) {
        var annotationMirror = getAnnotationMirror(annotated, Email.class);
        if (annotationMirror == null) {
            return null;
        }

        String message = getAnnotationStringValue(annotationMirror, "message", "{jakarta.validation.constraints.Email.message}");
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
        String message = getAnnotationStringValue(annotationMirror, "message", "{jakarta.validation.constraints.Pattern.message}");

        return new ValidationWriter.Pattern(
                regexp.replace("\\", "\\\\"), // TODO: Check how to prevent escaping
                resolveMessage(message, "{regexp}")
        );
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
    private static javax.lang.model.element.@Nullable AnnotationMirror getAnnotationMirror(
            AnnotatedConstruct annotated, Class<? extends Annotation> annotationClass) {
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
     * Extract a string value from an annotation mirror.
     */
    private static String getAnnotationStringValue(javax.lang.model.element.AnnotationMirror mirror, String attributeName, String defaultValue) {
        for (var entry : mirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals(attributeName)) {
                Object value = entry.getValue().getValue();
                // Remove quotes from string literals
                String strValue = value.toString();
                if (strValue.startsWith("\"") && strValue.endsWith("\"")) {
                    return strValue.substring(1, strValue.length() - 1);
                }
                return strValue;
            }
        }
        return defaultValue;
    }

    /**
     * Extract a long value from an annotation mirror.
     */
    private static long getAnnotationLongValue(javax.lang.model.element.AnnotationMirror mirror, String attributeName, long defaultValue) {
        for (var entry : mirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals(attributeName)) {
                Object value = entry.getValue().getValue();
                if (value instanceof Number number) {
                    return number.longValue();
                }
            }
        }
        return defaultValue;
    }

    /**
     * Extract an int value from an annotation mirror.
     */
    private static int getAnnotationIntValue(javax.lang.model.element.AnnotationMirror mirror, String attributeName, int defaultValue) {
        for (var entry : mirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals(attributeName)) {
                Object value = entry.getValue().getValue();
                if (value instanceof Number number) {
                    return number.intValue();
                }
            }
        }
        return defaultValue;
    }

}
