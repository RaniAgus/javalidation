package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class DecimalMinInclusiveRecordValidator implements Validator<DecimalMinInclusiveRecord> {
    @Override
    public void validate(Validation rootValidation, DecimalMinInclusiveRecord root) {
        rootValidation.validateField("value", valueValidation -> {
            var value = root.value();
            if (value != null) {
                if (!(value.compareTo(new BigDecimal("10.5")) >= 0)) {
                    valueValidation.addRootError("must be greater than or equal to {0}", "10.5");
                }
            }
        });
    }
}
