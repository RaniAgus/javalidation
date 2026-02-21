package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class NegativeReferenceRecordValidator implements Validator<NegativeReferenceRecord> {
    @Override
    public void validate(Validation validation, NegativeReferenceRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) return;
            if (!(value < 0)) {
                validation.addError("must be less than 0");
            }
        });
    }
}
