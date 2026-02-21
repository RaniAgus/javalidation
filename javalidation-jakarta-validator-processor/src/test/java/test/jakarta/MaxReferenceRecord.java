package test.jakarta;

import jakarta.validation.constraints.*;

public record MaxReferenceRecord(@Max(100) Long value) {}
