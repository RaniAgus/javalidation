package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.validator.Validator;
import java.time.Instant;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class PastRecordValidator implements Validator<PastRecord> {
    @Override
    public ValidationErrors validate(PastRecord root) {
        Validation rootValidation = Validation.create();
        var value = root.value();
        var valueValidation = Validation.create();
        if (value != null) {
            if (!(value.isBefore(Instant.now()) == true)) {
                valueValidation.addRootError("must be a past date");
            }
        }
        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
        return rootValidation.finish();
    }
}
