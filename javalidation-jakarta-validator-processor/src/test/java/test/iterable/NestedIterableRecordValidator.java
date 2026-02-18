package test.iterable;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class NestedIterableRecordValidator implements Validator<NestedIterableRecord> {
    @Override
    public void validate(Validation rootValidation, NestedIterableRecord root) {
        rootValidation.validateField("scores", scoresValidation -> {
            var scores = root.scores();
            if (scores != null) {
                int scoresIndex = 0;
                for (var scoresItem : scores) {
                    scoresValidation.validateField(scoresIndex++, scoresItemValidation -> {
                        if (scoresItem == null || scoresItem.isEmpty()) {
                            scoresItemValidation.addRootError("must not be empty");
                        }
                        if (scoresItem != null) {
                            int scoresItemIndex = 0;
                            for (var scoresItemItem : scoresItem) {
                                scoresItemValidation.validateField(scoresItemIndex++, scoresItemItemValidation -> {
                                    if (scoresItemItem == null) {
                                        scoresItemItemValidation.addRootError("must not be null");
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
