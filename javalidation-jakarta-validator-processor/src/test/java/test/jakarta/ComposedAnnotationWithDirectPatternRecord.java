package test.jakarta;

import jakarta.validation.constraints.Pattern;

public record ComposedAnnotationWithDirectPatternRecord(
        @Pattern(regexp = "^[0-9]+$") @ComposedAnnotation String value) {}
