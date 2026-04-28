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
public class NotNullAndMinRecordValidator implements InitializableValidator<NotNullAndMinRecord> {
    private static final Constraint<Long> VALUE_GE_10 = Constraints.minLong(10L);

    
    @Override
    public void initialize(ValidatorsHolder holder) {
    }
    
    @Override
    public void validate(Validation validation, NotNullAndMinRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) {
                validation.addError("io.github.raniagus.javalidation.constraints.NotNull.message");
                return;
            }
            VALUE_GE_10.validate(validation, (long) value);
        });
    }
}
