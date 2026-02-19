package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class NotBlankRecordValidator implements Validator<NotBlankRecord> {
    @Override
    public void validate(Validation validation, NotBlankRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null || value.isBlank()) {
                validation.addRootError("must not be blank");
                return;
            }
        });
    }
}
