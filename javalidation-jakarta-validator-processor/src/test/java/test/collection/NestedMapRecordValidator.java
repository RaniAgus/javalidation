package test.collection;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class NestedMapRecordValidator implements Validator<NestedMapRecord> {
    @Override
    public void validate(Validation validation, NestedMapRecord root) {
        validation.withField("scores", () -> {
            var scores = root.scores();
            if (scores == null) return;
            scores.forEach((scoresKey, scoresValue) -> {
                if (scoresKey == null) {
                    validation.addRootError("must not be null");
                    return;
                }
                validation.withField(scoresKey, () -> {
                    if (scoresValue == null || scoresValue.isEmpty()) {
                        validation.addRootError("must not be empty");
                        return;
                    }
                    scoresValue.forEach((scoresValueKey, scoresValueValue) -> {
                        if (scoresValueKey == null) {
                            validation.addRootError("must not be null");
                            return;
                        }
                        validation.withField(scoresValueKey, () -> {
                            if (scoresValueValue == null) {
                                validation.addRootError("must not be null");
                                return;
                            }
                        });
                    });
                });
            });
        });
    }
}
