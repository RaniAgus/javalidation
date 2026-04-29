package io.github.raniagus.javalidation.validator.processor;

import static io.github.raniagus.javalidation.assertj.JavalidationAssertions.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.JavaFileObjects;
import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.Validator;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import java.util.*;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.BeforeEach;
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
                    .hasErrorCount(1)
                    .hasFieldError("tags", "io.github.raniagus.javalidation.constraints.NotNull.message");
        }

        @Test
        void emptyTags_noErrors() {
            assertThat(validator.validate(new PrimitiveIterableRecord(List.of())))
                    .isEmpty();
        }

        @Test
        void validTags_noErrors() {
            assertThat(validator.validate(new PrimitiveIterableRecord(List.of("abc", "hello", "world123"))))
                    .isEmpty();
        }

        @Test
        void tagTooShort_hasFieldError() {
            assertThat(validator.validate(new PrimitiveIterableRecord(List.of("ab"))))
                    .hasErrorCount(1)
                    .hasFieldErrorAt(FieldKey.of("tags", 0), "io.github.raniagus.javalidation.constraints.Size.message", 3, 10);
        }

        @Test
        void tagTooLong_hasFieldError() {
            assertThat(validator.validate(new PrimitiveIterableRecord(List.of("abcdefghijk"))))
                    .hasErrorCount(1)
                    .hasFieldErrorAt(FieldKey.of("tags", 0), "io.github.raniagus.javalidation.constraints.Size.message", 3, 10);
        }

        @Test
        void nullTagItem_noErrors() {
            assertThat(validator.validate(new PrimitiveIterableRecord(Collections.singletonList(null))))
                    .isEmpty();
        }

        @Test
        void multipleErrors_allReported() {
            List<String> tags = List.of("ab", "hello", "abcdefghijk");
            assertThat(validator.validate(new PrimitiveIterableRecord(tags)))
                    .hasErrorCount(2)
                    .hasFieldErrorAt(FieldKey.of("tags", 0), "io.github.raniagus.javalidation.constraints.Size.message", 3, 10)
                    .hasFieldErrorAt(FieldKey.of("tags", 2), "io.github.raniagus.javalidation.constraints.Size.message", 3, 10);
        }
    }

    @Nested
    class ValidatedIterableRecordValidatorTest {
        private final InitializableValidator<ValidatedIterableRecord> validator = new ValidatedIterableRecordValidator();
        private final InitializableValidator<ValidatedIterableRecord.Person> personValidator = new ValidatedIterableRecord$PersonValidator();

        private final ValidatorsHolder validatorsHolder = new ValidatorsHolder(Map.of(
                ValidatedIterableRecord.class, validator,
                ValidatedIterableRecord.Person.class, personValidator
        ));

        @BeforeEach
        void setup() {
            validatorsHolder.initialize();
        }

        @Test
        void nullFriends_hasFieldError() {
            assertThat(validator.validate(new ValidatedIterableRecord(null)))
                    .hasErrorCount(1)
                    .hasFieldError("friends", "io.github.raniagus.javalidation.constraints.NotNull.message");
        }

        @Test
        void emptyFriends_noErrors() {
            assertThat(validator.validate(new ValidatedIterableRecord(List.of())))
                    .isEmpty();
        }

        @Test
        void validFriends_noErrors() {
            List<ValidatedIterableRecord.Person> friends = List.of(
                    new ValidatedIterableRecord.Person("Alice"),
                    new ValidatedIterableRecord.Person("Bob")
            );
            assertThat(validator.validate(new ValidatedIterableRecord(friends))).isEmpty();
        }

        @Test
        void nullFriendItem_hasFieldError() {
            assertThat(validator.validate(new ValidatedIterableRecord(Collections.singletonList(null))))
                    .hasErrorCount(1)
                    .hasFieldErrorAt(FieldKey.of("friends", 0), "io.github.raniagus.javalidation.constraints.NotNull.message");
        }

        @Test
        void friendWithNullName_hasNestedFieldError() {
            List<ValidatedIterableRecord.Person> friends = List.of(
                    new ValidatedIterableRecord.Person(null)
            );
            assertThat(validator.validate(new ValidatedIterableRecord(friends)))
                    .hasErrorCount(1)
                    .hasFieldErrorAt(FieldKey.of("friends", 0, "name"), "io.github.raniagus.javalidation.constraints.NotNull.message");
        }

        @Test
        void multipleErrors_allReported() {
            List<ValidatedIterableRecord.Person> friends = List.of(
                    new ValidatedIterableRecord.Person(null),
                    new ValidatedIterableRecord.Person("Alice"),
                    new ValidatedIterableRecord.Person(null)
            );
            assertThat(validator.validate(new ValidatedIterableRecord(friends)))
                    .hasErrorCount(2)
                    .hasFieldErrorAt(FieldKey.of("friends", 0, "name"), "io.github.raniagus.javalidation.constraints.NotNull.message")
                    .hasFieldErrorAt(FieldKey.of("friends", 2, "name"), "io.github.raniagus.javalidation.constraints.NotNull.message");
        }

        @Test
        void nullItemAndInvalidItem_allReported() {
            List<ValidatedIterableRecord.Person> friends = Arrays.asList(
                    null,
                    new ValidatedIterableRecord.Person(null)
            );
            assertThat(validator.validate(new ValidatedIterableRecord(friends)))
                    .hasErrorCount(2)
                    .hasFieldErrorAt(FieldKey.of("friends", 0), "io.github.raniagus.javalidation.constraints.NotNull.message")
                    .hasFieldErrorAt(FieldKey.of("friends", 1, "name"), "io.github.raniagus.javalidation.constraints.NotNull.message");
        }
    }

    @Nested
    class NestedIterableRecordValidatorTest {
        private final Validator<NestedIterableRecord> validator = new NestedIterableRecordValidator();

        @Test
        void nullScores_hasFieldError() {
            assertThat(validator.validate(new NestedIterableRecord(null)))
                    .hasErrorCount(1)
                    .hasFieldError("scores", "io.github.raniagus.javalidation.constraints.NotEmpty.message");
        }

        @Test
        void emptyScores_noHasFieldError() {
            assertThat(validator.validate(new NestedIterableRecord(List.of())))
                    .hasErrorCount(1)
                    .hasFieldError("scores", "io.github.raniagus.javalidation.constraints.NotEmpty.message");
        }

        @Test
        void emptyInnerList_hasFieldError() {
            assertThat(validator.validate(new NestedIterableRecord(List.of(List.of()))))
                    .hasErrorCount(1)
                    .hasFieldErrorAt(FieldKey.of("scores", 0), "io.github.raniagus.javalidation.constraints.NotEmpty.message");
        }

        @Test
        void nullInnerList_hasFieldError() {
            assertThat(validator.validate(new NestedIterableRecord(Collections.singletonList(null))))
                    .hasErrorCount(1)
                    .hasFieldErrorAt(FieldKey.of("scores", 0), "io.github.raniagus.javalidation.constraints.NotEmpty.message");
        }

        @Test
        void nullItemInInnerList_hasFieldError() {
            List<List<Integer>> scores = List.of(Collections.singletonList(null));
            assertThat(validator.validate(new NestedIterableRecord(scores)))
                    .hasErrorCount(1)
                    .hasFieldErrorAt(FieldKey.of("scores", 0, 0), "io.github.raniagus.javalidation.constraints.NotNull.message");
        }

        @Test
        void validScores_noErrors() {
            List<List<Integer>> scores = List.of(List.of(1, 2), List.of(3));
            assertThat(validator.validate(new NestedIterableRecord(scores)))
                    .isEmpty();
        }

        @Test
        void multipleErrors_allReported() {
            List<List<Integer>> scores = List.of(Arrays.asList(null, null), List.of());
            assertThat(validator.validate(new NestedIterableRecord(scores)))
                    .hasErrorCount(3)
                    .hasFieldErrorAt(FieldKey.of("scores", 0, 0), "io.github.raniagus.javalidation.constraints.NotNull.message")
                    .hasFieldErrorAt(FieldKey.of("scores", 0, 1), "io.github.raniagus.javalidation.constraints.NotNull.message")
                    .hasFieldErrorAt(FieldKey.of("scores", 1), "io.github.raniagus.javalidation.constraints.NotEmpty.message");
        }
    }

    @Nested
    class PrimitiveMapRecordValidatorTest {
        private final Validator<PrimitiveMapRecord> validator = new PrimitiveMapRecordValidator();

        @Test
        void nullTags_hasFieldError() {
            assertThat(validator.validate(new PrimitiveMapRecord(null)))
                    .hasErrorCount(1)
                    .hasFieldError("tags", "io.github.raniagus.javalidation.constraints.NotEmpty.message");
        }

        @Test
        void emptyTags_hasFieldError() {
            assertThat(validator.validate(new PrimitiveMapRecord(Map.of())))
                    .hasErrorCount(1)
                    .hasFieldError("tags", "io.github.raniagus.javalidation.constraints.NotEmpty.message");
        }

        @Test
        void validTags_noErrors() {
            assertThat(validator.validate(new PrimitiveMapRecord(Map.of("key", "value"))))
                    .isEmpty();
        }

        @Test
        void nullKey_hasFieldError() {
            assertThat(validator.validate(new PrimitiveMapRecord(Collections.singletonMap(null, "value"))))
                    .hasErrorCount(1)
                    .hasFieldError("tags", "io.github.raniagus.javalidation.constraints.NotNull.message");
        }

        @Test
        void nullValue_hasFieldError() {
            assertThat(validator.validate(new PrimitiveMapRecord(Collections.singletonMap("key", null))))
                    .hasErrorCount(1)
                    .hasFieldErrorAt(FieldKey.of("tags", "key"), "io.github.raniagus.javalidation.constraints.NotNull.message");
        }

        @Test
        void nullKeyStopsValueValidation() {
            assertThat(validator.validate(new PrimitiveMapRecord(Collections.singletonMap(null, null))))
                    .hasErrorCount(1)
                    .hasFieldError("tags", "io.github.raniagus.javalidation.constraints.NotNull.message");
        }

        @Test
        void multipleErrors_allReported() {
            Map<String, String> tags = new LinkedHashMap<>();
            tags.put("a", null);
            tags.put("b", "valid");
            tags.put("c", null);

            assertThat(validator.validate(new PrimitiveMapRecord(tags)))
                    .hasErrorCount(2)
                    .hasFieldErrorAt(FieldKey.of("tags", "a"), "io.github.raniagus.javalidation.constraints.NotNull.message")
                    .hasFieldErrorAt(FieldKey.of("tags", "c"), "io.github.raniagus.javalidation.constraints.NotNull.message");
        }
    }

    @Nested
    class NestedMapRecordValidatorTest {
        private final Validator<NestedMapRecord> validator = new NestedMapRecordValidator();

        @Test
        void nullScores_noErrors() {
            assertThat(validator.validate(new NestedMapRecord(null)))
                    .isEmpty();
        }

        @Test
        void emptyScores_noErrors() {
            assertThat(validator.validate(new NestedMapRecord(Map.of())))
                    .isEmpty();
        }

        @Test
        void nullOuterKey_hasFieldError() {
            assertThat(validator.validate(new NestedMapRecord(Collections.singletonMap(null, Map.of()))))
                    .hasErrorCount(1)
                    .hasFieldError("scores", "io.github.raniagus.javalidation.constraints.NotNull.message");
        }

        @Test
        void nullOuterValue_hasFieldError() {
            assertThat(validator.validate(new NestedMapRecord(Collections.singletonMap("a", null))))
                    .hasErrorCount(1)
                    .hasFieldErrorAt(FieldKey.of("scores", "a"), "io.github.raniagus.javalidation.constraints.NotEmpty.message");
        }

        @Test
        void emptyInnerMap_hasFieldError() {
            assertThat(validator.validate(new NestedMapRecord(Map.of("a", Map.of()))))
                    .hasErrorCount(1)
                    .hasFieldErrorAt(FieldKey.of("scores", "a"), "io.github.raniagus.javalidation.constraints.NotEmpty.message");
        }

        @Test
        void nullInnerKey_hasFieldError() {
            assertThat(validator.validate(new NestedMapRecord(Map.of("a", Collections.singletonMap(null, 1)))))
                    .hasErrorCount(1)
                    .hasFieldErrorAt(FieldKey.of("scores", "a"), "io.github.raniagus.javalidation.constraints.NotNull.message");
        }

        @Test
        void nullInnerValue_hasFieldError() {
            assertThat(validator.validate(new NestedMapRecord(Map.of("a", Collections.singletonMap("b", null)))))
                    .hasErrorCount(1)
                    .hasFieldErrorAt(FieldKey.of("scores", "a", "b"), "io.github.raniagus.javalidation.constraints.NotNull.message");
        }

        @Test
        void validScores_noErrors() {
            assertThat(validator.validate(new NestedMapRecord(Map.of("a", Map.of("b", 1, "c", 2)))))
                    .isEmpty();
        }

        @Test
        void nullOuterKeyStopsInnerValidation() {
            assertThat(validator.validate(new NestedMapRecord(Collections.singletonMap(null, null))))
                    .hasErrorCount(1)
                    .hasFieldError("scores", "io.github.raniagus.javalidation.constraints.NotNull.message");
        }

        @Test
        void multipleErrors_allReported() {
            Map<String, Map<String, Integer>> scores = new LinkedHashMap<>();
            scores.put("a", Collections.singletonMap(null, 1));
            scores.put("b", Collections.singletonMap("x", null));
            scores.put("c", null);

            assertThat(validator.validate(new NestedMapRecord(scores)))
                    .hasErrorCount(3)
                    .hasFieldErrorAt(FieldKey.of("scores", "a"), "io.github.raniagus.javalidation.constraints.NotNull.message")
                    .hasFieldErrorAt(FieldKey.of("scores", "b", "x"), "io.github.raniagus.javalidation.constraints.NotNull.message")
                    .hasFieldErrorAt(FieldKey.of("scores", "c"), "io.github.raniagus.javalidation.constraints.NotEmpty.message");
        }
    }

}
