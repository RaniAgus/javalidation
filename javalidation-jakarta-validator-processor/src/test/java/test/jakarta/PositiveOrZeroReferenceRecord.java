package test.jakarta;

import jakarta.validation.constraints.*;

public record PositiveOrZeroReferenceRecord(@PositiveOrZero Integer value) {}
