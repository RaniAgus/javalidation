package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import java.util.regex.Pattern;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class MultiPatternRecordValidator implements InitializableValidator<MultiPatternRecord> {
    private static final Pattern VALUE_PATTERN = Pattern.compile("^[a-z]+$");
    private static final Pattern VALUE_PATTERN_2 = Pattern.compile("^.{3,10}$");

    @Override
    public void initialize(ValidatorsHolder holder) {
    }

    @Override
    public void validate(Validation validation, MultiPatternRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) return;
            if (!VALUE_PATTERN.matcher(value.toString()).matches()) {
                validation.addError("io.github.raniagus.javalidation.constraints.Pattern.message", "^[a-z]+$");
            }
            if (!VALUE_PATTERN_2.matcher(value.toString()).matches()) {
                validation.addError("io.github.raniagus.javalidation.constraints.Pattern.message", "^.{3,10}$");
            }
        });
    }
}
