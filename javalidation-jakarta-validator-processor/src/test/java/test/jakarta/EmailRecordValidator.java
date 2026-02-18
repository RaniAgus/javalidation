package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import java.util.Objects;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class EmailRecordValidator implements Validator<EmailRecord> {
    @Override
    public void validate(Validation validation, EmailRecord root) {
        validation.validateField("value", () -> {
            var value = root.value();
            if (value != null) {
                if (!Objects.toString(value).matches("^[^@]+@[^@]+\\.[^@]+$")) {
                    validation.addRootError("must be a well-formed email address");
                }
            }
        });
    }
}
