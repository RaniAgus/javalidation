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
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) return;
            if (value.length() < 1 || value.length() > 10) {
                validation.addError("io.github.raniagus.javalidation.constraints.Size.message", 1, 10);
            }
        });
    }
}
