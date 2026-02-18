package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import java.util.Objects;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class PatternRecordValidator implements Validator<PatternRecord> {
    @Override
    public void validate(Validation rootValidation, PatternRecord root) {
        rootValidation.validateField("value", valueValidation -> {
            var value = root.value();
            if (value != null) {
                if (!Objects.toString(value).matches("^[a-z]+$")) {
                    valueValidation.addRootError("must match \"{0}\"", "^[a-z]+$");
                }
            }
        });
    }
}
