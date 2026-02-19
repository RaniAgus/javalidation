package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ValidationTest {

    // -- addRootError --

    @Test
    void givenMessage_whenAddRootError_thenAddsError() {
        var validation = Validation.create()
                .addRootError("error message");

        var errors = validation.finish();
        assertThat(errors.rootErrors()).containsExactly(TemplateString.of("error message"));
    }

    @Test
    void givenMultipleCalls_whenAddRootError_thenAccumulatesErrors() {
        var validation = Validation.create()
                .addRootError("error 1")
                .addRootError("error 2");

        var errors = validation.finish();
        assertThat(errors.rootErrors()).containsExactly(
                TemplateString.of("error 1"),
                TemplateString.of("error 2")
        );
    }

    @Test
    void givenNullMessage_whenAddRootError_thenThrowsNullPointerException() {
        var validation = Validation.create();

        assertThatThrownBy(() -> validation.addRootError(null))
                .isInstanceOf(NullPointerException.class);
    }

    // -- addFieldError --

    @Test
    void givenFieldAndMessage_whenAddFieldError_thenAddsError() {
        var validation = Validation.create()
                .addFieldError("field", "error message");

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("field"));
        assertThat(errors.fieldErrors().get(FieldKey.of("field"))).containsExactly(TemplateString.of("error message"));
    }

    @Test
    void givenSameFieldMultipleTimes_whenAddFieldError_thenAccumulatesErrors() {
        var validation = Validation.create()
                .addFieldError("field", "error 1")
                .addFieldError("field", "error 2");

        var errors = validation.finish();
        assertThat(errors.fieldErrors().get(FieldKey.of("field"))).containsExactly(
                TemplateString.of("error 1"),
                TemplateString.of("error 2")
        );
    }

    @Test
    void givenDifferentFields_whenAddFieldError_thenAddsToEachField() {
        var validation = Validation.create()
                .addFieldError("field1", "error 1")
                .addFieldError("field2", "error 2");

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsOnlyKeys(FieldKey.of("field1"), FieldKey.of("field2"));
    }

    @Test
    void givenNullMessage_whenAddFieldError_thenThrowsNullPointerException() {
        var validation = Validation.create();

        assertThatThrownBy(() -> validation.addFieldError("field", null))
                .isInstanceOf(NullPointerException.class);
    }

    // -- addAll(Validation) --

    @Test
    void givenValidation_whenAddAll_thenAddsAllErrors() {
        var validation = Validation.create()
                .addFieldError("field", "error");
        var validation2 = Validation.create()
                .addRootError("root error");

        var validation3 = Validation.create()
                .addAll(validation)
                .addAll(validation2);

        var errors = validation3.finish();
        assertThat(errors.fieldErrors()).containsEntry(FieldKey.of("field"), List.of(TemplateString.of("error")));
        assertThat(errors.rootErrors()).containsExactly(TemplateString.of("root error"));
    }

    @Test
    void givenValidation_whenAddAllScoped_thenAddsAllErrors() {
        var validation = Validation.create()
                .addFieldError("field", "error");
        var validation2 = Validation.create()
                .addRootError("root error");

        var validation3 = Validation.create();
        validation3.withField("parent", () -> {
            validation3
                    .addAll(validation)
                    .addAll(validation2);
        });

        var errors = validation3.finish();
        assertThat(errors.rootErrors()).isEmpty();
        assertThat(errors.fieldErrors())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        FieldKey.of("parent", "field"), List.of(TemplateString.of("error")),
                        FieldKey.of("parent"), List.of(TemplateString.of("root error"))
                ));
    }

    // -- addAll(ValidationErrors) --

    @Test
    void givenValidationErrors_whenAddAll_thenAddsAllErrors() {
        var validationErrors = ValidationErrors.ofField("field", "error");
        var validation = Validation.create()
                .addAll(validationErrors);

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("field"));
    }

    @Test
    void givenNull_whenAddAll_thenThrowsNullPointerException() {
        var validation = Validation.create();

        assertThatThrownBy(() -> validation.addAll((ValidationErrors) null))
                .isInstanceOf(NullPointerException.class);
    }

    // -- addAll(String, ValidationErrors) --

    @Test
    void givenFieldErrors_whenAddAllWithPrefix_thenPrefixesFieldNames() {
        var validationErrors = ValidationErrors.ofField("field", "error");
        var validation = Validation.create()
                .addAll(validationErrors, new Object[]{"root"});

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("root", "field"));
    }

    @Test
    void givenRootErrors_whenAddAllWithPrefix_thenConvertsToFieldErrors() {
        var validationErrors = ValidationErrors.ofRoot("root error");
        var validation = Validation.create()
                .addAll(validationErrors, new Object[]{"prefix"});

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("prefix"));
    }

    // -- finish --

    @Test
    void givenErrors_whenFinish_thenReturnsValidationErrors() {
        var validation = Validation.create()
                .addFieldError("field", "error");

        var errors = validation.finish();
        assertThat(errors).isNotNull();
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("field"));
    }

    @Test
    void givenNoErrors_whenFinish_thenReturnsEmpty() {
        var validation = Validation.create();

        var errors = validation.finish();
        assertThat(errors.isEmpty()).isTrue();
    }

    // -- asResult --

    @Test
    void givenNoErrors_whenAsResult_thenReturnsOk() {
        var validation = Validation.create();

        var result = validation.asResult(() -> "value");
        assertThat(result.getOrThrow()).isEqualTo("value");
    }

    @Test
    void givenErrors_whenAsResult_thenReturnsErr() {
        var validation = Validation.create()
                .addFieldError("field", "error");

        var result = validation.asResult(() -> "value");
        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    @Test
    void givenSupplierThrows_whenAsResult_thenReturnsErr() {
        var validation = Validation.create();

        var result = validation.asResult(() -> {
            throw JavalidationException.ofRoot("error");
        });
        assertThatThrownBy(result::getOrThrow)
                .isInstanceOf(JavalidationException.class);
    }

    // -- check --

    @Test
    void givenNoErrors_whenCheck_thenDoesNotThrow() {
        var validation = Validation.create();

        assertThatCode(validation::check).doesNotThrowAnyException();
    }

    @Test
    void givenErrors_whenCheck_thenThrowsValidationException() {
        var validation = Validation.create()
                .addFieldError("field", "error");

        assertThatThrownBy(validation::check)
                .isInstanceOf(JavalidationException.class);
    }

    // -- checkAndGet --

    @Test
    void givenNoErrors_whenCheckAndGet_thenReturnsValue() {
        var validation = Validation.create();

        var result = validation.checkAndGet(() -> "value");
        assertThat(result).isEqualTo("value");
    }

    @Test
    void givenErrors_whenCheckAndGet_thenThrowsValidationException() {
        var validation = Validation.create()
                .addFieldError("field", "error");

        assertThatThrownBy(() -> validation.checkAndGet(() -> "value"))
                .isInstanceOf(JavalidationException.class);
    }

    // -- withField --

    @Test
    void givenRootErrorInConsumer_whenWithField_thenConvertsToFieldError() {
        record Person(String name, int age) {}
        record Request(Person person) {}

        Request request = new Request(null);
        var validation = Validation.create();

        validation.withField("person", () -> {
            if (request.person() == null) {
                validation.addRootError("must not be null");
            }
        });

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("person"));
        assertThat(errors.fieldErrors().get(FieldKey.of("person")))
                .containsExactly(TemplateString.of("must not be null"));
    }

    @Test
    void givenFieldErrorInConsumer_whenWithField_thenPrefixesFieldName() {
        record Person(String name, int age) {}
        record Request(Person person) {}

        Request request = new Request(new Person(null, 15));
        var validation = Validation.create();

        validation.withField("person", () -> {
            if (request.person().name() == null) {
                validation.addFieldError("name", "must not be null");
            }
            if (request.person().age() < 18) {
                validation.addFieldError("age", "must be at least 18");
            }
        });

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsKeys(
                FieldKey.of("person", "name"),
                FieldKey.of("person", "age")
        );
        assertThat(errors.fieldErrors().get(FieldKey.of("person", "name")))
                .containsExactly(TemplateString.of("must not be null"));
        assertThat(errors.fieldErrors().get(FieldKey.of("person", "age")))
                .containsExactly(TemplateString.of("must be at least 18"));
    }

    @Test
    void givenNestedWithField_whenWithField_thenCreatesNestedPrefix() {
        var validation = Validation.create();

        validation.withField("address", () -> {
            validation.withField("street", () -> {
                validation.addRootError("required");
            });
        });

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("address", "street"));
        assertThat(errors.fieldErrors().get(FieldKey.of("address", "street")))
                .containsExactly(TemplateString.of("required"));
    }

    @Test
    void givenNullField_whenWithField_thenThrowsNullPointerException() {
        var validation = Validation.create();

        assertThatThrownBy(() -> validation.withField(null, () -> {}))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void givenNullConsumer_whenWithField_thenThrowsNullPointerException() {
        var validation = Validation.create();

        assertThatThrownBy(() -> validation.withField("field", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void givenNoErrorsInConsumer_whenWithField_thenNoErrorsAdded() {
        var validation = Validation.create();

        validation.withField("person", () -> {
            // No errors added
        });

        var errors = validation.finish();
        assertThat(errors.isEmpty()).isTrue();
    }

    // -- withEach --

    @Test
    void givenRootErrorInConsumer_whenWithEach_thenConvertsToIndexedFieldError() {
        record Tag(String name) {}

        var tags = List.of(new Tag(null), new Tag("valid"));
        var validation = Validation.create();

        validation.withEach(tags, tag -> {
            if (tag.name() == null) {
                validation.addRootError("must not be null");
            }
        });

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsOnlyKeys(FieldKey.of(0));
        assertThat(errors.fieldErrors().get(FieldKey.of(0)))
                .containsExactly(TemplateString.of("must not be null"));
    }

    @Test
    void givenFieldErrorInConsumer_whenWithEach_thenPrefixesWithIndex() {
        record Tag(String name) {}

        var tags = List.of(new Tag(null), new Tag(""));
        var validation = Validation.create();

        validation.withEach(tags, tag -> {
            if (tag.name() == null) {
                validation.addFieldError("name", "must not be null");
            } else if (tag.name().isBlank()) {
                validation.addFieldError("name", "must not be blank");
            }
        });

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsKeys(
                FieldKey.of(0, "name"),
                FieldKey.of(1, "name")
        );
        assertThat(errors.fieldErrors().get(FieldKey.of(0, "name")))
                .containsExactly(TemplateString.of("must not be null"));
        assertThat(errors.fieldErrors().get(FieldKey.of(1, "name")))
                .containsExactly(TemplateString.of("must not be blank"));
    }

    @Test
    void givenEmptyIterable_whenWithEach_thenNoErrorsAdded() {
        var validation = Validation.create();

        validation.withEach(List.of(), item -> {
            validation.addRootError("should not be called");
        });

        var errors = validation.finish();
        assertThat(errors.isEmpty()).isTrue();
    }

    @Test
    void givenNoErrorsInConsumer_whenWithEach_thenNoErrorsAdded() {
        var validation = Validation.create();

        validation.withEach(List.of("a", "b"), item -> {
            // No errors added
        });

        var errors = validation.finish();
        assertThat(errors.isEmpty()).isTrue();
    }

    @Test
    void givenNestedWithField_whenWithEach_thenCreatesNestedPrefix() {
        record Tag(String name) {}

        var tags = List.of(new Tag(null));
        var validation = Validation.create();

        validation.withField("tags", () -> {
            validation.withEach(tags, tag -> {
                if (tag.name() == null) {
                    validation.addRootError("must not be null");
                }
            });
        });

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsKey(FieldKey.of("tags", 0));
        assertThat(errors.fieldErrors().get(FieldKey.of("tags", 0)))
                .containsExactly(TemplateString.of("must not be null"));
    }

    @Test
    void givenMultipleErrorsOnSameElement_whenWithEach_thenAllErrorsAccumulated() {
        record Tag(String name, String color) {}

        var tags = List.of(new Tag(null, null));
        var validation = Validation.create();

        validation.withEach(tags, tag -> {
            if (tag.name() == null) {
                validation.addFieldError("name", "must not be null");
            }
            if (tag.color() == null) {
                validation.addFieldError("color", "must not be null");
            }
        });

        var errors = validation.finish();
        assertThat(errors.fieldErrors()).containsKeys(
                FieldKey.of(0, "name"),
                FieldKey.of(0, "color")
        );
    }

    @Test
    void givenNullItems_whenWithEach_thenThrowsNullPointerException() {
        var validation = Validation.create();

        assertThatThrownBy(() -> validation.withEach(null, item -> {}))
                .isInstanceOf(NullPointerException.class);
    }
}
