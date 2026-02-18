package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import java.time.Instant;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class PastOrPresentRecordValidator implements Validator<PastOrPresentRecord> {
    @Override
    public void validate(Validation rootValidation, PastOrPresentRecord root) {
        rootValidation.validateField("value", valueValidation -> {
            var value = root.value();
            if (value != null) {
                if (!(value.isAfter(Instant.now()) == false)) {
                    valueValidation.addRootError("must be a date in the past or in the present");
                }
            }
        });
    }
}
