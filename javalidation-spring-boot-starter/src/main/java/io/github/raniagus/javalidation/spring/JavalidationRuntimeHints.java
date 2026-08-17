package io.github.raniagus.javalidation.spring;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * Native-image hints for the library message bundle.
 *
 * <p>Jackson-specific reflection metadata is provided by the javalidation-jackson artifact
 * itself in {@code META-INF/native-image}.
 */
final class JavalidationRuntimeHints implements RuntimeHintsRegistrar {
    private static final String MESSAGE_BUNDLE = "io.github.raniagus.javalidation.messages";

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.resources().registerResourceBundle(MESSAGE_BUNDLE);
    }
}
