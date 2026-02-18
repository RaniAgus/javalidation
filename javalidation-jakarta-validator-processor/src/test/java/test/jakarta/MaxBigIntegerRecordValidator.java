package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.validator.Validator;
import java.math.BigInteger;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class MaxBigIntegerRecordValidator implements Validator<MaxBigIntegerRecord> {
    @Override
    public ValidationErrors validate(MaxBigIntegerRecord root) {
        Validation rootValidation = Validation.create();
        var value = root.value();
        var valueValidation = Validation.create();
        if (value != null) {
            if (!(value.compareTo(new BigInteger("100")) <= 0)) {
                valueValidation.addRootError("must be less than or equal to {0}", 100);
            }
        }
        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
        return rootValidation.finish();
    }
}
