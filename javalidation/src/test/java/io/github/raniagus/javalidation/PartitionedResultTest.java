package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PartitionedResultTest {

    @Test
    void givenEmptyErrors_whenHasErrors_thenReturnsFalse() {
        var partitioned = new PartitionedResult<>("value", ValidationErrors.empty());

        assertThat(partitioned.hasErrors()).isFalse();
    }

    @Test
    void givenNonEmptyErrors_whenHasErrors_thenReturnsTrue() {
        var errors = ValidationErrors.of("error");
        var partitioned = new PartitionedResult<>("value", errors);

        assertThat(partitioned.hasErrors()).isTrue();
    }

    @Test
    void givenEmptyErrors_whenToResult_thenReturnsOk() {
        var partitioned = new PartitionedResult<>("success", ValidationErrors.empty());

        var result = partitioned.toResult();

        assertThat(result.getOrThrow()).isEqualTo("success");
    }

    @Test
    void givenNonEmptyErrors_whenToResult_thenReturnsErr() {
        var errors = ValidationErrors.of("validation failed");
        var partitioned = new PartitionedResult<>("value", errors);

        var result = partitioned.toResult();

        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
        assertThat(result.getErrors()).isEqualTo(errors);
    }
}
