package test.jakarta;

import io.github.raniagus.javalidation.Constraint;
import io.github.raniagus.javalidation.Constraints;
import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import java.time.Instant;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class PastOrPresentRecordValidator implements InitializableValidator<PastOrPresentRecord> {
    private static final Constraint<Instant> VALUE_PAST_OR_PRESENT = Constraints.pastOrPresent(Instant::now);

    
    @Override
    public void initialize(ValidatorsHolder holder) {
    }
    
    @Override
    public void validate(Validation validation, PastOrPresentRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) return;
            VALUE_PAST_OR_PRESENT.validate(validation, value);
        });
    }
}
