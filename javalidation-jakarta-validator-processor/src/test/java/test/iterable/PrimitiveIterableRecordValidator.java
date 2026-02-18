package test.iterable;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class PrimitiveIterableRecordValidator implements Validator<PrimitiveIterableRecord> {

    @Override
    public ValidationErrors validate(PrimitiveIterableRecord root) {
        Validation rootValidation = Validation.create();

        var tags = root.tags();
        var tagsValidation = Validation.create();
        if (tags == null) {
            tagsValidation.addRootError("must not be null");
        }
        if (tags != null) {
            int tagsIndex = 0;
            for (var tagsItem : tags) {
                var tagsItemValidation = Validation.create();
                if (tagsItem != null) {
                    if (tagsItem.length() < 3 || tagsItem.length() > 10) {
                        tagsItemValidation.addRootError("size must be between {0} and {1}", 3, 10);
                    }
                }
                tagsValidation.addAll(tagsItemValidation.finish(), new Object[]{tagsIndex++});
            }

        }
        rootValidation.addAll(tagsValidation.finish(), new Object[]{"tags"});

        return rootValidation.finish();
    }
}