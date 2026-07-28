package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import java.util.regex.Pattern;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class PatternRecordValidator implements InitializableValidator<PatternRecord> {
    private static final Pattern VALUE_PATTERN = Pattern.compile("^[\\p{IsLatin}\\p{M}]+$");

    @Override
    public void initialize(ValidatorsHolder holder) {
    }

    @Override
    public void validate(Validation validation, PatternRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) return;
            if (!VALUE_PATTERN.matcher(value.toString()).matches()) {
                validation.addError("io.github.raniagus.javalidation.constraints.Pattern.message", "^[\\p{IsLatin}\\p{M}]+$");
            }
        });
    }
}
