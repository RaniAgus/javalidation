package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class PositivePrimitiveRecordValidator implements Validator<PositivePrimitiveRecord> {
    @Override
    public void validate(Validation validation, PositivePrimitiveRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (!(value > 0)) {
                validation.addRootError("must be greater than 0");
            }
        });
    }
}
