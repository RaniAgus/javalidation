package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class NotNullAndSizeRecordValidator implements Validator<NotNullAndSizeRecord> {
    @Override
    public void validate(Validation validation, NotNullAndSizeRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) {
                validation.addRootError("must not be null");
                return;
            }
            if (value.length() < 3 || value.length() > 10) {
                validation.addRootError("size must be between {0} and {1}", 3, 10);
            }
        });
    }
}
