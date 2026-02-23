package io.github.raniagus.javalidation.validator.processor;

import static com.google.testing.compile.Compiler.javac;
import static org.assertj.core.api.Assertions.assertThat;
import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.JavaFileObjects;
import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.validator.Validator;
import java.util.*;
import java.util.function.Consumer;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import test.collection.*;

public class CollectionValidationsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "PrimitiveIterableRecord",
            "ValidatedIterableRecord",
            "NestedIterableRecord",
            "PrimitiveMapRecord",
            "ValidatedMapRecord",
            "NestedMapRecord"
    })
    void givenAnnotatedRecords_WhenAnnotationProcessing_ThenGenerateExpectedFiles(String recordName) {
        JavaFileObject recordFile = JavaFileObjects.forResource("test/collection/" + recordName + ".java");
        JavaFileObject triggerFile = JavaFileObjects.forSourceString("test.SimpleService", """
                package test;
    
                import jakarta.validation.*;
    
                public class SimpleService {
                    public void doSomething(test.collection.@Valid %s input) {}
                }
                """.formatted(recordName)
        );

        assertThat(
                javac()
                        .withProcessors(new ValidatorProcessor())
                        .compile(recordFile, triggerFile))
                .generatedSourceFile("test.collection." + recordName + "Validator")
                .hasSourceEquivalentTo(
                        JavaFileObjects.forResource("test/collection/" + recordName + "Validator.java"));
    }

    @Nested
    class PrimitiveIterableRecordValidatorTest {
        private final Validator<PrimitiveIterableRecord> validator = new PrimitiveIterableRecordValidator();

        @Test
        void nullTags_hasFieldError() {
            assertThat(validator.validate(new PrimitiveIterableRecord(null)))
                    .isEqualTo(ValidationErrors.at("tags", "io.github.raniagus.javalidation.constraints.NotNull.message"));
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
                    .isEqualTo(ValidationErrors.at(FieldKey.of("tags", 0), "io.github.raniagus.javalidation.constraints.Size.message", 3, 10));
        }

        @Test
        void tagTooLong_hasFieldError() {
            assertThat(validator.validate(new PrimitiveIterableRecord(List.of("abcdefghijk"))))
                    .isEqualTo(ValidationErrors.at(FieldKey.of("tags", 0), "io.github.raniagus.javalidation.constraints.Size.message", 3, 10));
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
                            v.withField("tags", () -> {
                                v.addErrorAt(0, "io.github.raniagus.javalidation.constraints.Size.message", 3, 10);
                                v.addErrorAt(2, "io.github.raniagus.javalidation.constraints.Size.message", 3, 10);
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
                    .isEqualTo(ValidationErrors.at("friends", "io.github.raniagus.javalidation.constraints.NotNull.message"));
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
                    .isEqualTo(ValidationErrors.at(FieldKey.of("friends", 0), "io.github.raniagus.javalidation.constraints.NotNull.message"));
        }

        @Test
        void friendWithNullName_hasNestedFieldError() {
            assertThat(validator.validate(new ValidatedIterableRecord(List.of(
                    new ValidatedIterableRecord.Person(null)
            )))).isEqualTo(ValidationErrors.at(FieldKey.of("friends", 0, "name"), "io.github.raniagus.javalidation.constraints.NotNull.message"));
        }

        @Test
        void multipleErrors_allReported() {
            assertThat(validator.validate(new ValidatedIterableRecord(List.of(
                    new ValidatedIterableRecord.Person(null),
                    new ValidatedIterableRecord.Person("Alice"),
                    new ValidatedIterableRecord.Person(null)
            )))).isEqualTo(
                createValidationErrors(v ->
                    v.withField("friends", () -> {
                        v.withField(0, () -> {
                            v.addErrorAt("name", "io.github.raniagus.javalidation.constraints.NotNull.message");
                        });
                        v.withField(2, () -> {
                            v.addErrorAt("name", "io.github.raniagus.javalidation.constraints.NotNull.message");
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
                    v.withField("friends", () -> {
                        v.withField(0, () -> {
                                v.addError("io.github.raniagus.javalidation.constraints.NotNull.message");
                        });
                        v.withField(1, () -> {
                                v.addErrorAt("name", "io.github.raniagus.javalidation.constraints.NotNull.message");
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
        void nullScores_hasFieldError() {
            assertThat(validator.validate(new NestedIterableRecord(null)))
                    .isEqualTo(ValidationErrors.at("scores", "io.github.raniagus.javalidation.constraints.NotEmpty.message"));
        }

        @Test
        void emptyScores_noHasFieldError() {
            assertThat(validator.validate(new NestedIterableRecord(List.of())))
                    .isEqualTo(ValidationErrors.at("scores", "io.github.raniagus.javalidation.constraints.NotEmpty.message"));
        }

        @Test
        void emptyInnerList_hasFieldError() {
            assertThat(validator.validate(new NestedIterableRecord(List.of(List.of()))))
                    .isEqualTo(ValidationErrors.at(FieldKey.of("scores", 0), "io.github.raniagus.javalidation.constraints.NotEmpty.message"));
        }

        @Test
        void nullInnerList_hasFieldError() {
            assertThat(validator.validate(new NestedIterableRecord(Collections.singletonList(null))))
                    .isEqualTo(ValidationErrors.at(FieldKey.of("scores", 0), "io.github.raniagus.javalidation.constraints.NotEmpty.message"));
        }

        @Test
        void nullItemInInnerList_hasFieldError() {
            assertThat(validator.validate(new NestedIterableRecord(List.of(Collections.singletonList(null)))))
                    .isEqualTo(ValidationErrors.at(FieldKey.of("scores", 0, 0), "io.github.raniagus.javalidation.constraints.NotNull.message"));
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
                        v.withField("scores", () -> {
                            v.withField(0, () -> {
                                    v.addErrorAt(0, "io.github.raniagus.javalidation.constraints.NotNull.message");
                                    v.addErrorAt(1, "io.github.raniagus.javalidation.constraints.NotNull.message");
                            });
                            v.withField(1, () -> {
                                    v.addError("io.github.raniagus.javalidation.constraints.NotEmpty.message");
                            });
                        })
                    )
            );
        }
    }

    @Nested
    class PrimitiveMapRecordValidatorTest {
        private final Validator<PrimitiveMapRecord> validator = new PrimitiveMapRecordValidator();

        @Test
        void nullTags_hasFieldError() {
            assertThat(validator.validate(new PrimitiveMapRecord(null)))
                    .isEqualTo(ValidationErrors.at("tags", "io.github.raniagus.javalidation.constraints.NotEmpty.message"));
        }

        @Test
        void emptyTags_hasFieldError() {
            assertThat(validator.validate(new PrimitiveMapRecord(Map.of())))
                    .isEqualTo(ValidationErrors.at("tags", "io.github.raniagus.javalidation.constraints.NotEmpty.message"));
        }

        @Test
        void validTags_noErrors() {
            assertThat(validator.validate(new PrimitiveMapRecord(Map.of("key", "value"))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void nullKey_hasFieldError() {
            assertThat(validator.validate(new PrimitiveMapRecord(Collections.singletonMap(null, "value"))))
                    .isEqualTo(ValidationErrors.at("tags", "io.github.raniagus.javalidation.constraints.NotNull.message"));
        }

        @Test
        void nullValue_hasFieldError() {
            assertThat(validator.validate(new PrimitiveMapRecord(Collections.singletonMap("key", null))))
                    .isEqualTo(ValidationErrors.at(FieldKey.of("tags", "key"), "io.github.raniagus.javalidation.constraints.NotNull.message"));
        }

        @Test
        void nullKeyStopsValueValidation() {
            assertThat(validator.validate(new PrimitiveMapRecord(Collections.singletonMap(null, null))))
                    .isEqualTo(ValidationErrors.at("tags", "io.github.raniagus.javalidation.constraints.NotNull.message"));
        }

        @Test
        void multipleErrors_allReported() {
            Map<String, String> tags = new LinkedHashMap<>();
            tags.put("a", null);
            tags.put("b", "valid");
            tags.put("c", null);

            assertThat(validator.validate(new PrimitiveMapRecord(tags)))
                    .isEqualTo(
                            createValidationErrors(v ->
                                    v.withField("tags", () -> {
                                        v.withField("a", () -> v.addError("io.github.raniagus.javalidation.constraints.NotNull.message"));
                                        v.withField("c", () -> v.addError("io.github.raniagus.javalidation.constraints.NotNull.message"));
                                    })
                            )
                    );
        }
    }

    @Nested
    class NestedMapRecordValidatorTest {
        private final Validator<NestedMapRecord> validator = new NestedMapRecordValidator();

        @Test
        void nullScores_noErrors() {
            assertThat(validator.validate(new NestedMapRecord(null)))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void emptyScores_noErrors() {
            assertThat(validator.validate(new NestedMapRecord(Map.of())))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void nullOuterKey_hasFieldError() {
            assertThat(validator.validate(new NestedMapRecord(Collections.singletonMap(null, Map.of()))))
                    .isEqualTo(ValidationErrors.at("scores", "io.github.raniagus.javalidation.constraints.NotNull.message"));
        }

        @Test
        void nullOuterValue_hasFieldError() {
            assertThat(validator.validate(new NestedMapRecord(Collections.singletonMap("a", null))))
                    .isEqualTo(ValidationErrors.at(FieldKey.of("scores", "a"), "io.github.raniagus.javalidation.constraints.NotEmpty.message"));
        }

        @Test
        void emptyInnerMap_hasFieldError() {
            assertThat(validator.validate(new NestedMapRecord(Map.of("a", Map.of()))))
                    .isEqualTo(ValidationErrors.at(FieldKey.of("scores", "a"), "io.github.raniagus.javalidation.constraints.NotEmpty.message"));
        }

        @Test
        void nullInnerKey_hasFieldError() {
            assertThat(validator.validate(new NestedMapRecord(Map.of("a", Collections.singletonMap(null, 1)))))
                    .isEqualTo(ValidationErrors.at(FieldKey.of("scores", "a"), "io.github.raniagus.javalidation.constraints.NotNull.message"));
        }

        @Test
        void nullInnerValue_hasFieldError() {
            assertThat(validator.validate(new NestedMapRecord(Map.of("a", Collections.singletonMap("b", null)))))
                    .isEqualTo(ValidationErrors.at(FieldKey.of("scores", "a", "b"), "io.github.raniagus.javalidation.constraints.NotNull.message"));
        }

        @Test
        void validScores_noErrors() {
            assertThat(validator.validate(new NestedMapRecord(Map.of("a", Map.of("b", 1, "c", 2)))))
                    .isEqualTo(ValidationErrors.empty());
        }

        @Test
        void nullOuterKeyStopsInnerValidation() {
            assertThat(validator.validate(new NestedMapRecord(Collections.singletonMap(null, null))))
                    .isEqualTo(ValidationErrors.at("scores", "io.github.raniagus.javalidation.constraints.NotNull.message"));
        }

        @Test
        void multipleErrors_allReported() {
            Map<String, Map<String, Integer>> scores = new LinkedHashMap<>();
            scores.put("a", Collections.singletonMap(null, 1));
            scores.put("b", Collections.singletonMap("x", null));
            scores.put("c", null);

            assertThat(validator.validate(new NestedMapRecord(scores)))
                    .isEqualTo(
                            createValidationErrors(v ->
                                    v.withField("scores", () -> {
                                        v.withField("a", () -> v.addError("io.github.raniagus.javalidation.constraints.NotNull.message"));
                                        v.withField("b", () -> v.withField("x", () -> v.addError("io.github.raniagus.javalidation.constraints.NotNull.message")));
                                        v.withField("c", () -> v.addError("io.github.raniagus.javalidation.constraints.NotEmpty.message"));
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
