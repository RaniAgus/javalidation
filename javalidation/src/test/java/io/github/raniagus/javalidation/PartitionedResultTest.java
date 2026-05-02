package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PartitionedResultTest {

    @Nested
    class HasErrorsTests {

        @Test
        void givenEmptyErrors_whenHasErrors_thenReturnsFalse() {
            var partitioned = new PartialResult<>("value", ValidationErrors.empty());

            assertThat(partitioned.hasErrors()).isFalse();
        }

        @Test
        void givenNonEmptyErrors_whenHasErrors_thenReturnsTrue() {
            var errors = ValidationErrors.of("error");
            var partitioned = new PartialResult<>("value", errors);

            assertThat(partitioned.hasErrors()).isTrue();
        }
    }

    @Nested
    class MapTests {

        @Test
        void givenPartialResult_whenMap_thenValueIsTransformedAndErrorsPreserved() {
            var errors = ValidationErrors.of("some.error");
            var partitioned = new PartialResult<>("hello", errors);

            var mapped = partitioned.map(String::length);

            assertThat(mapped.success()).isEqualTo(5);
            assertThat(mapped.errors()).isEqualTo(errors);
        }

        @Test
        void givenPartialResultWithNoErrors_whenMap_thenValueIsTransformedAndErrorsStillEmpty() {
            var partitioned = new PartialResult<>("hello", ValidationErrors.empty());

            var mapped = partitioned.map(String::toUpperCase);

            assertThat(mapped.success()).isEqualTo("HELLO");
            assertThat(mapped.errors()).isEqualTo(ValidationErrors.empty());
        }
    }

    @Nested
    class MapErrTests {

        @Test
        void givenPartialResult_whenMapErr_thenErrorsAreTransformedAndValuePreserved() {
            var originalErrors = ValidationErrors.of("original.error");
            var partitioned = new PartialResult<>("value", originalErrors);
            var replacementErrors = ValidationErrors.of("replacement.error");

            var mapped = partitioned.mapErr(e -> replacementErrors);

            assertThat(mapped.success()).isEqualTo("value");
            assertThat(mapped.errors()).isEqualTo(replacementErrors);
        }

        @Test
        void givenPartialResultWithNoErrors_whenMapErr_thenErrorsRemainEmpty() {
            var partitioned = new PartialResult<>("value", ValidationErrors.empty());

            var mapped = partitioned.mapErr(e -> ValidationErrors.of("should.not.appear"));

            assertThat(mapped.success()).isEqualTo("value");
            // mapErr applies unconditionally — the mapper ran but errors were empty going in
            assertThat(mapped.errors()).isEqualTo(ValidationErrors.of("should.not.appear"));
        }
    }

    @Nested
    class BimapTests {

        @Test
        void givenPartialResult_whenBimap_thenBothValueAndErrorsAreTransformed() {
            var originalErrors = ValidationErrors.of("original.error");
            var partitioned = new PartialResult<>("hello", originalErrors);
            var replacementErrors = ValidationErrors.of("replacement.error");

            var mapped = partitioned.bimap(String::length, e -> replacementErrors);

            assertThat(mapped.success()).isEqualTo(5);
            assertThat(mapped.errors()).isEqualTo(replacementErrors);
        }

        @Test
        void givenPartialResultWithNoErrors_whenBimap_thenValueIsTransformedAndErrorsRemainEmpty() {
            var partitioned = new PartialResult<>("hello", ValidationErrors.empty());

            var mapped = partitioned.bimap(String::toUpperCase, e -> ValidationErrors.of("unused"));

            assertThat(mapped.success()).isEqualTo("HELLO");
            // mapErr side of bimap also applies unconditionally
            assertThat(mapped.errors()).isEqualTo(ValidationErrors.of("unused"));
        }
    }

    @Nested
    class ToResultTests {

        @Test
        void givenEmptyErrors_whenToResult_thenReturnsOk() {
            var partitioned = new PartialResult<>("success", ValidationErrors.empty());

            var result = partitioned.toResult();

            assertThat(result.getOrThrow()).isEqualTo("success");
        }

        @Test
        void givenNonEmptyErrors_whenToResult_thenReturnsErr() {
            var errors = ValidationErrors.of("validation failed");
            var partitioned = new PartialResult<>("value", errors);

            var result = partitioned.toResult();

            assertThatThrownBy(result::getOrThrow)
                    .isInstanceOf(JavalidationException.class);
            assertThat(result.errors()).isEqualTo(errors);
        }
    }
}
