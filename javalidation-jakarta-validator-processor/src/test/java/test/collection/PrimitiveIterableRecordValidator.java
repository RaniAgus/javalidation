package test.collection;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class PrimitiveIterableRecordValidator implements InitializableValidator<PrimitiveIterableRecord> {

    @Override
    public void initialize(ValidatorsHolder holder) {
    }

    @Override
    public void validate(Validation validation, PrimitiveIterableRecord root) {
        validation.withField("tags", () -> {
            var tags = root.tags();
            if (tags == null) {
                validation.addError("io.github.raniagus.javalidation.constraints.NotNull.message");
                return;
            }
            validation.withEach(tags, tagsItem -> {
                if (tagsItem == null) return;
                if (tagsItem.length() < 3 || tagsItem.length() > 10) {
                    validation.addError("io.github.raniagus.javalidation.constraints.Size.message", 3, 10);
                }
            });
        });
    }
}