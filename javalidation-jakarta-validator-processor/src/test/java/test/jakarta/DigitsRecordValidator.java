package test.jakarta;

import io.github.raniagus.javalidation.Constraint;
import io.github.raniagus.javalidation.Constraints;
import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class DigitsRecordValidator implements InitializableValidator<DigitsRecord> {
    private static final Constraint<BigDecimal> VALUE_DIGITS = Constraints.digits(5, 2);

    
    @Override
    public void initialize(ValidatorsHolder holder) {
    }
    
    @Override
    public void validate(Validation validation, DigitsRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) return;
            VALUE_DIGITS.validate(validation, value);
        });
    }
}
