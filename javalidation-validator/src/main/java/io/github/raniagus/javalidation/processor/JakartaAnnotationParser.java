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
import java.util.Map;
import javax.lang.model.element.RecordComponentElement;
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

    public static ValidationWriter.@Nullable NullSafeWriter parseNotNullAnnotation(RecordComponentElement component) {
        NotNull annotation = component.getAccessor().getAnnotation(NotNull.class);
        if (annotation == null) {
            return null;
        }

        return new ValidationWriter.NotNull(resolveMessage(annotation.message()));
    }

    public static ValidationWriter.@Nullable NullSafeWriter parseNotEmptyAnnotation(RecordComponentElement component) {
        NotEmpty annotation = component.getAccessor().getAnnotation(NotEmpty.class);
        if (annotation == null) {
            return null;
        }

        return new ValidationWriter.NotEmpty(resolveMessage(annotation.message()));
    }

    public static ValidationWriter.@Nullable NullSafeWriter parseNotBlankAnnotation(RecordComponentElement componentElement) {
        NotBlank annotation = componentElement.getAccessor().getAnnotation(NotBlank.class);
        if (annotation == null) {
            return null;
        }

        return new ValidationWriter.NotBlank(resolveMessage(annotation.message()));
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseSizeAnnotation(RecordComponentElement component) {
        Size annotation = component.getAccessor().getAnnotation(Size.class);
        if (annotation == null) {
            return null;
        }

        return new ValidationWriter.Size(
                "length", // TODO: Add iterables support
                resolveMessage(annotation.message(), "{min}", "{max}"),
                annotation.min(),
                annotation.max()
        );
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseMinAnnotation(RecordComponentElement component) {
        Min annotation = component.getAccessor().getAnnotation(Min.class);
        if (annotation == null) {
            return null;
        }

        return new ValidationWriter.MoreThanOrEqual(
                resolveMessage(annotation.message(), "{value}"),
                annotation.value()
        );
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseMaxAnnotation(RecordComponentElement component) {
        Max annotation = component.getAccessor().getAnnotation(Max.class);
        if (annotation == null) {
            return null;
        }

        return new ValidationWriter.LessThanOrEqual(
                resolveMessage(annotation.message(), "{value}"),
                annotation.value()
        );
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parsePositiveAnnotation(RecordComponentElement component) {
        Positive annotation = component.getAccessor().getAnnotation(Positive.class);
        if (annotation == null) {
            return null;
        }

        return new ValidationWriter.MoreThan(resolveMessage(annotation.message()), 0);
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parsePositiveOrZeroAnnotation(RecordComponentElement component) {
        PositiveOrZero annotation = component.getAccessor().getAnnotation(PositiveOrZero.class);
        if (annotation == null) {
            return null;
        }

        return new ValidationWriter.MoreThanOrEqual(resolveMessage(annotation.message()), 0);
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseNegativeAnnotation(RecordComponentElement component) {
        Negative annotation = component.getAccessor().getAnnotation(Negative.class);
        if (annotation == null) {
            return null;
        }

        return new ValidationWriter.LessThan(resolveMessage(annotation.message()), 0);
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseNegativeOrZeroAnnotation(RecordComponentElement component) {
        NegativeOrZero annotation = component.getAccessor().getAnnotation(NegativeOrZero.class);
        if (annotation == null) {
            return null;
        }

        return new ValidationWriter.LessThanOrEqual(resolveMessage(annotation.message()), 0);
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parseEmailAnnotation(RecordComponentElement component) {
        Email annotation = component.getAccessor().getAnnotation(Email.class);
        if (annotation == null) {
            return null;
        }

        return new ValidationWriter.Pattern(
                "^[^@]+@[^@]+\\\\.[^@]+$", // TODO: check email validation regex
                resolveMessage(annotation.message())
        );
    }

    public static ValidationWriter.@Nullable NullUnsafeWriter parsePatternAnnotation(RecordComponentElement component) {
        Pattern annotation = component.getAccessor().getAnnotation(Pattern.class);
        if (annotation == null) {
            return null;
        }

        return new ValidationWriter.Pattern(
                annotation.regexp().replace("\\", "\\\\"), // TODO: Check how to prevent escaping
                resolveMessage(annotation.message(), "{regexp}")
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
}
