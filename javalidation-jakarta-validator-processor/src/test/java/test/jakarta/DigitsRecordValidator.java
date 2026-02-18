package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.validator.Validator;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class DigitsRecordValidator implements Validator<DigitsRecord> {
    @Override
    public ValidationErrors validate(DigitsRecord root) {
        Validation rootValidation = Validation.create();
        var value = root.value();
        var valueValidation = Validation.create();
        if (value != null) {
            var value_bd = new BigDecimal(value.toString()).stripTrailingZeros();
            if (!(value_bd.precision() - value_bd.scale() <= 5 && Math.max(value_bd.scale(), 0) <= 2)) {
                valueValidation.addRootError("numeric value out of bounds ({0} digits, {1} decimal digits expected)", 5, 2);
            }
        }
        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
        return rootValidation.finish();
    }
}
