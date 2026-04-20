package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class MinNumberRecordValidator implements InitializableValidator<MinNumberRecord> {

    private static final BigDecimal VALUE_GE_10 = new BigDecimal("10");

    @Override
    public void initialize(ValidatorsHolder holder) {
    }

    @Override
    public void validate(Validation validation, MinNumberRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) return;
            if (!(new BigDecimal(value.toString()).compareTo(VALUE_GE_10) >= 0)) {
                validation.addError("io.github.raniagus.javalidation.constraints.Min.message", 10);
            }
        });
    }
}
