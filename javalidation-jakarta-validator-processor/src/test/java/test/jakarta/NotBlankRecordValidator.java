package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class NotBlankRecordValidator implements Validator<NotBlankRecord> {
    @Override
    public void validate(Validation rootValidation, NotBlankRecord root) {
        rootValidation.validateField("value", valueValidation -> {
            var value = root.value();
            if (value == null || value.isBlank()) {
                valueValidation.addRootError("must not be blank");
            }
        });
    }
}
