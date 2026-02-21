package test.collection;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class ValidatedIterableRecordValidator implements Validator<ValidatedIterableRecord> {
    private final Validator<ValidatedIterableRecord.Person> friendsItemValidator = new ValidatedIterableRecord$PersonValidator();

    @Override
    public void validate(Validation validation, ValidatedIterableRecord root) {
        validation.withField("friends", () -> {
            var friends = root.friends();
            if (friends == null) {
                validation.addError("must not be null");
                return;
            }
            validation.withEach(friends, friendsItem -> {
                if (friendsItem == null) {
                    validation.addError("must not be null");
                    return;
                }
                friendsItemValidator.validate(validation, friendsItem);
            });
        });
    }
}
