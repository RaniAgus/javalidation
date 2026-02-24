package test.collection;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class PrimitiveMapRecordValidator implements InitializableValidator<PrimitiveMapRecord> {

    @Override
    public void initialize(ValidatorsHolder holder) {
    }

    @Override
    public void validate(Validation validation, PrimitiveMapRecord root) {
        validation.withField("tags", () -> {
            var tags = root.tags();
            if (tags == null || tags.isEmpty()) {
                validation.addError("io.github.raniagus.javalidation.constraints.NotEmpty.message");
                return;
            }
            tags.forEach((tagsKey, tagsValue) -> {
                if (tagsKey == null) {
                    validation.addError("io.github.raniagus.javalidation.constraints.NotNull.message");
                    return;
                }
                validation.withField(tagsKey, () -> {
                    if (tagsValue == null) {
                        validation.addError("io.github.raniagus.javalidation.constraints.NotNull.message");
                        return;
                    }
                });
            });
        });
    }
}
