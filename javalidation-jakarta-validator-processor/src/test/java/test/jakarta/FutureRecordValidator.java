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
public class FutureRecordValidator implements Validator<FutureRecord> {
    @Override
    public ValidationErrors validate(FutureRecord root) {
        Validation rootValidation = Validation.create();
        var value = root.value();
        var valueValidation = Validation.create();
        if (value != null) {
            if (!(ValidatorUtils.toInstant(value).isAfter(Instant.now()) == true)) {
                valueValidation.addRootError("must be a future date");
            }
        }
        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
        return rootValidation.finish();
    }
}
