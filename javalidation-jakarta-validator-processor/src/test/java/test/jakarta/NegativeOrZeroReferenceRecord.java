package test.jakarta;

import jakarta.validation.constraints.*;

public record NegativeOrZeroReferenceRecord(@NegativeOrZero Integer value) {}
