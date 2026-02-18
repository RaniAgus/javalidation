package test.iterable;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;
import test.iterable.ValidatedIterableRecord;
import test.iterable.ValidatedIterableRecord$PersonValidator;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class ValidatedIterableRecordValidator implements Validator<ValidatedIterableRecord> {
    private final Validator<ValidatedIterableRecord.Person> friendsItemValidator = new ValidatedIterableRecord$PersonValidator();

    @Override
    public void validate(Validation rootValidation, ValidatedIterableRecord root) {
        rootValidation.validateField("friends", friendsValidation -> {
            var friends = root.friends();
            if (friends == null) {
                friendsValidation.addRootError("must not be null");
            }
            if (friends != null) {
                int friendsIndex = 0;
                for (var friendsItem : friends) {
                    friendsValidation.validateField(friendsIndex++, friendsItemValidation -> {
                        if (friendsItem == null) {
                            friendsItemValidation.addRootError("must not be null");
                        }
                        if (friendsItem != null) {
                            friendsItemValidator.validate(friendsItemValidation, friendsItem);
                        }
                    });
                }
            }
        });
    }
}
