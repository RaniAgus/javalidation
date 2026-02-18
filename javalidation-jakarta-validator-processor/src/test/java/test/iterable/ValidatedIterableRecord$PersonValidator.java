package test.iterable;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class ValidatedIterableRecord$PersonValidator implements Validator<ValidatedIterableRecord.Person> {
    @Override
    public void validate(Validation rootValidation, ValidatedIterableRecord.Person root) {
        rootValidation.validateField("name", nameValidation -> {
            var name = root.name();
            if (name == null) {
                nameValidation.addRootError("must not be null");
            }
        });
    }
}
