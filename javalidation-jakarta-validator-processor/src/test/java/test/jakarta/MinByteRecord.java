package test.jakarta;

import jakarta.validation.constraints.*;

public record MinByteRecord(@Min(10) byte value) {}
