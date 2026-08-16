package test.jakarta;

import io.github.raniagus.javalidation.Validation;
import io.github.raniagus.javalidation.validator.InitializableValidator;
import io.github.raniagus.javalidation.validator.ValidatorsHolder;
import java.util.regex.Pattern;
import javax.annotation.processing.Generated;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
public class ComposedAnnotationRecordValidator implements InitializableValidator<ComposedAnnotationRecord> {
    private static final Pattern VALUE_PATTERN = Pattern.compile("^[a-zA-Z]+$");

    @Override
    public void initialize(ValidatorsHolder holder) {
    }

    @Override
    public void validate(Validation validation, ComposedAnnotationRecord root) {
        validation.withField("value", () -> {
            var value = root.value();
            if (value == null || value.isBlank()) {
                validation.addError("io.github.raniagus.javalidation.constraints.NotBlank.message");
                return;
            }
            if (!VALUE_PATTERN.matcher(value.toString()).matches()) {
                validation.addError("io.github.raniagus.javalidation.constraints.Pattern.message", "^[a-zA-Z]+$");
            }
        });
    }
}
