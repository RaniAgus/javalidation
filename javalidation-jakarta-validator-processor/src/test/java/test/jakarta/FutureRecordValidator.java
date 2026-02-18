package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import java.time.Instant;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class FutureRecordValidator implements Validator<FutureRecord> {
    @Override
    public void validate(Validation rootValidation, FutureRecord root) {
        rootValidation.validateField("value", valueValidation -> {
            var value = root.value();
            if (value != null) {
                if (!(value.isAfter(Instant.now()) == true)) {
                    valueValidation.addRootError("must be a future date");
                }
            }
        });
    }
}
