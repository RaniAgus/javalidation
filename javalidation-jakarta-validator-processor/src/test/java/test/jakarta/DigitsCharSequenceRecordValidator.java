package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.Predicates;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import java.util.function.Predicate;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class DigitsCharSequenceRecordValidator implements InitializableValidator<DigitsCharSequenceRecord> {
    private static final Predicate<CharSequence> VALUE_DIGITS_PREDICATE = Predicates.digits(5, 2);

    @Override
    public void initialize(ValidatorsHolder holder) {
    }

    @Override
    public void validate(Validation validation, DigitsCharSequenceRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null) return;
            if (!VALUE_DIGITS_PREDICATE.test(value)) {
                validation.addError("io.github.raniagus.javalidation.constraints.Digits.message", 5, 2);
            }
        });
    }
}

