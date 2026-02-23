package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class NotNullAndMinRecordValidator implements Validator<NotNullAndMinRecord> {
    @Override
    public void validate(Validation validation, NotNullAndMinRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) {
                validation.addError("io.github.raniagus.javalidation.constraints.NotNull.message");
                return;
            }
            if (!(value >= 10)) {
                validation.addError("io.github.raniagus.javalidation.constraints.Min.message", 10);
            }
        });
    }
}
