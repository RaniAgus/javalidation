package test.collection;

import io.github.raniagus.javalidation.Constraint;
import io.github.raniagus.javalidation.Constraints;
import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class PrimitiveIterableRecordValidator implements InitializableValidator<PrimitiveIterableRecord> {
    private static final Constraint<String> TAGSITEM_SIZE = Constraints.length(3, 10);

    
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
                TAGSITEM_SIZE.validate(validation, tagsItem);
            });
        });
    }
}
