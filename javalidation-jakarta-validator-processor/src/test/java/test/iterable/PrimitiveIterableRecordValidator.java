package test.iterable;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class PrimitiveIterableRecordValidator implements Validator<PrimitiveIterableRecord> {

    @Override
    public void validate(Validation validation, PrimitiveIterableRecord root) {
        validation.validateField("tags", () -> {
            var tags = root.tags();
            if (tags == null) {
                validation.addRootError("must not be null");
            }
            if (tags != null) {
                int tagsIndex = 0;
                for (var tagsItem : tags) {
                    validation.validateField(tagsIndex++, () -> {
                        if (tagsItem != null) {
                            if (tagsItem.length() < 3 || tagsItem.length() > 10) {
                                validation.addRootError("size must be between {0} and {1}", 3, 10);
                            }
                        }
                    });
                }

            }
        });
    }
}