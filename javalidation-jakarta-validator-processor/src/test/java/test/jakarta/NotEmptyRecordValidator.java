package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class NotEmptyRecordValidator implements InitializableValidator<NotEmptyRecord> {

    @Override
    public void initialize(ValidatorsHolder holder) {
    }

    @Override
    public void validate(Validation validation, NotEmptyRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null || value.isEmpty()) {
                validation.addError("io.github.raniagus.javalidation.constraints.NotEmpty.message");
                return;
            }
        });
    }
}
