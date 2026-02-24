package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class DigitsRecordValidator implements InitializableValidator<DigitsRecord> {

    @Override
    public void initialize(ValidatorsHolder holder) {
    }

    @Override
    public void validate(Validation validation, DigitsRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) return;
            var value_bd = new BigDecimal(value.toString()).stripTrailingZeros();
            if (!(value_bd.precision() - value_bd.scale() <= 5 && Math.max(value_bd.scale(), 0) <= 2)) {
                validation.addError("io.github.raniagus.javalidation.constraints.Digits.message", 5, 2);
            }
        });
    }
}
