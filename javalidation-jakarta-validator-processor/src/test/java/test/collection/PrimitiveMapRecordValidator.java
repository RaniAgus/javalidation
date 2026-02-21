package test.collection;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class PrimitiveMapRecordValidator implements Validator<PrimitiveMapRecord> {
    @Override
    public void validate(Validation validation, PrimitiveMapRecord root) {
        validation.withField("tags", () -> {
            var tags = root.tags();
            if (tags == null || tags.isEmpty()) {
                validation.addError("must not be empty");
                return;
            }
            tags.forEach((tagsKey, tagsValue) -> {
                if (tagsKey == null) {
                    validation.addError("must not be null");
                    return;
                }
                validation.withField(tagsKey, () -> {
                    if (tagsValue == null) {
                        validation.addError("must not be null");
                        return;
                    }
                });
            });
        });
    }
}
