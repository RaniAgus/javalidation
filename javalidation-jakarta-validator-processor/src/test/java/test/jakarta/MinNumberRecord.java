package test.jakarta;

import jakarta.validation.constraints.*;

public record MinNumberRecord(@Min(10) Number value) {}
