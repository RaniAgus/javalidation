package io.github.raniagus.javalidation.validator;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import io.github.raniagus.javalidation.processor.ValidatorProcessor;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;

public class JakartaValidationsTest {

    // ─── helpers ────────────────────────────────────────────────────────────

    private static Compilation compile(String recordBody) {
        JavaFileObject source = JavaFileObjects.forSourceString("test.TestRecord", """
                package test;
                
                import io.github.raniagus.javalidation.annotation.*;
                import io.github.raniagus.javalidation.validator.*;
                import jakarta.validation.constraints.*;
                import java.math.*;
                import java.time.*;
                import java.util.*;
                
                @Validate
                public record TestRecord(%s) {}
                """.formatted(recordBody));

        return javac()
                .withProcessors(new ValidatorProcessor())
                .compile(source);
    }

    private static JavaFileObject expectedValidator(String body) {
        return expectedValidator(body, false);
    }

    private static JavaFileObject expectedValidator(String body, boolean withValidatorUtils) {
        return JavaFileObjects.forSourceString("test.TestRecordValidator", """
                package test;
                
                import io.github.raniagus.javalidation.Validation;
                import io.github.raniagus.javalidation.ValidationErrors;
                import io.github.raniagus.javalidation.validator.Validator;
                %simport javax.annotation.processing.Generated;
                import org.jspecify.annotations.Nullable;
                
                @Generated("io.github.raniagus.javalidation.processor.ValidatorProcessor")
                public class TestRecordValidator implements Validator<TestRecord> {
                    @Override
                    public ValidationErrors validate(@Nullable TestRecord root) {
                        Validation rootValidation = Validation.create();
                        %s
                        return rootValidation.finish();
                    }
                }
                """.formatted(withValidatorUtils ? """
                import io.github.raniagus.javalidation.validator.ValidatorUtils;
                """ : "", body));
    }

    // ─── @NotNull ────────────────────────────────────────────────────────────

    @Test
    void notNull() {
        assertThat(compile("@NotNull String value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                        var value = root.value();
                        var valueValidation = Validation.create();
                        if (java.util.Objects.isNull(value)) {
                            valueValidation.addRootError("must not be null");
                        }
                        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                        """));
    }

    // ─── @NotEmpty ───────────────────────────────────────────────────────────

    @Test
    void notEmpty() {
        assertThat(compile("@NotEmpty String value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                        var value = root.value();
                        var valueValidation = Validation.create();
                        if (java.util.Objects.isNull(value) || value.isEmpty()) {
                            valueValidation.addRootError("must not be empty");
                        }
                        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                        """));
    }

    // ─── @NotBlank ───────────────────────────────────────────────────────────

    @Test
    void notBlank() {
        assertThat(compile("@NotBlank String value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                        var value = root.value();
                        var valueValidation = Validation.create();
                        if (java.util.Objects.isNull(value) || value.isBlank()) {
                            valueValidation.addRootError("must not be blank");
                        }
                        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                        """));
    }

    // ─── @Size ───────────────────────────────────────────────────────────────

    @Test
    void size_minAndMax() {
        assertThat(compile("@Size(min = 1, max = 10) String value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                        var value = root.value();
                        var valueValidation = Validation.create();
                        if (java.util.Objects.nonNull(value)) {
                            if (value.length() < 1 || value.length() > 10) {
                                valueValidation.addRootError("size must be between {0} and {1}", 1, 10);
                            }
                        }
                        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                        """));
    }

    @Test
    void size_minOnly() {
        assertThat(compile("@Size(min = 1) String value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                        var value = root.value();
                        var valueValidation = Validation.create();
                        if (java.util.Objects.nonNull(value)) {
                            if (value.length() < 1 || value.length() > 2147483647) {
                                valueValidation.addRootError("size must be between {0} and {1}", 1, 2147483647);
                            }
                        }
                        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                        """));
    }

    @Test
    void size_maxOnly() {
        assertThat(compile("@Size(max = 10) String value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                        var value = root.value();
                        var valueValidation = Validation.create();
                        if (java.util.Objects.nonNull(value)) {
                            if (value.length() < 0 || value.length() > 10) {
                                valueValidation.addRootError("size must be between {0} and {1}", 0, 10);
                            }
                        }
                        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                        """));
    }

    @Test
    void size_collection() {
        assertThat(compile("@Size(min = 1, max = 10) List<String> value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                    var value = root.value();
                    var valueValidation = Validation.create();
                    if (java.util.Objects.nonNull(value)) {
                        if (value.size() < 1 || value.size() > 10) {
                            valueValidation.addRootError("size must be between {0} and {1}", 1, 10);
                        }
                    }
                    rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                    """));
    }

    @Test
    void size_map() {
        assertThat(compile("@Size(min = 1, max = 10) Map<String, String> value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                    var value = root.value();
                    var valueValidation = Validation.create();
                    if (java.util.Objects.nonNull(value)) {
                        if (value.size() < 1 || value.size() > 10) {
                            valueValidation.addRootError("size must be between {0} and {1}", 1, 10);
                        }
                    }
                    rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                    """));
    }

    // ─── @Email ───────────────────────────────────────────────────────────────

    @Test
    void email() {
        assertThat(compile("@Email String value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                        var value = root.value();
                        var valueValidation = Validation.create();
                        if (java.util.Objects.nonNull(value)) {
                            if (!java.util.Objects.toString(value).matches("^[^@]+@[^@]+\\\\.[^@]+$")) {
                                valueValidation.addRootError("must be a well-formed email address");
                            }
                        }
                        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                        """));
    }

    // ─── @Min / @Max ─────────────────────────────────────────────────────────

    @Test
    void min() {
        assertThat(compile("@Min(10) long value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                        var value = root.value();
                        var valueValidation = Validation.create();
                        if (java.util.Objects.nonNull(value)) {
                            if (!(value >= 10)) {
                                valueValidation.addRootError("must be greater than or equal to {0}", 10);
                            }
                        }
                        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                        """));
    }

    @Test
    void max_referenceType() {
        assertThat(compile("@Max(100) Integer value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                        var value = root.value();
                        var valueValidation = Validation.create();
                        if (java.util.Objects.nonNull(value)) {
                            if (!(value <= 100)) {
                                valueValidation.addRootError("must be less than or equal to {0}", 100);
                            }
                        }
                        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                        """));
    }

    @Test
    void max_primitiveType() {
        assertThat(compile("@Max(100) long value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                        var value = root.value();
                        var valueValidation = Validation.create();
                        if (java.util.Objects.nonNull(value)) {
                            if (!(value <= 100)) {
                                valueValidation.addRootError("must be less than or equal to {0}", 100);
                            }
                        }
                        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                        """));
    }

    // ─── @Positive / @PositiveOrZero ─────────────────────────────────────────

    @Test
    void positive_referenceType() {
        assertThat(compile("@Positive Integer value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                        var value = root.value();
                        var valueValidation = Validation.create();
                        if (java.util.Objects.nonNull(value)) {
                            if (!(value > 0)) {
                                valueValidation.addRootError("must be greater than 0");
                            }
                        }
                        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                        """));
    }

    @Test
    void positive_primitiveType() {
        assertThat(compile("@Positive long value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                        var value = root.value();
                        var valueValidation = Validation.create();
                        if (java.util.Objects.nonNull(value)) {
                            if (!(value > 0)) {
                                valueValidation.addRootError("must be greater than 0");
                            }
                        }
                        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                        """));
    }

    @Test
    void positiveOrZero_referenceType() {
        assertThat(compile("@PositiveOrZero Integer value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                        var value = root.value();
                        var valueValidation = Validation.create();
                        if (java.util.Objects.nonNull(value)) {
                            if (!(value >= 0)) {
                                valueValidation.addRootError("must be greater than or equal to 0");
                            }
                        }
                        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                        """));
    }

    @Test
    void positiveOrZero_primitiveType() {
        assertThat(compile("@PositiveOrZero long value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                        var value = root.value();
                        var valueValidation = Validation.create();
                        if (java.util.Objects.nonNull(value)) {
                            if (!(value >= 0)) {
                                valueValidation.addRootError("must be greater than or equal to 0");
                            }
                        }
                        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                        """));
    }

    // ─── @Negative / @NegativeOrZero ─────────────────────────────────────────

    @Test
    void negative_referenceType() {
        assertThat(compile("@Negative Integer value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                        var value = root.value();
                        var valueValidation = Validation.create();
                        if (java.util.Objects.nonNull(value)) {
                            if (!(value < 0)) {
                                valueValidation.addRootError("must be less than 0");
                            }
                        }
                        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                        """));
    }

    @Test
    void negative_primitiveType() {
        assertThat(compile("@Negative long value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                        var value = root.value();
                        var valueValidation = Validation.create();
                        if (java.util.Objects.nonNull(value)) {
                            if (!(value < 0)) {
                                valueValidation.addRootError("must be less than 0");
                            }
                        }
                        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                        """));
    }

    @Test
    void negativeOrZero_referenceType() {
        assertThat(compile("@NegativeOrZero Integer value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                        var value = root.value();
                        var valueValidation = Validation.create();
                        if (java.util.Objects.nonNull(value)) {
                            if (!(value <= 0)) {
                                valueValidation.addRootError("must be less than or equal to 0");
                            }
                        }
                        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                        """));
    }

    @Test
    void negativeOrZero_primitiveType() {
        assertThat(compile("@NegativeOrZero long value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                        var value = root.value();
                        var valueValidation = Validation.create();
                        if (java.util.Objects.nonNull(value)) {
                            if (!(value <= 0)) {
                                valueValidation.addRootError("must be less than or equal to 0");
                            }
                        }
                        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                        """));
    }

    @Test
    void past() {
        assertThat(compile("@Past Instant value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                    var value = root.value();
                    var valueValidation = Validation.create();
                    if (java.util.Objects.nonNull(value)) {
                        if (!(ValidatorUtils.toInstant(value).isBefore(java.time.Instant.now()) == true)) {
                            valueValidation.addRootError("must be a past date");
                        }
                    }
                    rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                    """, true));
    }

    @Test
    void pastOrPresent() {
        assertThat(compile("@PastOrPresent Instant value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                    var value = root.value();
                    var valueValidation = Validation.create();
                    if (java.util.Objects.nonNull(value)) {
                        if (!(ValidatorUtils.toInstant(value).isAfter(java.time.Instant.now()) == false)) {
                            valueValidation.addRootError("must be a date in the past or in the present");
                        }
                    }
                    rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                    """, true));
    }

    @Test
    void future() {
        assertThat(compile("@Future Instant value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                    var value = root.value();
                    var valueValidation = Validation.create();
                    if (java.util.Objects.nonNull(value)) {
                        if (!(ValidatorUtils.toInstant(value).isAfter(java.time.Instant.now()) == true)) {
                            valueValidation.addRootError("must be a future date");
                        }
                    }
                    rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                    """, true));
    }

    @Test
    void futureOrPresent() {
        assertThat(compile("@FutureOrPresent Instant value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                    var value = root.value();
                    var valueValidation = Validation.create();
                    if (java.util.Objects.nonNull(value)) {
                        if (!(ValidatorUtils.toInstant(value).isBefore(java.time.Instant.now()) == false)) {
                            valueValidation.addRootError("must be a date in the present or in the future");
                        }
                    }
                    rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                    """, true));
    }

// ─── @Pattern ─────────────────────────────────────────────────────────────

    @Test
    void pattern() {
        assertThat(compile("@Pattern(regexp = \"^[a-z]+$\") String value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                    var value = root.value();
                    var valueValidation = Validation.create();
                    if (java.util.Objects.nonNull(value)) {
                        if (!java.util.Objects.toString(value).matches("^[a-z]+$")) {
                            valueValidation.addRootError("must match \\"{0}\\"", "^[a-z]+$");
                        }
                    }
                    rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                    """));
    }

    // ─── @DecimalMin / @DecimalMax ────────────────────────────────────────────

    @Test
    void decimalMin_inclusive() {
        assertThat(compile("@DecimalMin(\"10.5\") BigDecimal value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                    var value = root.value();
                    var valueValidation = Validation.create();
                    if (java.util.Objects.nonNull(value)) {
                        if (!(ValidatorUtils.compare(value, "10.5") >= 0)) {
                            valueValidation.addRootError("must be greater than or equal to {0}", "10.5");
                        }
                    }
                    rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                    """, true));
    }

    @Test
    void decimalMin_exclusive() {
        assertThat(compile("@DecimalMin(value = \"10.5\", inclusive = false) BigDecimal value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                    var value = root.value();
                    var valueValidation = Validation.create();
                    if (java.util.Objects.nonNull(value)) {
                        if (!(ValidatorUtils.compare(value, "10.5") > 0)) {
                            valueValidation.addRootError("must be greater than {0}", "10.5");
                        }
                    }
                    rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                    """, true));
    }

    @Test
    void decimalMax_inclusive() {
        assertThat(compile("@DecimalMax(\"10.5\") BigDecimal value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                    var value = root.value();
                    var valueValidation = Validation.create();
                    if (java.util.Objects.nonNull(value)) {
                        if (!(ValidatorUtils.compare(value, "10.5") <= 0)) {
                            valueValidation.addRootError("must be less than or equal to {0}", "10.5");
                        }
                    }
                    rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                    """, true));
    }

    @Test
    void decimalMax_exclusive() {
        assertThat(compile("@DecimalMax(value = \"10.5\", inclusive = false) BigDecimal value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                    var value = root.value();
                    var valueValidation = Validation.create();
                    if (java.util.Objects.nonNull(value)) {
                        if (!(ValidatorUtils.compare(value, "10.5") < 0)) {
                            valueValidation.addRootError("must be less than {0}", "10.5");
                        }
                    }
                    rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                    """, true));
    }

    // ─── @Digits ──────────────────────────────────────────────────────────────

    @Test
    void digits() {
        assertThat(compile("@Digits(integer = 5, fraction = 2) BigDecimal value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                    var value = root.value();
                    var valueValidation = Validation.create();
                    if (java.util.Objects.nonNull(value)) {
                        if (!java.util.Objects.toString(value).matches("^-?\\\\d{0,5}(\\\\.\\\\d{0,2})?$")) {
                            valueValidation.addRootError("numeric value out of bounds ({0} digits, {1} decimal digits expected)", 5, 2);
                        }
                    }
                    rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                    """));
    }

    // ─── combinations ────────────────────────────────────────────────────────

    @Test
    void notNull_and_size() {
        assertThat(compile("@NotNull @Size(min = 3, max = 10) String value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                    var value = root.value();
                    var valueValidation = Validation.create();
                    if (java.util.Objects.isNull(value)) {
                        valueValidation.addRootError("must not be null");
                    }
                    if (java.util.Objects.nonNull(value)) {
                        if (value.length() < 3 || value.length() > 10) {
                            valueValidation.addRootError("size must be between {0} and {1}", 3, 10);
                        }
                    }
                    rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                    """));
    }

    @Test
    void notNull_and_min() {
        assertThat(compile("@NotNull @Min(10) Integer value"))
                .generatedSourceFile("test.TestRecordValidator")
                .hasSourceEquivalentTo(expectedValidator("""
                    var value = root.value();
                    var valueValidation = Validation.create();
                    if (java.util.Objects.isNull(value)) {
                        valueValidation.addRootError("must not be null");
                    }
                    if (java.util.Objects.nonNull(value)) {
                        if (!(value >= 10)) {
                            valueValidation.addRootError("must be greater than or equal to {0}", 10);
                        }
                    }
                    rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
                    """));
    }
}
