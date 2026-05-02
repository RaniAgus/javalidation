package io.github.raniagus.javalidation.validator.processor;

import com.google.testing.compile.JavaFileObjects;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import test.jakarta.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static io.github.raniagus.javalidation.assertj.JavalidationAssertions.assertThat;
import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
            "MinDoubleRecord",
            "MinIntegerRecord",
            "MinShortRecord",
            "MinByteRecord",
            "MinNumberRecord",
            "MinCharSequenceRecord",
            "MaxReferenceRecord",
            "MaxFloatRecord",
            "MaxBigIntegerRecord",
            "MaxBigDecimalRecord",
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
            "PastLocalDateRecord",
            "PastLocalTimeRecord",
            "PastLocalDateTimeRecord",
            "PastOffsetDateTimeRecord",
            "PastOffsetTimeRecord",
            "PastZonedDateTimeRecord",
            "PastYearRecord",
            "PastYearMonthRecord",
            "PastMonthDayRecord",
            "PastDateRecord",
            "PastCalendarRecord",
            "PastLongRecord",
            "PastOrPresentRecord",
            "FutureRecord",
            "FutureOrPresentRecord",
            "PatternRecord",
            "DecimalMinInclusiveRecord",
            "DecimalMinExclusiveRecord",
            "DecimalMaxInclusiveRecord",
            "DecimalMaxExclusiveRecord",
            "DigitsRecord",
            "DigitsPrimitiveRecord",
            "DigitsNumberRecord",
            "DigitsCharSequenceRecord",
            "NotNullAndSizeRecord",
            "NotNullAndMinRecord",
    })
    void givenAnnotatedRecords_whenAnnotationProcessing_thenGeneratesExpectedFiles(String recordName) {
        JavaFileObject recordFile = JavaFileObjects.forResource("test/jakarta/" + recordName + ".java");
        JavaFileObject triggerFile = JavaFileObjects.forSourceString("test.SimpleService", """
                package test;
    
                import jakarta.validation.*;
    
                public class SimpleService {
                    public void doSomething(test.jakarta.@Valid %s input) {}
                }
                """.formatted(recordName)
        );

        assertThat(
                javac()
                        .withProcessors(new ValidatorProcessor())
                        .compile(recordFile, triggerFile))
                .generatedSourceFile("test.jakarta." + recordName + "Validator")
                .hasSourceEquivalentTo(
                        JavaFileObjects.forResource("test/jakarta/" + recordName + "Validator.java"));
    }

    // ── @NotNull ──────────────────────────────────────────────────────────────
    @Nested
    class NotNull {
        NotNullRecordValidator validator = new NotNullRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new NotNullRecord(null)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.NotNull.message");
        }

        @Test
        void givenNonNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new NotNullRecord("hello")))
                    .isEmpty();
        }
    }

    // ── @NotEmpty ─────────────────────────────────────────────────────────────
    @Nested
    class NotEmpty {
        NotEmptyRecordValidator validator = new NotEmptyRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new NotEmptyRecord(null)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.NotEmpty.message");
        }

        @Test
        void givenEmptyString_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new NotEmptyRecord("")))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.NotEmpty.message");
        }

        @Test
        void givenBlankString_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new NotEmptyRecord(" ")))
                    .isEmpty();
        }

        @Test
        void givenNonEmptyString_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new NotEmptyRecord("hello")))
                    .isEmpty();
        }
    }

    // ── @NotBlank ─────────────────────────────────────────────────────────────
    @Nested
    class NotBlank {
        NotBlankRecordValidator validator = new NotBlankRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new NotBlankRecord(null)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.NotBlank.message");
        }

        @Test
        void givenEmptyString_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new NotBlankRecord("")))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.NotBlank.message");
        }

        @Test
        void givenBlankString_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new NotBlankRecord("   ")))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.NotBlank.message");
        }

        @Test
        void givenNonBlankString_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new NotBlankRecord("hello")))
                    .isEmpty();
        }
    }

    // ── @Size ─────────────────────────────────────────────────────────────────
    @Nested
    class Size {
        SizeMinMaxRecordValidator validator = new SizeMinMaxRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new SizeMinMaxRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenBelowMin_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new SizeMinMaxRecord("")))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Size.message", 1, 10);
        }

        @Test
        void givenAtMin_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new SizeMinMaxRecord("a")))
                    .isEmpty();
        }

        @Test
        void givenAtMax_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new SizeMinMaxRecord("0123456789")))
                    .isEmpty();
        }

        @Test
        void givenAboveMax_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new SizeMinMaxRecord("01234567890")))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Size.message", 1, 10);
        }
    }

    // ── @Email ────────────────────────────────────────────────────────────────
    @Nested
    class Email {
        EmailRecordValidator validator = new EmailRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new EmailRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenValidEmail_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new EmailRecord("user@example.com")))
                    .isEmpty();
        }

        @Test
        void givenValidEmailWithSubdomain_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new EmailRecord("user@mail.example.com")))
                    .isEmpty();
        }

        @Test
        void givenValidEmailWithPlusTag_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new EmailRecord("user+tag@example.com")))
                    .isEmpty();
        }

        @Test
        void givenValidEmailWithDots_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new EmailRecord("first.last@example.com")))
                    .isEmpty();
        }

        @Test
        void givenEmptyString_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new EmailRecord("")))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Email.message");
        }

        @Test
        void givenMissingAt_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new EmailRecord("userexample.com")))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Email.message");
        }

        @Test
        void givenMissingLocal_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new EmailRecord("@example.com")))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Email.message");
        }

        @Test
        void givenMissingDomain_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new EmailRecord("user@")))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Email.message");
        }

        @Test
        void givenMultipleAt_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new EmailRecord("user@@example.com")))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Email.message");
        }

        @Test
        void givenSpacesInEmail_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new EmailRecord("user name@example.com")))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Email.message");
        }
    }

    // ── @Min ──────────────────────────────────────────────────────────────────
    @Nested
    class Min {
        MinRecordValidator validator = new MinRecordValidator();

        @Test
        void givenBelowMin_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new MinRecord(9L)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Min.message", 10);
        }

        @Test
        void givenAtMin_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new MinRecord(10L)))
                    .isEmpty();
        }

        @Test
        void givenAboveMin_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new MinRecord(11L)))
                    .isEmpty();
        }
    }

    // ── @Min (Integer primitive) ───────────────────────────────────────────────
    @Nested
    class MinInteger {
        MinIntegerRecordValidator validator = new MinIntegerRecordValidator();

        @Test
        void givenBelowMin_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new MinIntegerRecord(9)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Min.message", 10);
        }

        @Test
        void givenAtMin_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new MinIntegerRecord(10)))
                    .isEmpty();
        }

        @Test
        void givenAboveMin_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new MinIntegerRecord(11)))
                    .isEmpty();
        }
    }

    // ── @Min (Short primitive) ─────────────────────────────────────────────────
    @Nested
    class MinShort {
        MinShortRecordValidator validator = new MinShortRecordValidator();

        @Test
        void givenBelowMin_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new MinShortRecord((short) 9)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Min.message", 10);
        }

        @Test
        void givenAtMin_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new MinShortRecord((short) 10)))
                    .isEmpty();
        }

        @Test
        void givenAboveMin_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new MinShortRecord((short) 11)))
                    .isEmpty();
        }
    }

    // ── @Min (Byte primitive) ──────────────────────────────────────────────────
    @Nested
    class MinByte {
        MinByteRecordValidator validator = new MinByteRecordValidator();

        @Test
        void givenBelowMin_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new MinByteRecord((byte) 9)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Min.message", 10);
        }

        @Test
        void givenAtMin_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new MinByteRecord((byte) 10)))
                    .isEmpty();
        }

        @Test
        void givenAboveMin_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new MinByteRecord((byte) 11)))
                    .isEmpty();
        }
    }

    // ── @Min (Number) ─────────────────────────────────────────────────────────
    @Nested
    class MinNumber {
        MinNumberRecordValidator validator = new MinNumberRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new MinNumberRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenBelowMin_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new MinNumberRecord(9L)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Min.message", 10);
        }

        @Test
        void givenAtMin_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new MinNumberRecord(10L)))
                    .isEmpty();
        }

        @Test
        void givenAboveMin_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new MinNumberRecord(11L)))
                    .isEmpty();
        }
    }

    // ── @Min (CharSequence) ────────────────────────────────────────────────────
    @Nested
    class MinCharSequence {
        MinCharSequenceRecordValidator validator = new MinCharSequenceRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new MinCharSequenceRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenBelowMin_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new MinCharSequenceRecord("9")))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Min.message", 10);
        }

        @Test
        void givenAtMin_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new MinCharSequenceRecord("10")))
                    .isEmpty();
        }

        @Test
        void givenAboveMin_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new MinCharSequenceRecord("11")))
                    .isEmpty();
        }
    }

    // ── @Min on double ────────────────────────────────────────────────────────
    @Nested
    class MinDouble {
        MinDoubleRecordValidator validator = new MinDoubleRecordValidator();

        @Test
        void givenAboveMin_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new MinDoubleRecord(1.5)))
                    .isEmpty();
        }

        @Test
        void givenAtMin_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new MinDoubleRecord(0.0)))
                    .isEmpty();
        }

        @Test
        void givenBelowMin_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new MinDoubleRecord(-0.1)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Min.message", 0);
        }
    }

    // ── @Max ──────────────────────────────────────────────────────────────────
    @Nested
    class Max {
        MaxPrimitiveRecordValidator primitiveValidator = new MaxPrimitiveRecordValidator();
        MaxReferenceRecordValidator referenceValidator = new MaxReferenceRecordValidator();
        MaxBigIntegerRecordValidator bigIntegerValidator = new MaxBigIntegerRecordValidator();
        MaxBigDecimalRecordValidator bigDecimalValidator = new MaxBigDecimalRecordValidator();

        @Test
        void givenBelowPrimitiveMax_whenValidate_thenIsEmpty() {
            assertThat(primitiveValidator.validate(new MaxPrimitiveRecord(99L)))
                    .isEmpty();
        }

        @Test
        void givenAtPrimitiveMax_whenValidate_thenIsEmpty() {
            assertThat(primitiveValidator.validate(new MaxPrimitiveRecord(100L)))
                    .isEmpty();
        }

        @Test
        void givenAbovePrimitiveMax_whenValidate_thenHasFieldError() {
            assertThat(primitiveValidator.validate(new MaxPrimitiveRecord(101L)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Max.message", 100);
        }

        @Test
        void givenBelowReferenceMax_whenValidate_thenIsEmpty() {
            assertThat(referenceValidator.validate(new MaxReferenceRecord(99L)))
                    .isEmpty();
        }

        @Test
        void givenAtReferenceMax_whenValidate_thenIsEmpty() {
            assertThat(referenceValidator.validate(new MaxReferenceRecord(100L)))
                    .isEmpty();
        }

        @Test
        void givenAboveReferenceMax_whenValidate_thenHasFieldError() {
            assertThat(referenceValidator.validate(new MaxReferenceRecord(101L)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Max.message", 100);
        }

        @Test
        void givenBelowBigIntegerMax_whenValidate_thenIsEmpty() {
            assertThat(bigIntegerValidator.validate(new MaxBigIntegerRecord(BigInteger.valueOf(99L))))
                    .isEmpty();
        }

        @Test
        void givenAtBigIntegerMax_whenValidate_thenIsEmpty() {
            assertThat(bigIntegerValidator.validate(new MaxBigIntegerRecord(BigInteger.valueOf(100L))))
                    .isEmpty();
        }

        @Test
        void givenAboveBigIntegerMax_whenValidate_thenHasFieldError() {
            assertThat(bigIntegerValidator.validate(new MaxBigIntegerRecord(BigInteger.valueOf(101L))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Max.message", 100);
        }

        @Test
        void givenBelowBigDecimalMax_whenValidate_thenIsEmpty() {
            assertThat(bigDecimalValidator.validate(new MaxBigDecimalRecord(BigDecimal.valueOf(99L))))
                    .isEmpty();
        }

        @Test
        void givenAtBigDecimalMax_whenValidate_thenIsEmpty() {
            assertThat(bigDecimalValidator.validate(new MaxBigDecimalRecord(BigDecimal.valueOf(100L))))
                    .isEmpty();
        }

        @Test
        void givenAboveBigDecimalMax_whenValidate_thenHasFieldError() {
            assertThat(bigDecimalValidator.validate(new MaxBigDecimalRecord(BigDecimal.valueOf(101L))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Max.message", 100);
        }
    }

    // ── @Max on Float ─────────────────────────────────────────────────────────
    @Nested
    class MaxFloat {
        MaxFloatRecordValidator validator = new MaxFloatRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new MaxFloatRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenAtMax_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new MaxFloatRecord(100f)))
                    .isEmpty();
        }

        @Test
        void givenBelowMax_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new MaxFloatRecord(50.5f)))
                    .isEmpty();
        }

        @Test
        void givenAboveMax_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new MaxFloatRecord(100.1f)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Max.message", 100);
        }
    }

    // ── @Positive ─────────────────────────────────────────────────────────────
    @Nested
    class Positive {
        PositivePrimitiveRecordValidator validator = new PositivePrimitiveRecordValidator();

        @Test
        void givenNegative_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new PositivePrimitiveRecord(-1L)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Positive.message");
        }

        @Test
        void givenZero_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new PositivePrimitiveRecord(0L)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Positive.message");
        }

        @Test
        void givenPositive_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PositivePrimitiveRecord(1L)))
                    .isEmpty();
        }
    }

    // ── @PositiveOrZero ───────────────────────────────────────────────────────
    @Nested
    class PositiveOrZero {
        PositiveOrZeroPrimitiveRecordValidator validator = new PositiveOrZeroPrimitiveRecordValidator();

        @Test
        void givenNegative_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new PositiveOrZeroPrimitiveRecord(-1L)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.PositiveOrZero.message");
        }

        @Test
        void givenZero_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PositiveOrZeroPrimitiveRecord(0L)))
                    .isEmpty();
        }

        @Test
        void givenPositive_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PositiveOrZeroPrimitiveRecord(1L)))
                    .isEmpty();
        }
    }

    // ── @Negative ─────────────────────────────────────────────────────────────
    @Nested
    class Negative {
        NegativePrimitiveRecordValidator validator = new NegativePrimitiveRecordValidator();

        @Test
        void givenNegative_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new NegativePrimitiveRecord(-1L)))
                    .isEmpty();
        }

        @Test
        void givenZero_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new NegativePrimitiveRecord(0L)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Negative.message");
        }

        @Test
        void givenPositive_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new NegativePrimitiveRecord(1L)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Negative.message");
        }
    }

    // ── @NegativeOrZero ───────────────────────────────────────────────────────
    @Nested
    class NegativeOrZero {
        NegativeOrZeroPrimitiveRecordValidator validator = new NegativeOrZeroPrimitiveRecordValidator();

        @Test
        void givenNegative_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new NegativeOrZeroPrimitiveRecord(-1L)))
                    .isEmpty();
        }

        @Test
        void givenZero_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new NegativeOrZeroPrimitiveRecord(0L)))
                    .isEmpty();
        }

        @Test
        void givenPositive_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new NegativeOrZeroPrimitiveRecord(1L)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.NegativeOrZero.message");
        }
    }

    // ── @Past ─────────────────────────────────────────────────────────────────
    @Nested
    class Past {
        PastRecordValidator validator = new PastRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenPastInstant_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastRecord(Instant.now().minus(Duration.ofDays(1)))))
                    .isEmpty();
        }

        @Test
        void givenFutureInstant_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new PastRecord(Instant.now().plus(Duration.ofDays(60)))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Past.message");
        }
    }

    // ── @Past (LocalDate) ──────────────────────────────────────────────────────
    @Nested
    class PastLocalDate {
        PastLocalDateRecordValidator validator = new PastLocalDateRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastLocalDateRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenPastDate_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastLocalDateRecord(LocalDate.now().minusDays(1))))
                    .isEmpty();
        }

        @Test
        void givenFutureDate_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new PastLocalDateRecord(LocalDate.now().plusDays(1))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Past.message");
        }
    }

    // ── @Past (LocalTime) ──────────────────────────────────────────────────────
    @Nested
    class PastLocalTime {
        PastLocalTimeRecordValidator validator = new PastLocalTimeRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastLocalTimeRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenPastTime_whenValidate_thenIsEmpty() {
            assumeTrue(LocalTime.now().isAfter(LocalTime.of(1, 0)), "Skipping: too close to midnight");
            assertThat(validator.validate(new PastLocalTimeRecord(LocalTime.MIDNIGHT)))
                    .isEmpty();
        }

        @Test
        void givenFutureTime_whenValidate_thenHasFieldError() {
            assumeTrue(LocalTime.now().isBefore(LocalTime.of(23, 0)), "Skipping: too close to end of day");
            assertThat(validator.validate(new PastLocalTimeRecord(LocalTime.MAX)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Past.message");
        }
    }

    // ── @Past (LocalDateTime) ──────────────────────────────────────────────────
    @Nested
    class PastLocalDateTime {
        PastLocalDateTimeRecordValidator validator = new PastLocalDateTimeRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastLocalDateTimeRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenPastDateTime_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastLocalDateTimeRecord(LocalDateTime.now().minusDays(1))))
                    .isEmpty();
        }

        @Test
        void givenFutureDateTime_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new PastLocalDateTimeRecord(LocalDateTime.now().plusDays(1))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Past.message");
        }
    }

    // ── @Past (OffsetDateTime) ─────────────────────────────────────────────────
    @Nested
    class PastOffsetDateTime {
        PastOffsetDateTimeRecordValidator validator = new PastOffsetDateTimeRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastOffsetDateTimeRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenPastDateTime_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastOffsetDateTimeRecord(OffsetDateTime.now().minusDays(1))))
                    .isEmpty();
        }

        @Test
        void givenFutureDateTime_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new PastOffsetDateTimeRecord(OffsetDateTime.now().plusDays(1))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Past.message");
        }
    }

    // ── @Past (OffsetTime) ─────────────────────────────────────────────────────
    @Nested
    class PastOffsetTime {
        PastOffsetTimeRecordValidator validator = new PastOffsetTimeRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastOffsetTimeRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenPastTime_whenValidate_thenIsEmpty() {
            assumeTrue(OffsetTime.now().isAfter(OffsetTime.of(LocalTime.of(1, 0), ZoneOffset.UTC)), "Skipping: too close to midnight UTC");
            assertThat(validator.validate(new PastOffsetTimeRecord(OffsetTime.of(LocalTime.MIDNIGHT, ZoneOffset.UTC))))
                    .isEmpty();
        }

        @Test
        void givenFutureTime_whenValidate_thenHasFieldError() {
            assumeTrue(OffsetTime.now().isBefore(OffsetTime.of(LocalTime.of(23, 0), ZoneOffset.UTC)), "Skipping: too close to end of day UTC");
            assertThat(validator.validate(new PastOffsetTimeRecord(OffsetTime.of(LocalTime.MAX, ZoneOffset.UTC))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Past.message");
        }
    }

    // ── @Past (ZonedDateTime) ──────────────────────────────────────────────────
    @Nested
    class PastZonedDateTime {
        PastZonedDateTimeRecordValidator validator = new PastZonedDateTimeRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastZonedDateTimeRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenPastDateTime_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastZonedDateTimeRecord(ZonedDateTime.now().minusDays(1))))
                    .isEmpty();
        }

        @Test
        void givenFutureDateTime_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new PastZonedDateTimeRecord(ZonedDateTime.now().plusDays(1))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Past.message");
        }
    }

    // ── @Past (Year) ──────────────────────────────────────────────────────────
    @Nested
    class PastYear {
        PastYearRecordValidator validator = new PastYearRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastYearRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenPastYear_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastYearRecord(Year.now().minusYears(1))))
                    .isEmpty();
        }

        @Test
        void givenFutureYear_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new PastYearRecord(Year.now().plusYears(1))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Past.message");
        }
    }

    // ── @Past (YearMonth) ──────────────────────────────────────────────────────
    @Nested
    class PastYearMonth {
        PastYearMonthRecordValidator validator = new PastYearMonthRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastYearMonthRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenPastMonth_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastYearMonthRecord(YearMonth.now().minusMonths(1))))
                    .isEmpty();
        }

        @Test
        void givenFutureMonth_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new PastYearMonthRecord(YearMonth.now().plusMonths(1))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Past.message");
        }
    }

    // ── @Past (MonthDay) ───────────────────────────────────────────────────────
    @Nested
    class PastMonthDay {
        PastMonthDayRecordValidator validator = new PastMonthDayRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastMonthDayRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenPastMonthDay_whenValidate_thenIsEmpty() {
            assumeTrue(MonthDay.now().isAfter(MonthDay.of(1, 1)), "Skipping: it's Jan 1");
            assertThat(validator.validate(new PastMonthDayRecord(MonthDay.of(1, 1))))
                    .isEmpty();
        }

        @Test
        void givenFutureMonthDay_whenValidate_thenHasFieldError() {
            assumeTrue(MonthDay.now().isBefore(MonthDay.of(12, 31)), "Skipping: it's Dec 31");
            assertThat(validator.validate(new PastMonthDayRecord(MonthDay.of(12, 31))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Past.message");
        }
    }

    // ── @Past (java.util.Date) ────────────────────────────────────────────────
    @Nested
    class PastDate {
        PastDateRecordValidator validator = new PastDateRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastDateRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenPastDate_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastDateRecord(new Date(System.currentTimeMillis() - 86400000))))
                    .isEmpty();
        }

        @Test
        void givenFutureDate_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new PastDateRecord(new Date(System.currentTimeMillis() + 5184000000L))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Past.message");
        }
    }

    // ── @Past (java.util.Calendar) ────────────────────────────────────────────
    @Nested
    class PastCalendar {
        PastCalendarRecordValidator validator = new PastCalendarRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastCalendarRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenPastDate_whenValidate_thenIsEmpty() {
            Calendar pastCal = Calendar.getInstance();
            pastCal.add(Calendar.DAY_OF_MONTH, -1);
            assertThat(validator.validate(new PastCalendarRecord(pastCal)))
                    .isEmpty();
        }

        @Test
        void givenFutureDate_whenValidate_thenHasFieldError() {
            Calendar futureCal = Calendar.getInstance();
            futureCal.add(Calendar.DAY_OF_MONTH, 1);
            assertThat(validator.validate(new PastCalendarRecord(futureCal)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Past.message");
        }
    }

    // ── @Past (Long - epoch millis) ────────────────────────────────────────────
    @Nested
    class PastLong {
        PastLongRecordValidator validator = new PastLongRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastLongRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenPastTimestamp_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastLongRecord(System.currentTimeMillis() - 86400000)))
                    .isEmpty();
        }

        @Test
        void givenFutureTimestamp_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new PastLongRecord(System.currentTimeMillis() + 5184000000L)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Past.message");
        }
    }

    // ── @PastOrPresent ────────────────────────────────────────────────────────
    @Nested
    class PastOrPresent {
        PastOrPresentRecordValidator validator = new PastOrPresentRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastOrPresentRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenPastInstant_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PastOrPresentRecord(Instant.now().minus(Duration.ofDays(1)))))
                    .isEmpty();
        }

        @Test
        void givenFutureInstant_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new PastOrPresentRecord(Instant.now().plus(Duration.ofDays(60)))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.PastOrPresent.message");
        }
    }

    // ── @Future ───────────────────────────────────────────────────────────────
    @Nested
    class Future {
        FutureRecordValidator validator = new FutureRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new FutureRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenFutureInstant_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new FutureRecord(Instant.now().plus(Duration.ofDays(60)))))
                    .isEmpty();
        }

        @Test
        void givenPastInstant_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new FutureRecord(Instant.now().minus(Duration.ofDays(1)))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Future.message");
        }
    }

    // ── @FutureOrPresent ──────────────────────────────────────────────────────
    @Nested
    class FutureOrPresent {
        FutureOrPresentRecordValidator validator = new FutureOrPresentRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new FutureOrPresentRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenFutureInstant_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new FutureOrPresentRecord(Instant.now().plus(Duration.ofDays(60)))))
                    .isEmpty();
        }

        @Test
        void givenPastInstant_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new FutureOrPresentRecord(Instant.now().minus(Duration.ofDays(1)))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.FutureOrPresent.message");
        }
    }

    // ── @Pattern ──────────────────────────────────────────────────────────────
    @Nested
    class Pattern {
        PatternRecordValidator validator = new PatternRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PatternRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenMatchingValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new PatternRecord("hello")))
                    .isEmpty();
        }

        @Test
        void givenNonMatchingValue_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new PatternRecord("Hello123")))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Pattern.message", "^[a-z]+$");
        }
    }

    // ── @DecimalMin ───────────────────────────────────────────────────────────
    @Nested
    class DecimalMin {
        DecimalMinInclusiveRecordValidator inclusiveValidator = new DecimalMinInclusiveRecordValidator();
        DecimalMinExclusiveRecordValidator exclusiveValidator = new DecimalMinExclusiveRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(inclusiveValidator.validate(new DecimalMinInclusiveRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenInclusive_whenValidate_thenAtMin_noErrors() {
            assertThat(inclusiveValidator.validate(new DecimalMinInclusiveRecord(new BigDecimal("10.5"))))
                    .isEmpty();
        }

        @Test
        void givenInclusive_whenValidate_thenBelowMin_hasFieldError() {
            assertThat(inclusiveValidator.validate(new DecimalMinInclusiveRecord(new BigDecimal("10.4"))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.DecimalMin.message", "10.5");
        }

        @Test
        void givenExclusive_whenValidate_thenAtMin_hasFieldError() {
            assertThat(exclusiveValidator.validate(new DecimalMinExclusiveRecord(new BigDecimal("10.5"))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.DecimalMin.exclusive.message", "10.5");
        }

        @Test
        void givenExclusive_whenValidate_thenAboveMin_noErrors() {
            assertThat(exclusiveValidator.validate(new DecimalMinExclusiveRecord(new BigDecimal("10.6"))))
                    .isEmpty();
        }
    }

    // ── @DecimalMax ───────────────────────────────────────────────────────────
    @Nested
    class DecimalMax {
        DecimalMaxInclusiveRecordValidator inclusiveValidator = new DecimalMaxInclusiveRecordValidator();
        DecimalMaxExclusiveRecordValidator exclusiveValidator = new DecimalMaxExclusiveRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(inclusiveValidator.validate(new DecimalMaxInclusiveRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenInclusive_whenValidate_thenAtMax_noErrors() {
            assertThat(inclusiveValidator.validate(new DecimalMaxInclusiveRecord(new BigDecimal("10.5"))))
                    .isEmpty();
        }

        @Test
        void givenInclusive_whenValidate_thenAboveMax_hasFieldError() {
            assertThat(inclusiveValidator.validate(new DecimalMaxInclusiveRecord(new BigDecimal("10.6"))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.DecimalMax.message", "10.5");
        }

        @Test
        void givenExclusive_whenValidate_thenAtMax_hasFieldError() {
            assertThat(exclusiveValidator.validate(new DecimalMaxExclusiveRecord(new BigDecimal("10.5"))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.DecimalMax.exclusive.message", "10.5");
        }

        @Test
        void givenExclusive_whenValidate_thenBelowMax_noErrors() {
            assertThat(exclusiveValidator.validate(new DecimalMaxExclusiveRecord(new BigDecimal("10.4"))))
                    .isEmpty();
        }
    }

    // ── @Digits ───────────────────────────────────────────────────────────────
    @Nested
    class Digits {
        DigitsRecordValidator validator = new DigitsRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new DigitsRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenValidDigits_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new DigitsRecord(new BigDecimal("12345.67"))))
                    .isEmpty();
        }

        @Test
        void givenTooManyIntegerDigits_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new DigitsRecord(new BigDecimal("123456.7"))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Digits.message", 5, 2);
        }

        @Test
        void givenTooManyFractionDigits_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new DigitsRecord(new BigDecimal("12345.678"))))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Digits.message", 5, 2);
        }
    }

    @Nested
    class DigitsCharSequence {
        DigitsCharSequenceRecordValidator validator = new DigitsCharSequenceRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new DigitsCharSequenceRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenValidIntegerOnly_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new DigitsCharSequenceRecord("12345")))
                    .isEmpty();
        }

        @Test
        void givenValidDecimal_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new DigitsCharSequenceRecord("12345.67")))
                    .isEmpty();
        }

        @Test
        void givenNegativeValidDecimal_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new DigitsCharSequenceRecord("-12345.67")))
                    .isEmpty();
        }

        @Test
        void givenTooManyIntegerDigits_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new DigitsCharSequenceRecord("123456.7")))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Digits.message", 5, 2);
        }

        @Test
        void givenTooManyFractionDigits_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new DigitsCharSequenceRecord("12345.678")))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Digits.message", 5, 2);
        }

        @Test
        void givenNonNumericValue_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new DigitsCharSequenceRecord("abc")))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Digits.message", 5, 2);
        }
    }

    // ── @Digits (Primitive Integer) ────────────────────────────────────────────
    @Nested
    class DigitsPrimitive {
        DigitsPrimitiveRecordValidator validator = new DigitsPrimitiveRecordValidator();

        @Test
        void givenValidDigits_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new DigitsPrimitiveRecord(12345)))
                    .isEmpty();
        }

        @Test
        void givenTooManyIntegerDigits_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new DigitsPrimitiveRecord(123456)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Digits.message", 5, 2);
        }

        @Test
        void givenZeroValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new DigitsPrimitiveRecord(0)))
                    .isEmpty();
        }

        @Test
        void givenNegativeValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new DigitsPrimitiveRecord(-12345)))
                    .isEmpty();
        }
    }

    // ── @Digits (Number) ──────────────────────────────────────────────────────
    @Nested
    class DigitsNumber {
        DigitsNumberRecordValidator validator = new DigitsNumberRecordValidator();

        @Test
        void givenValidDigits_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new DigitsNumberRecord(12345L)))
                    .isEmpty();
        }

        @Test
        void givenTooManyIntegerDigits_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new DigitsNumberRecord(123456L)))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Digits.message", 5, 2);
        }

        @Test
        void givenZeroValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new DigitsNumberRecord(0L)))
                    .isEmpty();
        }

        @Test
        void givenNegativeValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new DigitsNumberRecord(-12345L)))
                    .isEmpty();
        }
    }

    // ── @Size (Collection) ────────────────────────────────────────────────────
    @Nested
    class SizeCollection {
        SizeCollectionRecordValidator validator = new SizeCollectionRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new SizeCollectionRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenBelowMin_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new SizeCollectionRecord(List.of())))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Size.message", 1, 10);
        }

        @Test
        void givenAtMin_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new SizeCollectionRecord(List.of("a"))))
                    .isEmpty();
        }

        @Test
        void givenAtMax_whenValidate_thenIsEmpty() {
            SizeCollectionRecord record = new SizeCollectionRecord(
                    List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));

            assertThat(validator.validate(record))
                    .isEmpty();
        }

        @Test
        void givenAboveMax_whenValidate_thenHasFieldError() {
            SizeCollectionRecord record = new SizeCollectionRecord(
                    List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"));

            assertThat(validator.validate(record))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Size.message", 1, 10);
        }
    }

    // ── @Size (Map) ───────────────────────────────────────────────────────────
    @Nested
    class SizeMap {
        SizeMapRecordValidator validator = new SizeMapRecordValidator();

        @Test
        void givenNullValue_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new SizeMapRecord(null)))
                    .isEmpty();
        }

        @Test
        void givenBelowMin_whenValidate_thenHasFieldError() {
            assertThat(validator.validate(new SizeMapRecord(Map.of()))).hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Size.message", 1, 10);
        }

        @Test
        void givenAtMin_whenValidate_thenIsEmpty() {
            assertThat(validator.validate(new SizeMapRecord(Map.of("a", "1"))))
                    .isEmpty();
        }

        @Test
        void givenAtMax_whenValidate_thenIsEmpty() {
            SizeMapRecord record = new SizeMapRecord(Map.ofEntries(
                    Map.entry("1", "a"), Map.entry("2", "b"), Map.entry("3", "c"), Map.entry("4", "d"),
                    Map.entry("5", "e"), Map.entry("6", "f"), Map.entry("7", "g"), Map.entry("8", "h"),
                    Map.entry("9", "i"), Map.entry("10", "j")));

            assertThat(validator.validate(record))
                    .isEmpty();
        }

        @Test
        void givenAboveMax_whenValidate_thenHasFieldError() {
            SizeMapRecord record = new SizeMapRecord(Map.ofEntries(
                    Map.entry("1", "a"), Map.entry("2", "b"), Map.entry("3", "c"), Map.entry("4", "d"),
                    Map.entry("5", "e"), Map.entry("6", "f"), Map.entry("7", "g"), Map.entry("8", "h"),
                    Map.entry("9", "i"), Map.entry("10", "j"), Map.entry("11", "k")));

            assertThat(validator.validate(record))
                    .hasErrorCount(1)
                    .hasFieldError("value", "io.github.raniagus.javalidation.constraints.Size.message", 1, 10);
        }
    }
}