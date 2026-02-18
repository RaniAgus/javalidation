package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import java.math.BigInteger;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class MaxBigIntegerRecordValidator implements Validator<MaxBigIntegerRecord> {
    @Override
    public void validate(Validation validation, MaxBigIntegerRecord root) {
        validation.validateField("value", () -> {
            var value = root.value();
            if (value != null) {
                if (!(value.compareTo(new BigInteger("100")) <= 0)) {
                    validation.addRootError("must be less than or equal to {0}", 100);
                }
            }
        });
    }
}
