package test.iterable;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class NestedIterableRecordValidator implements Validator<NestedIterableRecord> {
    @Override
    public void validate(Validation validation, NestedIterableRecord root) {
        validation.validateField("scores", () -> {
            var scores = root.scores();
            if (scores != null) {
                int scoresIndex = 0;
                for (var scoresItem : scores) {
                    validation.validateField(scoresIndex++, () -> {
                        if (scoresItem == null || scoresItem.isEmpty()) {
                            validation.addRootError("must not be empty");
                        }
                        if (scoresItem != null) {
                            int scoresItemIndex = 0;
                            for (var scoresItemItem : scoresItem) {
                                validation.validateField(scoresItemIndex++, () -> {
                                    if (scoresItemItem == null) {
                                        validation.addRootError("must not be null");
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }
}
