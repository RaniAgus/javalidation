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
        validation.withField("scores", () -> {
            var scores = root.scores();
            if (scores == null) return;
            validation.withEach(scores, scoresItem -> {
                if (scoresItem == null || scoresItem.isEmpty()) {
                    validation.addRootError("must not be empty");
                    return;
                }
                validation.withEach(scoresItem, scoresItemItem -> {
                    if (scoresItemItem == null) {
                        validation.addRootError("must not be null");
                        return;
                    }
                });
            });
        });
    }
}
