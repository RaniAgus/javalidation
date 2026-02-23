package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class DecimalMaxInclusiveRecordValidator implements Validator<DecimalMaxInclusiveRecord> {
    @Override
    public void validate(Validation validation, DecimalMaxInclusiveRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) return;
            if (!(value.compareTo(new BigDecimal("10.5")) <= 0)) {
                validation.addError("io.github.raniagus.javalidation.constraints.DecimalMax.message", "10.5");
            }
        });
    }
}
