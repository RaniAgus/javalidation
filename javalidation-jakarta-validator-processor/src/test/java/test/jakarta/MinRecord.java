package test.jakarta;

import jakarta.validation.constraints.*;

public record MinRecord(@Min(10) long value) {}
