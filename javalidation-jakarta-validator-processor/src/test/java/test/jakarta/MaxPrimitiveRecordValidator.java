package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class MaxPrimitiveRecordValidator implements Validator<MaxPrimitiveRecord> {
    @Override
    public void validate(Validation rootValidation, MaxPrimitiveRecord root) {
        rootValidation.validateField("value", valueValidation -> {
            var value = root.value();
            if (!(value <= 100)) {
                valueValidation.addRootError("must be less than or equal to {0}", 100);
            }
        });
    }
}
