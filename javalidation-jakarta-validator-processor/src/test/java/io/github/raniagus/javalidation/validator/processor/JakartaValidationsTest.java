package io.github.raniagus.javalidation.validator.processor;

import com.google.testing.compile.JavaFileObjects;
import io.github.raniagus.javalidation.ValidationErrors;
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

import static org.assertj.core.api.Assertions.assertThat;
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
            "MinIntegerRecord",
            "MinShortRecord",
            "MinByteRecord",
            "MinNumberRecord",
            "MinCharSequenceRecord",
            "MaxReferenceRecord",
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
    void givenAnnotatedRecords_WhenAnnotationProcessing_ThenGenerateExpectedFiles(String recordName) {
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
        void nullValue_hasFieldError() {
            assertThat(validator.validate(new NotNullRecord(null)))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.NotNull.message"));
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
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.NotEmpty.message"));
        }

        @Test
        void emptyString_hasFieldError() {
            assertThat(validator.validate(new NotEmptyRecord("")))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.NotEmpty.message"));
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
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.NotBlank.message"));
        }

        @Test
        void emptyString_hasFieldError() {
            assertThat(validator.validate(new NotBlankRecord("")))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.NotBlank.message"));
        }

        @Test
        void blankString_hasFieldError() {
            assertThat(validator.validate(new NotBlankRecord("   ")))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.NotBlank.message"));
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
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Size.message", 1, 10));
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
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Size.message", 1, 10));
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
        void validEmailWithSubdomain_noErrors() {
            assertThat(validator.validate(new EmailRecord("user@mail.example.com")))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void validEmailWithPlusTag_noErrors() {
            assertThat(validator.validate(new EmailRecord("user+tag@example.com")))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void validEmailWithDots_noErrors() {
            assertThat(validator.validate(new EmailRecord("first.last@example.com")))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void emptyString_hasFieldError() {
            assertThat(validator.validate(new EmailRecord("")))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Email.message"));
        }

        @Test
        void missingAt_hasFieldError() {
            assertThat(validator.validate(new EmailRecord("userexample.com")))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Email.message"));
        }

        @Test
        void missingLocal_hasFieldError() {
            assertThat(validator.validate(new EmailRecord("@example.com")))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Email.message"));
        }

        @Test
        void missingDomain_hasFieldError() {
            assertThat(validator.validate(new EmailRecord("user@")))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Email.message"));
        }

        @Test
        void multipleAt_hasFieldError() {
            assertThat(validator.validate(new EmailRecord("user@@example.com")))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Email.message"));
        }

        @Test
        void spacesInEmail_hasFieldError() {
            assertThat(validator.validate(new EmailRecord("user name@example.com")))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Email.message"));
        }
    }

    // ── @Min ──────────────────────────────────────────────────────────────────
    @Nested
    class Min {
        MinRecordValidator validator = new MinRecordValidator();

        @Test
        void belowMin_hasFieldError() {
            assertThat(validator.validate(new MinRecord(9L)))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Min.message", 10));
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

    // ── @Min (Integer primitive) ───────────────────────────────────────────────
    @Nested
    class MinInteger {
        MinIntegerRecordValidator validator = new MinIntegerRecordValidator();

        @Test
        void belowMin_hasFieldError() {
            assertThat(validator.validate(new MinIntegerRecord(9)))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Min.message", 10));
        }

        @Test
        void atMin_noErrors() {
            assertThat(validator.validate(new MinIntegerRecord(10)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void aboveMin_noErrors() {
            assertThat(validator.validate(new MinIntegerRecord(11)))
                    .isEqualTo(ValidationErrors.empty());
        }
    }

    // ── @Min (Short primitive) ─────────────────────────────────────────────────
    @Nested
    class MinShort {
        MinShortRecordValidator validator = new MinShortRecordValidator();

        @Test
        void belowMin_hasFieldError() {
            assertThat(validator.validate(new MinShortRecord((short) 9)))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Min.message", 10));
        }

        @Test
        void atMin_noErrors() {
            assertThat(validator.validate(new MinShortRecord((short) 10)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void aboveMin_noErrors() {
            assertThat(validator.validate(new MinShortRecord((short) 11)))
                    .isEqualTo(ValidationErrors.empty());
        }
    }

    // ── @Min (Byte primitive) ──────────────────────────────────────────────────
    @Nested
    class MinByte {
        MinByteRecordValidator validator = new MinByteRecordValidator();

        @Test
        void belowMin_hasFieldError() {
            assertThat(validator.validate(new MinByteRecord((byte) 9)))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Min.message", 10));
        }

        @Test
        void atMin_noErrors() {
            assertThat(validator.validate(new MinByteRecord((byte) 10)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void aboveMin_noErrors() {
            assertThat(validator.validate(new MinByteRecord((byte) 11)))
                    .isEqualTo(ValidationErrors.empty());
        }
    }

    // ── @Min (Number) ─────────────────────────────────────────────────────────
    @Nested
    class MinNumber {
        MinNumberRecordValidator validator = new MinNumberRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new MinNumberRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void belowMin_hasFieldError() {
            assertThat(validator.validate(new MinNumberRecord(9L)))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Min.message", 10));
        }

        @Test
        void atMin_noErrors() {
            assertThat(validator.validate(new MinNumberRecord(10L)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void aboveMin_noErrors() {
            assertThat(validator.validate(new MinNumberRecord(11L)))
                    .isEqualTo(ValidationErrors.empty());
        }
    }

    // ── @Min (CharSequence) ────────────────────────────────────────────────────
    @Nested
    class MinCharSequence {
        MinCharSequenceRecordValidator validator = new MinCharSequenceRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new MinCharSequenceRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void belowMin_hasFieldError() {
            assertThat(validator.validate(new MinCharSequenceRecord("9")))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Min.message", 10));
        }

        @Test
        void atMin_noErrors() {
            assertThat(validator.validate(new MinCharSequenceRecord("10")))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void aboveMin_noErrors() {
            assertThat(validator.validate(new MinCharSequenceRecord("11")))
                    .isEqualTo(ValidationErrors.empty());
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
        void belowPrimitiveMax_noErrors() {
            assertThat(primitiveValidator.validate(new MaxPrimitiveRecord(99L)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void atPrimitiveMax_noErrors() {
            assertThat(primitiveValidator.validate(new MaxPrimitiveRecord(100L)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void abovePrimitiveMax_hasFieldError() {
            assertThat(primitiveValidator.validate(new MaxPrimitiveRecord(101L)))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Max.message", 100));
        }

        @Test
        void belowReferenceMax_noErrors() {
            assertThat(referenceValidator.validate(new MaxReferenceRecord(99L)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void atReferenceMax_noErrors() {
            assertThat(referenceValidator.validate(new MaxReferenceRecord(100L)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void aboveReferenceMax_hasFieldError() {
            assertThat(referenceValidator.validate(new MaxReferenceRecord(101L)))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Max.message", 100));
        }

        @Test
        void belowBigIntegerMax_noErrors() {
            assertThat(bigIntegerValidator.validate(new MaxBigIntegerRecord(BigInteger.valueOf(99L))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void atBigIntegerMax_noErrors() {
            assertThat(bigIntegerValidator.validate(new MaxBigIntegerRecord(BigInteger.valueOf(100L))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void aboveBigIntegerMax_hasFieldError() {
            assertThat(bigIntegerValidator.validate(new MaxBigIntegerRecord(BigInteger.valueOf(101L))))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Max.message", 100));
        }

        @Test
        void belowBigDecimalMax_noErrors() {
            assertThat(bigDecimalValidator.validate(new MaxBigDecimalRecord(BigDecimal.valueOf(99L))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void atBigDecimalMax_noErrors() {
            assertThat(bigDecimalValidator.validate(new MaxBigDecimalRecord(BigDecimal.valueOf(100L))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void aboveBigDecimalMax_hasFieldError() {
            assertThat(bigDecimalValidator.validate(new MaxBigDecimalRecord(BigDecimal.valueOf(101L))))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Max.message", 100));
        }
    }

    // ── @Positive ─────────────────────────────────────────────────────────────
    @Nested
    class Positive {
        PositivePrimitiveRecordValidator validator = new PositivePrimitiveRecordValidator();

        @Test
        void negative_hasFieldError() {
            assertThat(validator.validate(new PositivePrimitiveRecord(-1L)))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Positive.message"));
        }

        @Test
        void zero_hasFieldError() {
            assertThat(validator.validate(new PositivePrimitiveRecord(0L)))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Positive.message"));
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
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.PositiveOrZero.message"));
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
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Negative.message"));
        }

        @Test
        void positive_hasFieldError() {
            assertThat(validator.validate(new NegativePrimitiveRecord(1L)))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Negative.message"));
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
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.NegativeOrZero.message"));
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
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Past.message"));
        }
    }

    // ── @Past (LocalDate) ──────────────────────────────────────────────────────
    @Nested
    class PastLocalDate {
        PastLocalDateRecordValidator validator = new PastLocalDateRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new PastLocalDateRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void pastDate_noErrors() {
            assertThat(validator.validate(new PastLocalDateRecord(LocalDate.now().minusDays(1))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void futureDate_hasFieldError() {
            assertThat(validator.validate(new PastLocalDateRecord(LocalDate.now().plusDays(1))))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Past.message"));
        }
    }

    // ── @Past (LocalTime) ──────────────────────────────────────────────────────
    @Nested
    class PastLocalTime {
        PastLocalTimeRecordValidator validator = new PastLocalTimeRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new PastLocalTimeRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void pastTime_noErrors() {
            assumeTrue(LocalTime.now().isAfter(LocalTime.of(1, 0)), "Skipping: too close to midnight");
            assertThat(validator.validate(new PastLocalTimeRecord(LocalTime.MIDNIGHT)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void futureTime_hasFieldError() {
            assumeTrue(LocalTime.now().isBefore(LocalTime.of(23, 0)), "Skipping: too close to end of day");
            assertThat(validator.validate(new PastLocalTimeRecord(LocalTime.MAX)))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Past.message"));
        }
    }

    // ── @Past (LocalDateTime) ──────────────────────────────────────────────────
    @Nested
    class PastLocalDateTime {
        PastLocalDateTimeRecordValidator validator = new PastLocalDateTimeRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new PastLocalDateTimeRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void pastDateTime_noErrors() {
            assertThat(validator.validate(new PastLocalDateTimeRecord(LocalDateTime.now().minusDays(1))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void futureDateTime_hasFieldError() {
            assertThat(validator.validate(new PastLocalDateTimeRecord(LocalDateTime.now().plusDays(1))))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Past.message"));
        }
    }

    // ── @Past (OffsetDateTime) ─────────────────────────────────────────────────
    @Nested
    class PastOffsetDateTime {
        PastOffsetDateTimeRecordValidator validator = new PastOffsetDateTimeRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new PastOffsetDateTimeRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void pastDateTime_noErrors() {
            assertThat(validator.validate(new PastOffsetDateTimeRecord(OffsetDateTime.now().minusDays(1))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void futureDateTime_hasFieldError() {
            assertThat(validator.validate(new PastOffsetDateTimeRecord(OffsetDateTime.now().plusDays(1))))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Past.message"));
        }
    }

    // ── @Past (OffsetTime) ─────────────────────────────────────────────────────
    @Nested
    class PastOffsetTime {
        PastOffsetTimeRecordValidator validator = new PastOffsetTimeRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new PastOffsetTimeRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void pastTime_noErrors() {
            assumeTrue(OffsetTime.now().isAfter(OffsetTime.of(LocalTime.of(1, 0), ZoneOffset.UTC)), "Skipping: too close to midnight UTC");
            assertThat(validator.validate(new PastOffsetTimeRecord(OffsetTime.of(LocalTime.MIDNIGHT, ZoneOffset.UTC))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void futureTime_hasFieldError() {
            assumeTrue(OffsetTime.now().isBefore(OffsetTime.of(LocalTime.of(23, 0), ZoneOffset.UTC)), "Skipping: too close to end of day UTC");
            assertThat(validator.validate(new PastOffsetTimeRecord(OffsetTime.of(LocalTime.MAX, ZoneOffset.UTC))))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Past.message"));
        }
    }

    // ── @Past (ZonedDateTime) ──────────────────────────────────────────────────
    @Nested
    class PastZonedDateTime {
        PastZonedDateTimeRecordValidator validator = new PastZonedDateTimeRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new PastZonedDateTimeRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void pastDateTime_noErrors() {
            assertThat(validator.validate(new PastZonedDateTimeRecord(ZonedDateTime.now().minusDays(1))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void futureDateTime_hasFieldError() {
            assertThat(validator.validate(new PastZonedDateTimeRecord(ZonedDateTime.now().plusDays(1))))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Past.message"));
        }
    }

    // ── @Past (Year) ──────────────────────────────────────────────────────────
    @Nested
    class PastYear {
        PastYearRecordValidator validator = new PastYearRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new PastYearRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void pastYear_noErrors() {
            assertThat(validator.validate(new PastYearRecord(Year.now().minusYears(1))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void futureYear_hasFieldError() {
            assertThat(validator.validate(new PastYearRecord(Year.now().plusYears(1))))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Past.message"));
        }
    }

    // ── @Past (YearMonth) ──────────────────────────────────────────────────────
    @Nested
    class PastYearMonth {
        PastYearMonthRecordValidator validator = new PastYearMonthRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new PastYearMonthRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void pastMonth_noErrors() {
            assertThat(validator.validate(new PastYearMonthRecord(YearMonth.now().minusMonths(1))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void futureMonth_hasFieldError() {
            assertThat(validator.validate(new PastYearMonthRecord(YearMonth.now().plusMonths(1))))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Past.message"));
        }
    }

    // ── @Past (MonthDay) ───────────────────────────────────────────────────────
    @Nested
    class PastMonthDay {
        PastMonthDayRecordValidator validator = new PastMonthDayRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new PastMonthDayRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void pastMonthDay_noErrors() {
            assumeTrue(MonthDay.now().isAfter(MonthDay.of(1, 1)), "Skipping: it's Jan 1");
            assertThat(validator.validate(new PastMonthDayRecord(MonthDay.of(1, 1))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void futureMonthDay_hasFieldError() {
            assumeTrue(MonthDay.now().isBefore(MonthDay.of(12, 31)), "Skipping: it's Dec 31");
            assertThat(validator.validate(new PastMonthDayRecord(MonthDay.of(12, 31))))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Past.message"));
        }
    }

    // ── @Past (java.util.Date) ────────────────────────────────────────────────
    @Nested
    class PastDate {
        PastDateRecordValidator validator = new PastDateRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new PastDateRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void pastDate_noErrors() {
            assertThat(validator.validate(new PastDateRecord(new Date(System.currentTimeMillis() - 86400000))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void futureDate_hasFieldError() {
            assertThat(validator.validate(new PastDateRecord(new Date(System.currentTimeMillis() + 5184000000L))))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Past.message"));
        }
    }

    // ── @Past (java.util.Calendar) ────────────────────────────────────────────
    @Nested
    class PastCalendar {
        PastCalendarRecordValidator validator = new PastCalendarRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new PastCalendarRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void pastDate_noErrors() {
            Calendar pastCal = Calendar.getInstance();
            pastCal.add(Calendar.DAY_OF_MONTH, -1);
            assertThat(validator.validate(new PastCalendarRecord(pastCal)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void futureDate_hasFieldError() {
            Calendar futureCal = Calendar.getInstance();
            futureCal.add(Calendar.DAY_OF_MONTH, 1);
            assertThat(validator.validate(new PastCalendarRecord(futureCal)))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Past.message"));
        }
    }

    // ── @Past (Long - epoch millis) ────────────────────────────────────────────
    @Nested
    class PastLong {
        PastLongRecordValidator validator = new PastLongRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new PastLongRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void pastTimestamp_noErrors() {
            assertThat(validator.validate(new PastLongRecord(System.currentTimeMillis() - 86400000)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void futureTimestamp_hasFieldError() {
            assertThat(validator.validate(new PastLongRecord(System.currentTimeMillis() + 5184000000L)))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Past.message"));
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
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.PastOrPresent.message"));
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
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Future.message"));
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
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.FutureOrPresent.message"));
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
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Pattern.message", "^[a-z]+$"));
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
            assertThat(inclusiveValidator.validate(new DecimalMinInclusiveRecord(new BigDecimal("10.5"))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void inclusive_belowMin_hasFieldError() {
            assertThat(inclusiveValidator.validate(new DecimalMinInclusiveRecord(new BigDecimal("10.4"))))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.DecimalMin.message", "10.5"));
        }

        @Test
        void exclusive_atMin_hasFieldError() {
            assertThat(exclusiveValidator.validate(new DecimalMinExclusiveRecord(new BigDecimal("10.5"))))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.DecimalMin.exclusive.message", "10.5"));
        }

        @Test
        void exclusive_aboveMin_noErrors() {
            assertThat(exclusiveValidator.validate(new DecimalMinExclusiveRecord(new BigDecimal("10.6"))))
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
            assertThat(inclusiveValidator.validate(new DecimalMaxInclusiveRecord(new BigDecimal("10.5"))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void inclusive_aboveMax_hasFieldError() {
            assertThat(inclusiveValidator.validate(new DecimalMaxInclusiveRecord(new BigDecimal("10.6"))))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.DecimalMax.message", "10.5"));
        }

        @Test
        void exclusive_atMax_hasFieldError() {
            assertThat(exclusiveValidator.validate(new DecimalMaxExclusiveRecord(new BigDecimal("10.5"))))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.DecimalMax.exclusive.message", "10.5"));
        }

        @Test
        void exclusive_belowMax_noErrors() {
            assertThat(exclusiveValidator.validate(new DecimalMaxExclusiveRecord(new BigDecimal("10.4"))))
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
            assertThat(validator.validate(new DigitsRecord(new BigDecimal("12345.67"))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void tooManyIntegerDigits_hasFieldError() {
            assertThat(validator.validate(new DigitsRecord(new BigDecimal("123456.7"))))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Digits.message", 5, 2));
        }

        @Test
        void tooManyFractionDigits_hasFieldError() {
            assertThat(validator.validate(new DigitsRecord(new BigDecimal("12345.678"))))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Digits.message", 5, 2));
        }
    }

    @Nested
    class DigitsCharSequence {
        DigitsCharSequenceRecordValidator validator = new DigitsCharSequenceRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new DigitsCharSequenceRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void validIntegerOnly_noErrors() {
            assertThat(validator.validate(new DigitsCharSequenceRecord("12345")))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void validDecimal_noErrors() {
            assertThat(validator.validate(new DigitsCharSequenceRecord("12345.67")))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void negativeValidDecimal_noErrors() {
            assertThat(validator.validate(new DigitsCharSequenceRecord("-12345.67")))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void tooManyIntegerDigits_hasFieldError() {
            assertThat(validator.validate(new DigitsCharSequenceRecord("123456.7")))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Digits.message", 5, 2));
        }

        @Test
        void tooManyFractionDigits_hasFieldError() {
            assertThat(validator.validate(new DigitsCharSequenceRecord("12345.678")))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Digits.message", 5, 2));
        }

        @Test
        void nonNumericValue_hasFieldError() {
            assertThat(validator.validate(new DigitsCharSequenceRecord("abc")))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Digits.message", 5, 2));
        }
    }

    // ── @Digits (Primitive Integer) ────────────────────────────────────────────
    @Nested
    class DigitsPrimitive {
        DigitsPrimitiveRecordValidator validator = new DigitsPrimitiveRecordValidator();

        @Test
        void validDigits_noErrors() {
            assertThat(validator.validate(new DigitsPrimitiveRecord(12345)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void tooManyIntegerDigits_hasFieldError() {
            assertThat(validator.validate(new DigitsPrimitiveRecord(123456)))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Digits.message", 5, 2));
        }

        @Test
        void zeroValue_noErrors() {
            assertThat(validator.validate(new DigitsPrimitiveRecord(0)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void negativeValue_noErrors() {
            assertThat(validator.validate(new DigitsPrimitiveRecord(-12345)))
                    .isEqualTo(ValidationErrors.empty());
        }
    }

    // ── @Digits (Number) ──────────────────────────────────────────────────────
    @Nested
    class DigitsNumber {
        DigitsNumberRecordValidator validator = new DigitsNumberRecordValidator();

        @Test
        void validDigits_noErrors() {
            assertThat(validator.validate(new DigitsNumberRecord(12345L)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void tooManyIntegerDigits_hasFieldError() {
            assertThat(validator.validate(new DigitsNumberRecord(123456L)))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Digits.message", 5, 2));
        }

        @Test
        void zeroValue_noErrors() {
            assertThat(validator.validate(new DigitsNumberRecord(0L)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void negativeValue_noErrors() {
            assertThat(validator.validate(new DigitsNumberRecord(-12345L)))
                    .isEqualTo(ValidationErrors.empty());
        }
    }

    // ── @Size (Collection) ────────────────────────────────────────────────────
    @Nested
    class SizeCollection {
        SizeCollectionRecordValidator validator = new SizeCollectionRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new SizeCollectionRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void belowMin_hasFieldError() {
            assertThat(validator.validate(new SizeCollectionRecord(List.of())))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Size.message", 1, 10));
        }

        @Test
        void atMin_noErrors() {
            assertThat(validator.validate(new SizeCollectionRecord(List.of("a"))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void atMax_noErrors() {
            assertThat(validator.validate(new SizeCollectionRecord(
                    List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void aboveMax_hasFieldError() {
            assertThat(validator.validate(new SizeCollectionRecord(
                    List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"))))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Size.message", 1, 10));
        }
    }

    // ── @Size (Map) ───────────────────────────────────────────────────────────
    @Nested
    class SizeMap {
        SizeMapRecordValidator validator = new SizeMapRecordValidator();

        @Test
        void nullValue_noErrors() {
            assertThat(validator.validate(new SizeMapRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void belowMin_hasFieldError() {
            assertThat(validator.validate(new SizeMapRecord(Map.of())))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Size.message", 1, 10));
        }

        @Test
        void atMin_noErrors() {
            assertThat(validator.validate(new SizeMapRecord(Map.of("a", "1"))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void atMax_noErrors() {
            assertThat(validator.validate(new SizeMapRecord(Map.ofEntries(
                    Map.entry("1", "a"), Map.entry("2", "b"), Map.entry("3", "c"), Map.entry("4", "d"),
                    Map.entry("5", "e"), Map.entry("6", "f"), Map.entry("7", "g"), Map.entry("8", "h"),
                    Map.entry("9", "i"), Map.entry("10", "j")))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void aboveMax_hasFieldError() {
            assertThat(validator.validate(new SizeMapRecord(Map.ofEntries(
                    Map.entry("1", "a"), Map.entry("2", "b"), Map.entry("3", "c"), Map.entry("4", "d"),
                    Map.entry("5", "e"), Map.entry("6", "f"), Map.entry("7", "g"), Map.entry("8", "h"),
                    Map.entry("9", "i"), Map.entry("10", "j"), Map.entry("11", "k")))))
                    .isEqualTo(ValidationErrors.at("value", "io.github.raniagus.javalidation.constraints.Size.message", 1, 10));
        }
    }
}