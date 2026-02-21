package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class SizeMapRecordValidator implements Validator<SizeMapRecord> {
    @Override
    public void validate(Validation validation, SizeMapRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) return;
            if (value.size() < 1 || value.size() > 10) {
                validation.addError("size must be between {0} and {1}", 1, 10);
            }
        });
    }
}
