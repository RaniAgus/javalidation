package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import java.time.Instant;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class PastRecordValidator implements Validator<PastRecord> {
    @Override
    public void validate(Validation validation, PastRecord root) {
        validation.validateField("value", () -> {
            var value = root.value();
            if (value != null) {
                if (!(value.isBefore(Instant.now()) == true)) {
                    validation.addRootError("must be a past date");
                }
            }
        });
    }
}
