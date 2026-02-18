package io.github.raniagus.javalidation.validator.processor;

import static com.google.testing.compile.Compiler.javac;
import static org.assertj.core.api.Assertions.assertThat;
import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.JavaFileObjects;
import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.validator.Validator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import test.iterable.*;

public class IterableValidationsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "PrimitiveIterableRecord",
            "ValidatedIterableRecord",
            "NestedIterableRecord",
    })
    void givenAnnotatedRecords_WhenAnnotationProcessing_ThenGenerateExpectedFiles(String recordName) {
        assertThat(
                javac()
                        .withProcessors(new ValidatorProcessor())
                        .compile(JavaFileObjects.forResource("test/iterable/" + recordName + ".java")))
                .generatedSourceFile("test.iterable." + recordName + "Validator")
                .hasSourceEquivalentTo(
                        JavaFileObjects.forResource("test/iterable/" + recordName + "Validator.java"));
    }

    @Nested
    class PrimitiveIterableRecordValidatorTest {
        private final Validator<PrimitiveIterableRecord> validator = new PrimitiveIterableRecordValidator();

        @Test
        void nullTags_hasFieldError() {
            assertThat(validator.validate(new PrimitiveIterableRecord(null)))
                    .isEqualTo(ValidationErrors.ofField("tags", "must not be null"));
        }

        @Test
        void emptyTags_noErrors() {
            assertThat(validator.validate(new PrimitiveIterableRecord(List.of())))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void validTags_noErrors() {
            assertThat(validator.validate(new PrimitiveIterableRecord(List.of("abc", "hello", "world123"))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void tagTooShort_hasFieldError() {
            assertThat(validator.validate(new PrimitiveIterableRecord(List.of("ab"))))
                    .isEqualTo(ValidationErrors.ofField(FieldKey.of("tags", 0), "size must be between {0} and {1}", 3, 10));
        }

        @Test
        void tagTooLong_hasFieldError() {
            assertThat(validator.validate(new PrimitiveIterableRecord(List.of("abcdefghijk"))))
                    .isEqualTo(ValidationErrors.ofField(FieldKey.of("tags", 0), "size must be between {0} and {1}", 3, 10));
        }

        @Test
        void nullTagItem_noErrors() {
            assertThat(validator.validate(new PrimitiveIterableRecord(Collections.singletonList(null))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void multipleErrors_allReported() {
            assertThat(validator.validate(new PrimitiveIterableRecord(List.of("ab", "hello", "abcdefghijk"))))
                    .isEqualTo(
                        createValidationErrors(v ->
                            v.validateField("tags", () -> {
                                v.addFieldError(0, "size must be between {0} and {1}", 3, 10);
                                v.addFieldError(2, "size must be between {0} and {1}", 3, 10);
                            })
                        )
                    );
        }
    }

    @Nested
    class ValidatedIterableRecordValidatorTest {
        private final Validator<ValidatedIterableRecord> validator = new ValidatedIterableRecordValidator();

        @Test
        void nullFriends_hasFieldError() {
            assertThat(validator.validate(new ValidatedIterableRecord(null)))
                    .isEqualTo(ValidationErrors.ofField("friends", "must not be null"));
        }

        @Test
        void emptyFriends_noErrors() {
            assertThat(validator.validate(new ValidatedIterableRecord(List.of())))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void validFriends_noErrors() {
            assertThat(validator.validate(new ValidatedIterableRecord(List.of(
                    new ValidatedIterableRecord.Person("Alice"),
                    new ValidatedIterableRecord.Person("Bob")
            )))).isEqualTo(ValidationErrors.empty());
        }

        @Test
        void nullFriendItem_hasFieldError() {
            assertThat(validator.validate(new ValidatedIterableRecord(Collections.singletonList(null))))
                    .isEqualTo(ValidationErrors.ofField(FieldKey.of("friends", 0), "must not be null"));
        }

        @Test
        void friendWithNullName_hasNestedFieldError() {
            assertThat(validator.validate(new ValidatedIterableRecord(List.of(
                    new ValidatedIterableRecord.Person(null)
            )))).isEqualTo(ValidationErrors.ofField(FieldKey.of("friends", 0, "name"), "must not be null"));
        }

        @Test
        void multipleErrors_allReported() {
            assertThat(validator.validate(new ValidatedIterableRecord(List.of(
                    new ValidatedIterableRecord.Person(null),
                    new ValidatedIterableRecord.Person("Alice"),
                    new ValidatedIterableRecord.Person(null)
            )))).isEqualTo(
                createValidationErrors(v ->
                    v.validateField("friends", () -> {
                        v.validateField(0, () -> {
                            v.addFieldError("name", "must not be null");
                        });
                        v.validateField(2, () -> {
                            v.addFieldError("name", "must not be null");
                        });
                    })
                )
            );
        }

        @Test
        void nullItemAndInvalidItem_allReported() {
            assertThat(validator.validate(new ValidatedIterableRecord(Arrays.asList(
                    null,
                    new ValidatedIterableRecord.Person(null)
            )))).isEqualTo(
                createValidationErrors(v ->
                    v.validateField("friends", () -> {
                        v.validateField(0, () -> {
                                v.addRootError("must not be null");
                        });
                        v.validateField(1, () -> {
                                v.addFieldError("name", "must not be null");
                        });
                    })
                )
            );
        }
    }

    @Nested
    class NestedIterableRecordValidatorTest {
        private final Validator<NestedIterableRecord> validator = new NestedIterableRecordValidator();

        @Test
        void nullScores_noErrors() {
            assertThat(validator.validate(new NestedIterableRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void emptyScores_noErrors() {
            assertThat(validator.validate(new NestedIterableRecord(List.of())))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void emptyInnerList_hasFieldError() {
            assertThat(validator.validate(new NestedIterableRecord(List.of(List.of()))))
                    .isEqualTo(ValidationErrors.ofField(FieldKey.of("scores", 0), "must not be empty"));
        }

        @Test
        void nullInnerList_hasFieldError() {
            assertThat(validator.validate(new NestedIterableRecord(Collections.singletonList(null))))
                    .isEqualTo(ValidationErrors.ofField(FieldKey.of("scores", 0), "must not be empty"));
        }

        @Test
        void nullItemInInnerList_hasFieldError() {
            assertThat(validator.validate(new NestedIterableRecord(List.of(Collections.singletonList(null)))))
                    .isEqualTo(ValidationErrors.ofField(FieldKey.of("scores", 0, 0), "must not be null"));
        }

        @Test
        void validScores_noErrors() {
            assertThat(validator.validate(new NestedIterableRecord(List.of(List.of(1, 2), List.of(3)))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void multipleErrors_allReported() {
            assertThat(validator.validate(new NestedIterableRecord(List.of(
                    Arrays.asList(null, null),
                    List.of()
            )))).isEqualTo(
                    createValidationErrors(v ->
                        v.validateField("scores", () -> {
                            v.validateField(0, () -> {
                                    v.addFieldError(0, "must not be null");
                                    v.addFieldError(1, "must not be null");
                            });
                            v.validateField(1, () -> {
                                    v.addRootError("must not be empty");
                            });
                        })
                    )
            );
        }
    }

    private ValidationErrors createValidationErrors(Consumer<Validation> consumer) {
        Validation validation = Validation.create();
        consumer.accept(validation);
        return validation.finish();
    }
}
