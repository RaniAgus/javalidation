package test.collection;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.Validator;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class ValidatedIterableRecordValidator implements InitializableValidator<ValidatedIterableRecord> {
    private Validator<ValidatedIterableRecord.Person> friendsItemValidator;

    @Override
    public void initialize(ValidatorsHolder holder) {
        friendsItemValidator = holder.getValidator(ValidatedIterableRecord.Person.class);
    }

    @Override
    public void validate(Validation validation, ValidatedIterableRecord root) {
        validation.withField("friends", () -> {
            var friends = root.friends();
            if (friends == null) {
                validation.addError("io.github.raniagus.javalidation.constraints.NotNull.message");
                return;
            }
            validation.withEach(friends, friendsItem -> {
                if (friendsItem == null) {
                    validation.addError("io.github.raniagus.javalidation.constraints.NotNull.message");
                    return;
                }
                friendsItemValidator.validate(validation, friendsItem);
            });
        });
    }
}
