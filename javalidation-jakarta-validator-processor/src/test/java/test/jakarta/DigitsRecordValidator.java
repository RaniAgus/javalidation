package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.Predicates;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
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
            if (!Predicates.digits(value, 5, 2)) {
                validation.addError("io.github.raniagus.javalidation.constraints.Digits.message", 5, 2);
            }
        });
    }
}
