package test.jakarta;

import jakarta.validation.constraints.*;

public record PositiveReferenceRecord(@Positive Integer value) {}
