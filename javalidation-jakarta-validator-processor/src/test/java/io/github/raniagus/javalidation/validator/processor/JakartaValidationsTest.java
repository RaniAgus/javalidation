package io.github.raniagus.javalidation.validator.processor;

import com.google.testing.compile.JavaFileObjects;
import io.github.raniagus.javalidation.ValidationErrors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import test.jakarta.*;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

class JakartaValidationsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "NotNullRecord",
            "NotEmptyRecord",
            "NotBlankRecord",
            "SizeMinMaxRecord",
            "SizeMinOnlyRecord",
            "SizeMaxOnlyRecord",
            "SizeCollectionRecord",
            "SizeMapRecord",
            "EmailRecord",
            "MinRecord",
            "MaxReferenceRecord",
            "MaxPrimitiveRecord",
            "PositiveReferenceRecord",
            "PositivePrimitiveRecord",
            "PositiveOrZeroReferenceRecord",
            "PositiveOrZeroPrimitiveRecord",
            "NegativeReferenceRecord",
            "NegativePrimitiveRecord",
            "NegativeOrZeroReferenceRecord",
            "NegativeOrZeroPrimitiveRecord",
            "PastRecord",
            "PastOrPresentRecord",
            "FutureRecord",
            "FutureOrPresentRecord",
            "PatternRecord",
            "DecimalMinInclusiveRecord",
            "DecimalMinExclusiveRecord",
            "DecimalMaxInclusiveRecord",
            "DecimalMaxExclusiveRecord",
            "DigitsRecord",
            "NotNullAndSizeRecord",
            "NotNullAndMinRecord",
    })
    void givenAnnotatedRecords_WhenAnnotationProcessing_ThenGenerateExpectedFiles(String recordName) {
        assertThat(
                javac()
                        .withProcessors(new ValidatorProcessor())
                        .compile(JavaFileObjects.forResource("test/jakarta/" + recordName + ".java")))
                .generatedSourceFile("test.jakarta." + recordName + "Validator")
                .hasSourceEquivalentTo(
                        JavaFileObjects.forResource("test/jakarta/" + recordName + "Validator.java"));
    }


    // ── @NotNull ──────────────────────────────────────────────────────────────
    @Nested
    class NotNull {
        NotNullRecordValidator validator = new NotNullRecordValidator();

        @Test
        void nullValue_hasFieldError() {
            assertThat(validator.validate(new NotNullRecord(null)))
                    .isEqualTo(ValidationErrors.ofField("value", "must not be null"));
        }

        @Test
        void nonNullValue_noErrors() {
            assertThat(validator.validate(new NotNullRecord("hello")))
                    .isEqualTo(ValidationErrors.empty());
        }
    }

    // ── @NotEmpty ─────────────────────────────────────────────────────────────
    @Nested
    class NotEmpty {
        NotEmptyRecordValidator validator = new NotEmptyRecordValidator();

        @Test
        void nullValue_hasFieldError() {
            assertThat(validator.validate(new NotEmptyRecord(null)))
                    .isEqualTo(ValidationErrors.ofField("value", "must not be empty"));
        }

        @Test
        void emptyString_hasFieldError() {
            assertThat(validator.validate(new NotEmptyRecord("")))
                    .isEqualTo(ValidationErrors.ofField("value", "must not be empty"));
        }

        @Test
        void blankString_noErrors() {
            assertThat(validator.validate(new NotEmptyRecord(" ")))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void nonEmptyString_noErrors() {
            assertThat(validator.validate(new NotEmptyRecord("hello")))
                    .isEqualTo(ValidationErrors.empty());
        }
    }

    // ── @NotBlank ─────────────────────────────────────────────────────────────
    @Nested
    class NotBlank {
        NotBlankRecordValidator validator = new NotBlankRecordValidator();

        @Test
        void nullValue_hasFieldError() {
            assertThat(validator.validate(new NotBlankRecord(null)))
                    .isEqualTo(ValidationErrors.ofField("value", "must not be blank"));
        }

        @Test
        void emptyString_hasFieldError() {
            assertThat(validator.validate(new NotBlankRecord("")))
                    .isEqualTo(ValidationErrors.ofField("value", "must not be blank"));
        }

        @Test
        void blankString_hasFieldError() {
            assertThat(validator.validate(new NotBlankRecord("   ")))
                    .isEqualTo(ValidationErrors.ofField("value", "must not be blank"));
        }

        @Test
        void nonBlankString_noErrors() {
            assertThat(validator.validate(new NotBlankRecord("hello")))
                    .isEqualTo(ValidationErrors.empty());
        }
    }

    // ── @Size ─────────────────────────────────────────────────────────────────
    @Nested
    class Size {
        SizeMinMaxRecordValidator validator = new SizeMinMaxRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new SizeMinMaxRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void belowMin_hasFieldError() {
            assertThat(validator.validate(new SizeMinMaxRecord("")))
                    .isEqualTo(ValidationErrors.ofField("value", "size must be between {0} and {1}", 1, 10));
        }

        @Test
        void atMin_noErrors() {
            assertThat(validator.validate(new SizeMinMaxRecord("a")))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void atMax_noErrors() {
            assertThat(validator.validate(new SizeMinMaxRecord("0123456789")))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void aboveMax_hasFieldError() {
            assertThat(validator.validate(new SizeMinMaxRecord("01234567890")))
                    .isEqualTo(ValidationErrors.ofField("value", "size must be between {0} and {1}", 1, 10));
        }
    }

    // ── @Email ────────────────────────────────────────────────────────────────
    @Nested
    class Email {
        EmailRecordValidator validator = new EmailRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new EmailRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void validEmail_noErrors() {
            assertThat(validator.validate(new EmailRecord("user@example.com")))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void missingAt_hasFieldError() {
            assertThat(validator.validate(new EmailRecord("userexample.com")))
                    .isEqualTo(ValidationErrors.ofField("value", "must be a well-formed email address"));
        }

        @Test
        void missingDomain_hasFieldError() {
            assertThat(validator.validate(new EmailRecord("user@")))
                    .isEqualTo(ValidationErrors.ofField("value", "must be a well-formed email address"));
        }
    }

    // ── @Min ──────────────────────────────────────────────────────────────────
    @Nested
    class Min {
        MinRecordValidator validator = new MinRecordValidator();

        @Test
        void belowMin_hasFieldError() {
            assertThat(validator.validate(new MinRecord(9L)))
                    .isEqualTo(ValidationErrors.ofField("value", "must be greater than or equal to {0}", 10));
        }

        @Test
        void atMin_noErrors() {
            assertThat(validator.validate(new MinRecord(10L)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void aboveMin_noErrors() {
            assertThat(validator.validate(new MinRecord(11L)))
                    .isEqualTo(ValidationErrors.empty());
        }
    }

    // ── @Max ──────────────────────────────────────────────────────────────────
    @Nested
    class Max {
        MaxPrimitiveRecordValidator validator = new MaxPrimitiveRecordValidator();

        @Test
        void belowMax_noErrors() {
            assertThat(validator.validate(new MaxPrimitiveRecord(99L)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void atMax_noErrors() {
            assertThat(validator.validate(new MaxPrimitiveRecord(100L)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void aboveMax_hasFieldError() {
            assertThat(validator.validate(new MaxPrimitiveRecord(101L)))
                    .isEqualTo(ValidationErrors.ofField("value", "must be less than or equal to {0}", 100));
        }
    }

    // ── @Positive ─────────────────────────────────────────────────────────────
    @Nested
    class Positive {
        PositivePrimitiveRecordValidator validator = new PositivePrimitiveRecordValidator();

        @Test
        void negative_hasFieldError() {
            assertThat(validator.validate(new PositivePrimitiveRecord(-1L)))
                    .isEqualTo(ValidationErrors.ofField("value", "must be greater than 0"));
        }

        @Test
        void zero_hasFieldError() {
            assertThat(validator.validate(new PositivePrimitiveRecord(0L)))
                    .isEqualTo(ValidationErrors.ofField("value", "must be greater than 0"));
        }

        @Test
        void positive_noErrors() {
            assertThat(validator.validate(new PositivePrimitiveRecord(1L)))
                    .isEqualTo(ValidationErrors.empty());
        }
    }

    // ── @PositiveOrZero ───────────────────────────────────────────────────────
    @Nested
    class PositiveOrZero {
        PositiveOrZeroPrimitiveRecordValidator validator = new PositiveOrZeroPrimitiveRecordValidator();

        @Test
        void negative_hasFieldError() {
            assertThat(validator.validate(new PositiveOrZeroPrimitiveRecord(-1L)))
                    .isEqualTo(ValidationErrors.ofField("value", "must be greater than or equal to 0"));
        }

        @Test
        void zero_noErrors() {
            assertThat(validator.validate(new PositiveOrZeroPrimitiveRecord(0L)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void positive_noErrors() {
            assertThat(validator.validate(new PositiveOrZeroPrimitiveRecord(1L)))
                    .isEqualTo(ValidationErrors.empty());
        }
    }

    // ── @Negative ─────────────────────────────────────────────────────────────
    @Nested
    class Negative {
        NegativePrimitiveRecordValidator validator = new NegativePrimitiveRecordValidator();

        @Test
        void negative_noErrors() {
            assertThat(validator.validate(new NegativePrimitiveRecord(-1L)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void zero_hasFieldError() {
            assertThat(validator.validate(new NegativePrimitiveRecord(0L)))
                    .isEqualTo(ValidationErrors.ofField("value", "must be less than 0"));
        }

        @Test
        void positive_hasFieldError() {
            assertThat(validator.validate(new NegativePrimitiveRecord(1L)))
                    .isEqualTo(ValidationErrors.ofField("value", "must be less than 0"));
        }
    }

    // ── @NegativeOrZero ───────────────────────────────────────────────────────
    @Nested
    class NegativeOrZero {
        NegativeOrZeroPrimitiveRecordValidator validator = new NegativeOrZeroPrimitiveRecordValidator();

        @Test
        void negative_noErrors() {
            assertThat(validator.validate(new NegativeOrZeroPrimitiveRecord(-1L)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void zero_noErrors() {
            assertThat(validator.validate(new NegativeOrZeroPrimitiveRecord(0L)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void positive_hasFieldError() {
            assertThat(validator.validate(new NegativeOrZeroPrimitiveRecord(1L)))
                    .isEqualTo(ValidationErrors.ofField("value", "must be less than or equal to 0"));
        }
    }

    // ── @Past ─────────────────────────────────────────────────────────────────
    @Nested
    class Past {
        PastRecordValidator validator = new PastRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new PastRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void pastInstant_noErrors() {
            assertThat(validator.validate(new PastRecord(Instant.now().minus(Duration.ofDays(1)))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void futureInstant_hasFieldError() {
            assertThat(validator.validate(new PastRecord(Instant.now().plus(Duration.ofDays(60)))))
                    .isEqualTo(ValidationErrors.ofField("value", "must be a past date"));
        }
    }

    // ── @PastOrPresent ────────────────────────────────────────────────────────
    @Nested
    class PastOrPresent {
        PastOrPresentRecordValidator validator = new PastOrPresentRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new PastOrPresentRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void pastInstant_noErrors() {
            assertThat(validator.validate(new PastOrPresentRecord(Instant.now().minus(Duration.ofDays(1)))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void futureInstant_hasFieldError() {
            assertThat(validator.validate(new PastOrPresentRecord(Instant.now().plus(Duration.ofDays(60)))))
                    .isEqualTo(ValidationErrors.ofField("value", "must be a date in the past or in the present"));
        }
    }

    // ── @Future ───────────────────────────────────────────────────────────────
    @Nested
    class Future {
        FutureRecordValidator validator = new FutureRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new FutureRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void futureInstant_noErrors() {
            assertThat(validator.validate(new FutureRecord(Instant.now().plus(Duration.ofDays(60)))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void pastInstant_hasFieldError() {
            assertThat(validator.validate(new FutureRecord(Instant.now().minus(Duration.ofDays(1)))))
                    .isEqualTo(ValidationErrors.ofField("value", "must be a future date"));
        }
    }

    // ── @FutureOrPresent ──────────────────────────────────────────────────────
    @Nested
    class FutureOrPresent {
        FutureOrPresentRecordValidator validator = new FutureOrPresentRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new FutureOrPresentRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void futureInstant_noErrors() {
            assertThat(validator.validate(new FutureOrPresentRecord(Instant.now().plus(Duration.ofDays(60)))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void pastInstant_hasFieldError() {
            assertThat(validator.validate(new FutureOrPresentRecord(Instant.now().minus(Duration.ofDays(1)))))
                    .isEqualTo(ValidationErrors.ofField("value", "must be a date in the present or in the future"));
        }
    }

    // ── @Pattern ──────────────────────────────────────────────────────────────
    @Nested
    class Pattern {
        PatternRecordValidator validator = new PatternRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new PatternRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void matchingValue_noErrors() {
            assertThat(validator.validate(new PatternRecord("hello")))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void nonMatchingValue_hasFieldError() {
            assertThat(validator.validate(new PatternRecord("Hello123")))
                    .isEqualTo(ValidationErrors.ofField("value", "must match \"{0}\"", "^[a-z]+$"));
        }
    }

    // ── @DecimalMin ───────────────────────────────────────────────────────────
    @Nested
    class DecimalMin {
        DecimalMinInclusiveRecordValidator inclusiveValidator = new DecimalMinInclusiveRecordValidator();
        DecimalMinExclusiveRecordValidator exclusiveValidator = new DecimalMinExclusiveRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(inclusiveValidator.validate(new DecimalMinInclusiveRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void inclusive_atMin_noErrors() {
            assertThat(inclusiveValidator.validate(new DecimalMinInclusiveRecord(new java.math.BigDecimal("10.5"))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void inclusive_belowMin_hasFieldError() {
            assertThat(inclusiveValidator.validate(new DecimalMinInclusiveRecord(new java.math.BigDecimal("10.4"))))
                    .isEqualTo(ValidationErrors.ofField("value", "must be greater than or equal to {0}", "10.5"));
        }

        @Test
        void exclusive_atMin_hasFieldError() {
            assertThat(exclusiveValidator.validate(new DecimalMinExclusiveRecord(new java.math.BigDecimal("10.5"))))
                    .isEqualTo(ValidationErrors.ofField("value", "must be greater than {0}", "10.5"));
        }

        @Test
        void exclusive_aboveMin_noErrors() {
            assertThat(exclusiveValidator.validate(new DecimalMinExclusiveRecord(new java.math.BigDecimal("10.6"))))
                    .isEqualTo(ValidationErrors.empty());
        }
    }

    // ── @DecimalMax ───────────────────────────────────────────────────────────
    @Nested
    class DecimalMax {
        DecimalMaxInclusiveRecordValidator inclusiveValidator = new DecimalMaxInclusiveRecordValidator();
        DecimalMaxExclusiveRecordValidator exclusiveValidator = new DecimalMaxExclusiveRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(inclusiveValidator.validate(new DecimalMaxInclusiveRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void inclusive_atMax_noErrors() {
            assertThat(inclusiveValidator.validate(new DecimalMaxInclusiveRecord(new java.math.BigDecimal("10.5"))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void inclusive_aboveMax_hasFieldError() {
            assertThat(inclusiveValidator.validate(new DecimalMaxInclusiveRecord(new java.math.BigDecimal("10.6"))))
                    .isEqualTo(ValidationErrors.ofField("value", "must be less than or equal to {0}", "10.5"));
        }

        @Test
        void exclusive_atMax_hasFieldError() {
            assertThat(exclusiveValidator.validate(new DecimalMaxExclusiveRecord(new java.math.BigDecimal("10.5"))))
                    .isEqualTo(ValidationErrors.ofField("value", "must be less than {0}", "10.5"));
        }

        @Test
        void exclusive_belowMax_noErrors() {
            assertThat(exclusiveValidator.validate(new DecimalMaxExclusiveRecord(new java.math.BigDecimal("10.4"))))
                    .isEqualTo(ValidationErrors.empty());
        }
    }

    // ── @Digits ───────────────────────────────────────────────────────────────
    @Nested
    class Digits {
        DigitsRecordValidator validator = new DigitsRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new DigitsRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void validDigits_noErrors() {
            assertThat(validator.validate(new DigitsRecord(new java.math.BigDecimal("12345.67"))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void tooManyIntegerDigits_hasFieldError() {
            assertThat(validator.validate(new DigitsRecord(new java.math.BigDecimal("123456.7"))))
                    .isEqualTo(ValidationErrors.ofField("value", "numeric value out of bounds ({0} digits, {1} decimal digits expected)", 5, 2));
        }

        @Test
        void tooManyFractionDigits_hasFieldError() {
            assertThat(validator.validate(new DigitsRecord(new java.math.BigDecimal("12345.678"))))
                    .isEqualTo(ValidationErrors.ofField("value", "numeric value out of bounds ({0} digits, {1} decimal digits expected)", 5, 2));
        }
    }
}