package io.github.raniagus.javalidation.processor;

import static io.github.raniagus.javalidation.processor.ProcessorUtils.INDENT;
import static io.github.raniagus.javalidation.processor.ProcessorUtils.getReferredType;

import io.github.raniagus.javalidation.annotation.Validator;
import java.io.IOException;
import java.io.Writer;
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
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
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
        String validatorName = recordElement.getSimpleName() + "Validator";

        StringBuilder sb = new StringBuilder();
        sb.append(
                """
                package %1$s;
                
                import io.github.raniagus.javalidation.*;
                import org.jspecify.annotations.Nullable;
                
                public class %2$s implements Validator<@Nullable %3$s> {
                """.formatted(packageName, validatorName, recordElement.getSimpleName()));

        sb.append(generateValidatorBody(recordElement, 0));

        try {
            JavaFileObject file = processingEnv.getFiler()
                    .createSourceFile(packageName + "." + validatorName);

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

    private StringBuilder generateValidatorBody(TypeElement recordElement, int indentLevel) {
        StringBuilder sb = new StringBuilder();
        for (Element enclosed : recordElement.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.RECORD_COMPONENT) continue;
            if (enclosed instanceof RecordComponentElement component) {
                TypeElement referredType = getReferredType(component);
                if (referredType != null && referredType.getAnnotation(Validator.class) != null) {
                    String componentFullName = referredType.getQualifiedName().toString();
                    String componentName = component.getSimpleName().toString();
                    sb.append(
                            """
                                private final Validator<%1$s> %2$sValidator = new %1$sValidator();
                            """.formatted(componentFullName, componentName)
                    );
                }
            }
        }

        sb.append(
                """
                
                    @Override
                    public ValidationErrors validate(@Nullable %1$s obj) {
                        Validation validation = Validation.create();
                """.formatted(recordElement.getSimpleName()));

        String indent = INDENT.repeat(indentLevel + 2);

        for (Element enclosed : recordElement.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.RECORD_COMPONENT) continue;
            if (enclosed instanceof RecordComponentElement component) {
                ValidationWriter.getAllValidations(component)
                        .flatMap(String::lines)
                        .forEach(line -> {
                            sb.append(indent);
                            sb.append(line);
                            sb.append('\n');
                        });
            }
        }

        sb.append(
                """
                        return validation.finish();
                    }
                """
        );

        // TODO: Create validator class for each nested record

        sb.append(
                """
                }
                """
        );

        return sb;
    }
}
