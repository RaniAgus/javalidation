package io.github.raniagus.javalidation.processor;

import static io.github.raniagus.javalidation.processor.JakartaValidationAdapter.resolveMessage;
import static io.github.raniagus.javalidation.processor.ProcessorUtils.getReferredType;

import io.github.raniagus.javalidation.annotation.Validator;
import jakarta.validation.constraints.*;
import java.util.stream.Stream;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import org.jspecify.annotations.Nullable;

/**
 * Enum that handles generating validation code for different Jakarta validation annotations.
 * Each enum constant corresponds to a specific validation annotation type and knows how to
 * generate the appropriate validation code for it.
 */
public enum ValidationWriter {

    NOT_NULL {
        @Override
        public @Nullable String getValidation(RecordComponentElement element) {
            NotNull annotation = element.getAccessor().getAnnotation(NotNull.class);
            if (annotation == null) {
                return null;
            }

            String fieldName = element.getSimpleName().toString();
            String message = resolveMessage(annotation.message());

            return
                """
                if (obj.%s() == null) {
                    validation.addFieldError("%s", "%s");
                }
                """.formatted(fieldName, fieldName, message);
        }
    },

    SIZE {
        @Override
        public @Nullable String getValidation(RecordComponentElement element) {
            Size annotation = element.getAccessor().getAnnotation(Size.class);
            if (annotation == null) {
                return null;
            }

            String fieldName = element.getSimpleName().toString();
            String message = resolveMessage(annotation.message(), "{min}", "{max}");
            int min = annotation.min();
            int max = annotation.max();

            return 
                """
                if (obj.%s() != null) {
                    int length = obj.%s().length();
                    if (length < %d || length > %d) {
                        validation.addFieldError("%s", "%s", %d, %d);
                    }
                }
                """.formatted(fieldName, fieldName, min, max, fieldName, message, min, max);
        }
    },

    MIN {
        @Override
        public @Nullable String getValidation(RecordComponentElement element) {
            Min annotation = element.getAccessor().getAnnotation(Min.class);
            if (annotation == null) {
                return null;
            }

            String fieldName = element.getSimpleName().toString();
            String message = resolveMessage(annotation.message(), "{value}");
            long value = annotation.value();

            return 
                """
                if (obj.%s() != null && obj.%s() < %d) {
                    validation.addFieldError("%s", "%s", %d);
                }
                """.formatted(fieldName, fieldName, value, fieldName, message, value);
        }
    },

    MAX {
        @Override
        public @Nullable String getValidation(RecordComponentElement element) {
            Max annotation = element.getAccessor().getAnnotation(Max.class);
            if (annotation == null) {
                return null;
            }

            String fieldName = element.getSimpleName().toString();
            String message = resolveMessage(annotation.message(), "{value}");
            long value = annotation.value();

            return 
                """
                if (obj.%s() != null && obj.%s() > %d) {
                    validation.addFieldError("%s", "%s", %d);
                }
                """.formatted(fieldName, fieldName, value, fieldName, message, value);
        }
    },

    EMAIL {
        @Override
        public @Nullable String getValidation(RecordComponentElement element) {
            Email annotation = element.getAccessor().getAnnotation(Email.class);
            if (annotation == null) {
                return null;
            }

            String fieldName = element.getSimpleName().toString();
            String message = resolveMessage(annotation.message());
            // Basic email validation regex
            String emailRegex = "^[^@]+@[^@]+\\\\.[^@]+$";

            return 
                """
                if (obj.%s() != null && !obj.%s().matches("%s")) {
                    validation.addFieldError("%s", "%s");
                }
                """.formatted(fieldName, fieldName, emailRegex, fieldName, message);
        }
    },

    NOT_EMPTY {
        @Override
        public @Nullable String getValidation(RecordComponentElement element) {
            NotEmpty annotation = element.getAccessor().getAnnotation(NotEmpty.class);
            if (annotation == null) {
                return null;
            }

            String fieldName = element.getSimpleName().toString();
            String message = resolveMessage(annotation.message());

            return
                """
                if (obj.%s() == null || obj.%s().isEmpty()) {
                    validation.addFieldError("%s", "%s");
                }
                """.formatted(fieldName, fieldName, fieldName, message);
        }
    },

    NOT_BLANK {
        @Override
        public @Nullable String getValidation(RecordComponentElement element) {
            NotBlank annotation = element.getAccessor().getAnnotation(NotBlank.class);
            if (annotation == null) {
                return null;
            }

            String fieldName = element.getSimpleName().toString();
            String message = resolveMessage(annotation.message());

            return
                """
                if (obj.%s() == null || obj.%s().isBlank()) {
                    validation.addFieldError("%s", "%s");
                }
                """.formatted(fieldName, fieldName, fieldName, message);
        }
    },

    PATTERN {
        @Override
        public @Nullable String getValidation(RecordComponentElement element) {
            Pattern annotation = element.getAccessor().getAnnotation(Pattern.class);
            if (annotation == null) {
                return null;
            }

            String fieldName = element.getSimpleName().toString();
            String message = resolveMessage(annotation.message(), "{regexp}");
            String regex = annotation.regexp().replace("\\", "\\\\");

            return 
                """
                if (obj.%s() != null && !obj.%s().matches("%s")) {
                    validation.addFieldError("%s", "%s", "%s");
                }
                """.formatted(fieldName, fieldName, regex, fieldName, message, regex);
        }
    },

    POSITIVE {
        @Override
        public @Nullable String getValidation(RecordComponentElement element) {
            Positive annotation = element.getAccessor().getAnnotation(Positive.class);
            if (annotation == null) {
                return null;
            }

            String fieldName = element.getSimpleName().toString();
            String message = resolveMessage(annotation.message());

            return
                """
                if (obj.%s() != null && obj.%s() <= 0) {
                    validation.addFieldError("%s", "%s");
                }
                """.formatted(fieldName, fieldName, fieldName, message);
        }
    },

    POSITIVE_OR_ZERO {
        @Override
        public @Nullable String getValidation(RecordComponentElement element) {
            PositiveOrZero annotation = element.getAccessor().getAnnotation(PositiveOrZero.class);
            if (annotation == null) {
                return null;
            }

            String fieldName = element.getSimpleName().toString();
            String message = resolveMessage(annotation.message());

            return
                """
                if (obj.%s() != null && obj.%s() < 0) {
                    validation.addFieldError("%s", "%s");
                }
                """.formatted(fieldName, fieldName, fieldName, message);
        }
    },

    NEGATIVE {
        @Override
        public @Nullable String getValidation(RecordComponentElement element) {
            Negative annotation = element.getAccessor().getAnnotation(Negative.class);
            if (annotation == null) {
                return null;
            }

            String fieldName = element.getSimpleName().toString();
            String message = resolveMessage(annotation.message());

            return
                """
                if (obj.%s() != null && obj.%s() >= 0) {
                    validation.addFieldError("%s", "%s");
                }
                """.formatted(fieldName, fieldName, fieldName, message);
        }
    },

    NEGATIVE_OR_ZERO {
        @Override
        public @Nullable String getValidation(RecordComponentElement element) {
            NegativeOrZero annotation = element.getAccessor().getAnnotation(NegativeOrZero.class);
            if (annotation == null) {
                return null;
            }

            String fieldName = element.getSimpleName().toString();
            String message = resolveMessage(annotation.message());

            return
                """
                if (obj.%s() != null && obj.%s() > 0) {
                    validation.addFieldError("%s", "%s");
                }
                """.formatted(fieldName, fieldName, fieldName, message);
        }
    },

    VALIDATOR {
        @Override
        public @Nullable String getValidation(RecordComponentElement component) {
            TypeElement referredType = getReferredType(component);
            if (referredType == null || referredType.getAnnotation(Validator.class) == null) {
                return null;
            }

            String fieldName = component.getSimpleName().toString();
            return
                """
                if (obj.%1$s() != null) {
                    validation.addAll(%1$sValidator.validate(obj.%1$s()), new StringBuilder("%1$s"));
                }
                """.formatted(fieldName);
        }
    };


    /**
     * Generates validation code for the given element if it has the corresponding annotation.
     *
     * @param element the element representing a record component
     * @return the validation code as a string
     */
    public abstract @Nullable String getValidation(RecordComponentElement element);

    /**
     * Generates all applicable validation code for a given element by checking all validation writers.
     *
     * @param element the element representing a record component
     */
    public static Stream<String> getAllValidations(RecordComponentElement element) {
        Stream.Builder<String> builder = Stream.builder();
        for (ValidationWriter writer : values()) {
            String validation = writer.getValidation(element);
            if (validation == null) {
                continue;
            }
            builder.add(validation);
        }
        return builder.build();
    }
}
