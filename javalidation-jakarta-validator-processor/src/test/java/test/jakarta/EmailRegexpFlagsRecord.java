package test.jakarta;

import jakarta.validation.constraints.*;

public record EmailRegexpFlagsRecord(@Email(regexp = ".*example.*", flags = Pattern.Flag.CASE_INSENSITIVE) String value) {}
