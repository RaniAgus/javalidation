package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.validator.Validator;
import io.github.raniagus.javalidation.validator.ValidatorUtils;
import java.time.Instant;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class FutureOrPresentRecordValidator implements Validator<FutureOrPresentRecord> {
    @Override
    public ValidationErrors validate(FutureOrPresentRecord root) {
        Validation rootValidation = Validation.create();
        var value = root.value();
        var valueValidation = Validation.create();
        if (value != null) {
            if (!(ValidatorUtils.toInstant(value).isBefore(Instant.now()) == false)) {
                valueValidation.addRootError("must be a date in the present or in the future");
            }
        }
        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
        return rootValidation.finish();
    }
}
