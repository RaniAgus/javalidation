package test.collection;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.Validator;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class ValidatedMapRecord$PersonValidator implements Validator<ValidatedMapRecord.Person> {
    @Override
    public void validate(Validation validation, ValidatedMapRecord.Person root) {
        validation.withField("name", () -> {
            var name = root.name();
            if (name == null) {
                validation.addError("must not be null");
                return;
            }
        });
    }
}
