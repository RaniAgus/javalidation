package test.jakarta;

import jakarta.validation.constraints.*;

public record MinCharSequenceRecord(@Min(10) String value) {}
