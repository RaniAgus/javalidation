package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class PositiveOrZeroReferenceRecordValidator implements Validator<PositiveOrZeroReferenceRecord> {
    @Override
    public void validate(Validation rootValidation, PositiveOrZeroReferenceRecord root) {
        rootValidation.validateField("value", valueValidation -> {
            var value = root.value();
            if (value != null) {
                if (!(value >= 0)) {
                    valueValidation.addRootError("must be greater than or equal to 0");
                }
            }
        });
    }
}
