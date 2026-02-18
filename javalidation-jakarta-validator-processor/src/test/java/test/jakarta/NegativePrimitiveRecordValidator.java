package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class NegativePrimitiveRecordValidator implements Validator<NegativePrimitiveRecord> {
    @Override
    public void validate(Validation rootValidation, NegativePrimitiveRecord root) {
        rootValidation.validateField("value", valueValidation -> {
            var value = root.value();
            if (!(value < 0)) {
                valueValidation.addRootError("must be less than 0");
            }
        });
    }
}
