package test.iterable;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class PrimitiveIterableRecordValidator implements Validator<PrimitiveIterableRecord> {

    @Override
    public void validate(Validation rootValidation, PrimitiveIterableRecord root) {
        rootValidation.validateField("tags", tagsValidation -> {
            var tags = root.tags();
            if (tags == null) {
                tagsValidation.addRootError("must not be null");
            }
            if (tags != null) {
                int tagsIndex = 0;
                for (var tagsItem : tags) {
                    tagsValidation.validateField(tagsIndex++, tagsItemValidation -> {
                        if (tagsItem != null) {
                            if (tagsItem.length() < 3 || tagsItem.length() > 10) {
                                tagsItemValidation.addRootError("size must be between {0} and {1}", 3, 10);
                            }
                        }
                    });
                }

            }
        });
    }
}