package io.github.raniagus.javalidation.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import org.jspecify.annotations.Nullable;

public final class ProcessorUtils {
    public static final int INDENT_SIZE = 4;
    public static final String INDENT = " ".repeat(INDENT_SIZE);

    private ProcessorUtils() {}

    public static @Nullable TypeElement getReferredType(RecordComponentElement component) {
        if (component.asType() instanceof DeclaredType declaredType) {
            Element element = declaredType.asElement();
            if (element instanceof TypeElement typeElement) {
                return typeElement;
            }
        }
        return null;
    }
}
