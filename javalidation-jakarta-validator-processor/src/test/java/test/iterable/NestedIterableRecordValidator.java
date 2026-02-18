package test.iterable;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class NestedIterableRecordValidator implements Validator<NestedIterableRecord> {
    @Override
    public ValidationErrors validate(NestedIterableRecord root) {
        Validation rootValidation = Validation.create();

        var scores = root.scores();
        var scoresValidation = Validation.create();
        if (scores != null) {
            int scoresIndex = 0;
            for (var scoresItem : scores) {
                var scoresItemValidation = Validation.create();
                if (scoresItem == null || scoresItem.isEmpty()) {
                    scoresItemValidation.addRootError("must not be empty");
                }
                if (scoresItem != null) {
                    int scoresItemIndex = 0;
                    for (var scoresItemItem : scoresItem) {
                        var scoresItemItemValidation = Validation.create();
                        if (scoresItemItem == null) {
                            scoresItemItemValidation.addRootError("must not be null");
                        }
                        scoresItemValidation.addAll(scoresItemItemValidation.finish(), new Object[]{scoresItemIndex++});
                    }

                }
                scoresValidation.addAll(scoresItemValidation.finish(), new Object[]{scoresIndex++});
            }

        }
        rootValidation.addAll(scoresValidation.finish(), new Object[]{"scores"});

        return rootValidation.finish();
    }
}
