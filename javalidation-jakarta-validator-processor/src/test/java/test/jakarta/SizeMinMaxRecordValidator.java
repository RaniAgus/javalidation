package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class SizeMinMaxRecordValidator implements Validator<SizeMinMaxRecord> {
    @Override
    public void validate(Validation validation, SizeMinMaxRecord root) {
        validation.validateField("value", () -> {
            var value = root.value();
            if (value != null) {
                if (value.length() < 1 || value.length() > 10) {
                    validation.addRootError("size must be between {0} and {1}", 1, 10);
                }
            }
        });
    }
}
