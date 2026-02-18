package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class NotNullAndSizeRecordValidator implements Validator<NotNullAndSizeRecord> {
    @Override
    public ValidationErrors validate(NotNullAndSizeRecord root) {
        Validation rootValidation = Validation.create();
        var value = root.value();
        var valueValidation = Validation.create();
        if (value == null) {
            valueValidation.addRootError("must not be null");
        }
        if (value != null) {
            if (value.length() < 3 || value.length() > 10) {
                valueValidation.addRootError("size must be between {0} and {1}", 3, 10);
            }
        }
        rootValidation.addAll(valueValidation.finish(), new Object[]{"value"});
        return rootValidation.finish();
    }
}
