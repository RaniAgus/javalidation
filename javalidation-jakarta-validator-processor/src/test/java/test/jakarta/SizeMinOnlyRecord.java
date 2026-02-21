package test.jakarta;

import jakarta.validation.constraints.*;

public record SizeMinOnlyRecord(@Size(min = 1) String value) {}
