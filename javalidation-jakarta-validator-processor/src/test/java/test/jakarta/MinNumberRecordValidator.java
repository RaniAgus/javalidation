package test.jakarta;

import io.github.raniagus.javalidation.Constraint;
import io.github.raniagus.javalidation.Constraints;
import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class MinNumberRecordValidator implements InitializableValidator<MinNumberRecord> {
    private static final Constraint<Number> VALUE_GE_10 = Constraints.minNumber(10L);

    
    @Override
    public void initialize(ValidatorsHolder holder) {
    }
    
    @Override
    public void validate(Validation validation, MinNumberRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) return;
            VALUE_GE_10.validate(validation, value);
        });
    }
}
