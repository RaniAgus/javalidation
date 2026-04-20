package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import java.math.BigInteger;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class MaxBigIntegerRecordValidator implements InitializableValidator<MaxBigIntegerRecord> {

    private static final BigInteger VALUE_LE_100 = new BigInteger("100");

    @Override
    public void initialize(ValidatorsHolder holder) {
    }

    @Override
    public void validate(Validation validation, MaxBigIntegerRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) return;
            if (!(value.compareTo(VALUE_LE_100) <= 0)) {
                validation.addError("io.github.raniagus.javalidation.constraints.Max.message", 100);
            }
        });
    }
}
