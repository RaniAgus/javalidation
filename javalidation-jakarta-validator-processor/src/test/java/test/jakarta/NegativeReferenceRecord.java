package test.jakarta;

import jakarta.validation.constraints.*;

public record NegativeReferenceRecord(@Negative Integer value) {}
