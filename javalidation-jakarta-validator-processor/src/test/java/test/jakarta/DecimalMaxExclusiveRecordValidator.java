package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class DecimalMaxExclusiveRecordValidator implements InitializableValidator<DecimalMaxExclusiveRecord> {

    private static final BigDecimal VALUE_LT_10_5 = new BigDecimal("10.5");

    @Override
    public void initialize(ValidatorsHolder holder) {
    }

    @Override
    public void validate(Validation validation, DecimalMaxExclusiveRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) return;
            if (!(value.compareTo(VALUE_LT_10_5) < 0)) {
                validation.addError("io.github.raniagus.javalidation.constraints.DecimalMax.exclusive.message", "10.5");
            }
        });
    }
}
