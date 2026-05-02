package test.jakarta;

import jakarta.validation.constraints.*;

public record MinDoubleRecord(@Min(0) double value) {}
