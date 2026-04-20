package test.jakarta;

import jakarta.validation.constraints.*;

public record PastLongRecord(@Past Long value) {}
