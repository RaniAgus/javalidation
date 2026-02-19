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
        validation.withField("tags", () -> {
            var tags = root.tags();
            if (tags == null) {
                validation.addRootError("must not be null");
                return;
            }
            validation.withEach(tags, tagsItem -> {
                if (tagsItem == null) return;
                if (tagsItem.length() < 3 || tagsItem.length() > 10) {
                    validation.addRootError("size must be between {0} and {1}", 3, 10);
                }
            });
        });
    }
}