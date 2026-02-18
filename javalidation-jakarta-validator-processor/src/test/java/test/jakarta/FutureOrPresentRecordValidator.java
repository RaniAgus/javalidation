package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import java.time.Instant;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class FutureOrPresentRecordValidator implements Validator<FutureOrPresentRecord> {
    @Override
    public void validate(Validation rootValidation, FutureOrPresentRecord root) {
        rootValidation.validateField("value", valueValidation -> {
            var value = root.value();
            if (value != null) {
                if (!(value.isBefore(Instant.now()) == false)) {
                    valueValidation.addRootError("must be a date in the present or in the future");
                }
            }
        });
    }
}
