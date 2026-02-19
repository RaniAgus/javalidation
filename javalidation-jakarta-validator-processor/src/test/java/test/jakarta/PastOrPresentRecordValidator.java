package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import java.time.Instant;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class PastOrPresentRecordValidator implements Validator<PastOrPresentRecord> {
    @Override
    public void validate(Validation validation, PastOrPresentRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) return;
            if (!(value.isAfter(Instant.now()) == false)) {
                validation.addRootError("must be a date in the past or in the present");
            }
        });
    }
}
