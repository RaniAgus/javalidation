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
    public void validate(Validation validation, FutureRecord root) {
        validation.validateField("value", () -> {
            var value = root.value();
            if (value != null) {
                if (!(value.isAfter(Instant.now()) == true)) {
                    validation.addRootError("must be a future date");
                }
            }
        });
    }
}
