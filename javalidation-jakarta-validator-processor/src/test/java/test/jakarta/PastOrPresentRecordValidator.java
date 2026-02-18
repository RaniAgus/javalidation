package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.validator.Validator;
import java.time.Instant;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class PastOrPresentRecordValidator implements Validator<PastOrPresentRecord> {
    @Override
    public ValidationErrors validate(PastOrPresentRecord root) {
        Validation rootValidation = Validation.create();
        var value = root.value();
        var valueValidation = Validation.create();
        if (value != null) {
            if (!(value.isAfter(Instant.now()) == false)) {
                valueValidation.addRootError("must be a date in the past or in the present");
            }
        }
        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
        return rootValidation.finish();
    }
}
