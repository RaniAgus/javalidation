package test.collection;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class NestedMapRecordValidator implements InitializableValidator<NestedMapRecord> {

    @Override
    public void initialize(ValidatorsHolder holder) {
    }

    @Override
    public void validate(Validation validation, NestedMapRecord root) {
        validation.withField("scores", () -> {
            var scores = root.scores();
            if (scores == null) return;
            scores.forEach((scoresKey, scoresValue) -> {
                if (scoresKey == null) {
                    validation.addError("io.github.raniagus.javalidation.constraints.NotNull.message");
                    return;
                }
                validation.withField(scoresKey, () -> {
                    if (scoresValue == null || scoresValue.isEmpty()) {
                        validation.addError("io.github.raniagus.javalidation.constraints.NotEmpty.message");
                        return;
                    }
                    scoresValue.forEach((scoresValueKey, scoresValueValue) -> {
                        if (scoresValueKey == null) {
                            validation.addError("io.github.raniagus.javalidation.constraints.NotNull.message");
                            return;
                        }
                        validation.withField(scoresValueKey, () -> {
                            if (scoresValueValue == null) {
                                validation.addError("io.github.raniagus.javalidation.constraints.NotNull.message");
                                return;
                            }
                        });
                    });
                });
            });
        });
    }
}
