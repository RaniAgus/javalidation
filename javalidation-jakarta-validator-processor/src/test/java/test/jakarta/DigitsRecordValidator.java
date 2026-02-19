package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class DigitsRecordValidator implements Validator<DigitsRecord> {
    @Override
    public void validate(Validation validation, DigitsRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) return;
            var value_bd = new BigDecimal(value.toString()).stripTrailingZeros();
            if (!(value_bd.precision() - value_bd.scale() <= 5 && Math.max(value_bd.scale(), 0) <= 2)) {
                validation.addRootError("numeric value out of bounds ({0} digits, {1} decimal digits expected)", 5, 2);
            }
        });
    }
}
