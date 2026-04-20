package test.jakarta;

import jakarta.validation.constraints.*;

public record MinShortRecord(@Min(10) short value) {}
