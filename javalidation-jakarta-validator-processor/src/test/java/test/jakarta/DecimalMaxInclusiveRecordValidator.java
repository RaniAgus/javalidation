package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.validator.Validator;
import io.github.raniagus.javalidation.validator.ValidatorUtils;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class DecimalMaxInclusiveRecordValidator implements Validator<DecimalMaxInclusiveRecord> {
    @Override
    public ValidationErrors validate(DecimalMaxInclusiveRecord root) {
        Validation rootValidation = Validation.create();
        var value = root.value();
        var valueValidation = Validation.create();
        if (value != null) {
            if (!(ValidatorUtils.compare(value, "10.5") <= 0)) {
                valueValidation.addRootError("must be less than or equal to {0}", "10.5");
            }
        }
        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
        return rootValidation.finish();
    }
}
