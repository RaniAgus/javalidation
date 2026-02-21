package test.jakarta;

import jakarta.validation.constraints.*;

public record SizeMaxOnlyRecord(@Size(max = 10) String value) {}
