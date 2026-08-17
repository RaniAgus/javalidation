package io.github.raniagus.javalidation.spring;

import java.util.Map;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * Native-image hints for resources and Jackson's internal structured-result DTOs.
 */
final class JavalidationRuntimeHints implements RuntimeHintsRegistrar {
    private static final String MESSAGE_BUNDLE = "io.github.raniagus.javalidation.messages";

    private static final Map<String, MemberCategory[]> JACKSON_REFLECTION_HINTS = Map.of(
            "io.github.raniagus.javalidation.ValidationErrors", new MemberCategory[] {
                    MemberCategory.INVOKE_PUBLIC_METHODS,
            },
            "io.github.raniagus.javalidation.jackson.ValidationErrorsMixIn", new MemberCategory[] {
                    MemberCategory.INVOKE_DECLARED_METHODS,
            },
            "io.github.raniagus.javalidation.jackson.StructuredErrorDto", new MemberCategory[] {
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.ACCESS_DECLARED_FIELDS,
            },
            "io.github.raniagus.javalidation.jackson.StructuredFieldErrorDto", new MemberCategory[] {
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.ACCESS_DECLARED_FIELDS,
            },
            "io.github.raniagus.javalidation.jackson.StructuredValidationErrorsDto", new MemberCategory[] {
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.ACCESS_DECLARED_FIELDS,
            }
    );

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.resources().registerResourceBundle(MESSAGE_BUNDLE);

        JACKSON_REFLECTION_HINTS.forEach((type, categories) ->
            hints.reflection().registerTypeIfPresent(
                    classLoader,
                    type,
                    categories
            )
        );
    }
}
