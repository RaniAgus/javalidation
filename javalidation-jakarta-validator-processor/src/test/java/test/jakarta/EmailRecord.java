package test.jakarta;

import jakarta.validation.constraints.*;

public record EmailRecord(@Email String value) {}
