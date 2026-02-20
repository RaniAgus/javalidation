package test.collection;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class ValidatedMapRecordValidator implements Validator<ValidatedMapRecord> {
    private final Validator<ValidatedMapRecord.Person> friendsValueValidator = new ValidatedMapRecord$PersonValidator();

    @Override
    public void validate(Validation validation, ValidatedMapRecord root) {
        validation.withField("friends", () -> {
            var friends = root.friends();
            if (friends == null || friends.isEmpty()) {
                validation.addRootError("must not be empty");
                return;
            }
            friends.forEach((friendsKey, friendsValue) -> {
                if (friendsKey == null) {
                    validation.addRootError("must not be null");
                    return;
                }

                validation.withField(friendsKey, () -> {
                    if (friendsValue == null) {
                        validation.addRootError("must not be null");
                        return;
                    }
                    friendsValueValidator.validate(validation, friendsValue);
                });
            });
        });
    }
}
