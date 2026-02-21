package test.jakarta;

import jakarta.validation.constraints.*;

public record SizeMinMaxRecord(@Size(min = 1, max = 10) String value) {}
