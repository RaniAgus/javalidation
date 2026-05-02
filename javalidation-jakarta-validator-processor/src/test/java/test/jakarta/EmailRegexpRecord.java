package test.jakarta;

import jakarta.validation.constraints.*;

public record EmailRegexpRecord(@Email(regexp = ".*example.*") String value) {}
