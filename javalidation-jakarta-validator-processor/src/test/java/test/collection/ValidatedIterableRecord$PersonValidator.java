package test.collection;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class ValidatedIterableRecord$PersonValidator implements Validator<ValidatedIterableRecord.Person> {
    @Override
    public void validate(Validation validation, ValidatedIterableRecord.Person root) {
        validation.withField("name", () -> {
            var name = root.name();
            if (name == null) {
                validation.addRootError("must not be null");
                return;
            }
        });
    }
}
