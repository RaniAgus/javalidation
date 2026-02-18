package test.iterable;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
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
    public ValidationErrors validate(ValidatedIterableRecord root) {
        Validation rootValidation = Validation.create();

        var friends = root.friends();
        var friendsValidation = Validation.create();
        if (friends == null) {
            friendsValidation.addRootError("must not be null");
        }
        if (friends != null) {
            int friendsIndex = 0;
            for (var friendsItem : friends) {
                var friendsItemValidation = Validation.create();
                if (friendsItem == null) {
                    friendsItemValidation.addRootError("must not be null");
                }
                if (friendsItem != null) {
                    friendsItemValidation.addAll(friendsItemValidator.validate(friendsItem));
                }
                friendsValidation.addAll(friendsItemValidation.finish(), new Object[]{friendsIndex++});
            }

        }
        rootValidation.addAll(friendsValidation.finish(), new Object[]{"friends"});

        return rootValidation.finish();
    }
}
