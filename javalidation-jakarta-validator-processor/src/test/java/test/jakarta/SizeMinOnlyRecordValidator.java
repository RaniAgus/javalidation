package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class SizeMinOnlyRecordValidator implements Validator<SizeMinOnlyRecord> {
    @Override
    public void validate(Validation validation, SizeMinOnlyRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) return;
            if (value.length() < 1 || value.length() > 2147483647) {
                validation.addError("size must be between {0} and {1}", 1, 2147483647);
            }
        });
    }
}
