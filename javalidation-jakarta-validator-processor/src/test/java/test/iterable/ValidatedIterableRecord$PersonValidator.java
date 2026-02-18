package test.iterable;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class ValidatedIterableRecord$PersonValidator implements Validator<ValidatedIterableRecord.Person> {
    @Override
    public ValidationErrors validate(ValidatedIterableRecord.Person root) {
        Validation rootValidation = Validation.create();

        var name = root.name();
        var nameValidation = Validation.create();
        if (name == null) {
            nameValidation.addRootError("must not be null");
        }
        rootValidation.addAll(nameValidation.finish(), new Object[]{"name"});

        return rootValidation.finish();
    }
}
