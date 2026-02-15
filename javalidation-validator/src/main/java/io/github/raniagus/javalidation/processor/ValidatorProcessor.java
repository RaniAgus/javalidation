package io.github.raniagus.javalidation.processor;

import static io.github.raniagus.javalidation.processor.ProcessorUtils.INDENT;
import static io.github.raniagus.javalidation.processor.ProcessorUtils.getReferredType;

import io.github.raniagus.javalidation.annotation.Validator;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.RecordComponent;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("io.github.raniagus.javalidation.annotation.Validator")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class ValidatorProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Validator.class)) {
            if (element instanceof TypeElement recordElement) {
                if (element.getKind() != ElementKind.RECORD) {
                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            "@Validator can only be applied to records"
                    );
                    continue;
                }
                generateValidator(recordElement);
            }
        }
        return true;
    }

    private void generateValidator(TypeElement recordElement) {
        String packageName = processingEnv.getElementUtils()
                .getPackageOf(recordElement).getQualifiedName().toString();

        StringBuilder sb = new StringBuilder();
        sb.append(
                """
                package %1$s;
                
                import io.github.raniagus.javalidation.*;
                import org.jspecify.annotations.Nullable;
                
                public class %2$s implements Validator<%3$s> {
                """.formatted(packageName, getValidatorName(recordElement), getRecordName(recordElement)));

        generateValidatorBody(recordElement, sb);

        sb.append(
                """
                }
                """
        );

        try {
            JavaFileObject file = processingEnv.getFiler()
                    .createSourceFile(packageName + "." + getValidatorName(recordElement));

            try (Writer writer = file.openWriter()) {
                writer.write(sb.toString());
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Failed to generate validator: " + e.getMessage()
            );
        }
    }

    private void generateValidatorBody(TypeElement recordElement, StringBuilder sb) {
        generateValidatorInjection(recordElement, sb);
        generateValidateMethod(recordElement, sb);
    }

    private static void generateValidatorInjection(TypeElement recordElement, StringBuilder sb) {
        for (Element enclosed : recordElement.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.RECORD_COMPONENT) continue;
            if (enclosed instanceof RecordComponentElement component) {
                TypeElement referredType = getReferredType(component);
                if (referredType != null && referredType.getAnnotation(Validator.class) != null) {
                    String componentName = component.getSimpleName().toString();
                    sb.append(INDENT);
                    sb.append(
                            """
                            private final Validator<%1$s> %2$sValidator = new %3$sValidator();
                            """.formatted(referredType.getQualifiedName(), componentName, getValidatorFullName(referredType))
                    );
                }
            }
        }
    }

    private static void generateValidateMethod(TypeElement recordElement, StringBuilder sb) {
        sb.append("\n");
        sb.append(INDENT);
        sb.append(
                """
                @Override
                """);
        sb.append(INDENT);

        Element enclosingElement = recordElement.getEnclosingElement();
        if (isEnclosingClass(enclosingElement)) {
            sb.append(
                    """
                    public ValidationErrors validate(%2$s.@Nullable %1$s obj) {
                    """.formatted(recordElement.getSimpleName(), enclosingElement.getSimpleName()));
        } else {
            sb.append(
                    """
                    public ValidationErrors validate(@Nullable %1$s obj) {
                    """.formatted(recordElement.getSimpleName()));
        }

        sb.append(INDENT);
        sb.append(
                """
                    Validation validation = Validation.create();
                """);


        for (Element enclosed : recordElement.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.RECORD_COMPONENT) continue;
            if (enclosed instanceof RecordComponentElement component) {
                generateFieldValidations(component, 2, sb);
            }
        }

        sb.append(INDENT);
        sb.append(
                """
                    return validation.finish();
                """
        );
        sb.append(INDENT);
        sb.append(
                """
                }
                """
        );
    }

    private static void generateFieldValidations(RecordComponentElement component, int indentLevel, StringBuilder sb) {
        ValidationWriter.getAllValidations(component)
                .flatMap(String::lines)
                .forEach(line -> {
                    sb.append(INDENT.repeat(indentLevel));
                    sb.append(line);
                    sb.append('\n');
                });

        // TODO: Implement Iterable<T> support
        // - Check if component corresponds to an Iterable<T> type
        // - Get annotations and generate root validations for each element
        // - Check if element type is a record
        // - Call generateFieldValidations recursively by adding a prefix

        // TODO: Implement Map<K, V> support
        // - Check if component corresponds to a Map<K, V> type
        // - Get annotations and generate root validations for each entry
        // - Check if value types are records
        // - Call generateFieldValidations recursively by adding a prefix
    }

    private static String getValidatorName(TypeElement recordElement) {
        Element enclosingElement = recordElement.getEnclosingElement();
        if (isEnclosingClass(enclosingElement)) {
            return enclosingElement.getSimpleName() + "$" + recordElement.getSimpleName() + "Validator";
        }

        return recordElement.getSimpleName() + "Validator";
    }

    private static String getValidatorFullName(TypeElement recordElement) {
        Element enclosingElement = recordElement.getEnclosingElement();
        if (isEnclosingClass(enclosingElement)) {
            return enclosingElement + "$" + recordElement.getSimpleName();
        }

        return recordElement.toString();
    }

    private static String getRecordName(TypeElement recordElement) {
        Element enclosingElement = recordElement.getEnclosingElement();
        if (isEnclosingClass(enclosingElement)) {
            return enclosingElement.getSimpleName() + "." + recordElement.getSimpleName();
        }

        return recordElement.getSimpleName().toString();
    }

    private static boolean isEnclosingClass(Element enclosingElement) {
        return enclosingElement.getKind() == ElementKind.CLASS
                || enclosingElement.getKind() == ElementKind.RECORD
                || enclosingElement.getKind() == ElementKind.INTERFACE
                || enclosingElement.getKind() == ElementKind.ENUM;
    }
}
