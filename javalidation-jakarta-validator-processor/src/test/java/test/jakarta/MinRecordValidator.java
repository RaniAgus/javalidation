package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class MinRecordValidator implements InitializableValidator<MinRecord> {

    @Override
    public void initialize(ValidatorsHolder holder) {
    }

    @Override
    public void validate(Validation validation, MinRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (!(value >= 10)) {
                validation.addError("io.github.raniagus.javalidation.constraints.Min.message", 10);
            }
        });
    }
}
