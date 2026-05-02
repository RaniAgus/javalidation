package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.Predicates;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import java.util.regex.Pattern;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class EmailRegexpFlagsRecordValidator implements InitializableValidator<EmailRegexpFlagsRecord> {
    private static final Pattern VALUE_REGEXP_PATTERN = Pattern.compile(".*example.*", Pattern.CASE_INSENSITIVE);

    @Override
    public void initialize(ValidatorsHolder holder) {
    }

    @Override
    public void validate(Validation validation, EmailRegexpFlagsRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) return;
            if (!Predicates.isEmail(value) || !VALUE_REGEXP_PATTERN.matcher(value).matches()) {
                validation.addError("io.github.raniagus.javalidation.constraints.Email.message");
            }
        });
    }
}
