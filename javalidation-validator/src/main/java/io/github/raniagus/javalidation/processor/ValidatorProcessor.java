package io.github.raniagus.javalidation.processor;

import static io.github.raniagus.javalidation.processor.JakartaAnnotationParser.*;

import io.github.raniagus.javalidation.annotation.Validator;
import io.github.raniagus.javalidation.format.FieldKeyFormatter;
import jakarta.validation.constraints.*;
import java.io.Writer;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.jspecify.annotations.Nullable;

@SupportedAnnotationTypes("io.github.raniagus.javalidation.annotation.Validator")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class ValidatorProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<ValidatorClassWriter> classWriters = parseClassWriters(roundEnv);

        // TODO: Generate a validator locator

        for (ValidatorClassWriter classWriter : classWriters) {
            writeClass(classWriter);
        }

        return true;
    }

    // -- Writing --

    private void writeClass(ClassWriter classWriter) {
        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(classWriter.fullName());
            try (Writer writer = file.openWriter()) {
                classWriter.write(new ValidationOutput(writer, FieldKeyFormatter.getDefault()));
            }
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Failed to generate validator: " + e.getMessage()
            );
        }
    }

    // -- Class writer --

    private List<ValidatorClassWriter> parseClassWriters(RoundEnvironment roundEnv) {
        return roundEnv.getElementsAnnotatedWith(Validator.class).stream()
                .flatMap(element -> {
                    if (!(element instanceof TypeElement recordElement)) {
                        return Stream.empty();
                    }

                    if (recordElement.getKind() != ElementKind.RECORD) {
                        processingEnv.getMessager().printMessage(
                                Diagnostic.Kind.ERROR, "@Validator can only be applied to records");
                        return Stream.empty();
                    }

                    return Stream.of(recordElement);
                })
                .map(this::parseClassWriter)
                .toList();
    }

    private ValidatorClassWriter parseClassWriter(TypeElement recordElement) {
        return new ValidatorClassWriter(
                processingEnv.getElementUtils().getPackageOf(recordElement).getQualifiedName().toString(),
                getValidatorName(recordElement),
                getEnclosingClassPrefix(recordElement, "."),
                getRecordName(recordElement),
                getRecordFullName(recordElement),
                parseFieldWriters(recordElement)
        );
    }

    private List<FieldWriter> parseFieldWriters(TypeElement recordElement) {
        return recordElement.getEnclosedElements().stream()
                .filter(enclosed -> enclosed.getKind() == ElementKind.RECORD_COMPONENT)
                .flatMap(enclosed -> enclosed instanceof RecordComponentElement component ?
                          Stream.of(component)
                        : Stream.empty())
                .map(this::parseFieldWriter)
                .toList();
    }

    // -- Field writers --

    private FieldWriter parseFieldWriter(RecordComponentElement component) {
        return new FieldWriter(
                component.getSimpleName().toString(),
                parseNullSafeWriters(component).filter(Objects::nonNull).findFirst().orElse(null),
                parseNullUnsafeWriters(component).filter(Objects::nonNull).toList()
        );
    }

    private Stream<ValidationWriter.@Nullable NullSafeWriter> parseNullSafeWriters(RecordComponentElement component) {
        return Stream.<ValidationWriter.@Nullable NullSafeWriter>of(
                parseNotBlankAnnotation(component),
                parseNotEmptyAnnotation(component),
                parseNotNullAnnotation(component)
        );
    }

    private Stream<ValidationWriter.@Nullable NullUnsafeWriter> parseNullUnsafeWriters(RecordComponentElement component) {
        return Stream.<ValidationWriter.@Nullable NullUnsafeWriter>of(
                parseSizeAnnotation(component),
                parseMinAnnotation(component),
                parseMaxAnnotation(component),
                parsePositiveAnnotation(component),
                parsePositiveOrZeroAnnotation(component),
                parseNegativeAnnotation(component),
                parseNegativeOrZeroAnnotation(component),
                parseEmailAnnotation(component),
                parsePatternAnnotation(component),
                parseValidatorAnnotation(component)
        );
    }

    private ValidationWriter.@Nullable NullUnsafeWriter parseValidatorAnnotation(RecordComponentElement component) {
        TypeElement referredType = getReferredType(component);
        if (referredType == null || referredType.getAnnotation(Validator.class) == null) {
            return null;
        }

        return new ValidationWriter.Validator(
                getRecordFullName(referredType),
                getValidatorName(referredType),
                getValidatorFullName(referredType)
        );
    }

    private static @Nullable TypeElement getReferredType(RecordComponentElement component) {
        if (component.asType() instanceof DeclaredType declaredType) {
            Element element = declaredType.asElement();
            if (element instanceof TypeElement typeElement) {
                return typeElement;
            }
        }
        return null;
    }

    // -- Utility functions --

    private static String getValidatorName(TypeElement recordElement) {
        return getEnclosingClassPrefix(recordElement, "$") + getRecordName(recordElement) + "Validator";
    }

    private static String getEnclosingClassPrefix(TypeElement recordElement, String prefix) {
        Element enclosingElement = recordElement.getEnclosingElement();
        if (isEnclosingClass(enclosingElement)) {
            return enclosingElement.getSimpleName() + prefix;
        }
        return "";
    }

    private static String getValidatorFullName(TypeElement recordElement) {
        Element enclosingElement = recordElement.getEnclosingElement();
        if (isEnclosingClass(enclosingElement)) {
            return enclosingElement + "$" + getRecordName(recordElement) + "Validator";
        }

        return recordElement + "Validator";
    }

    private static String getRecordName(TypeElement recordElement) {
        return recordElement.getSimpleName().toString();
    }

    private static String getRecordFullName(TypeElement recordElement) {
        return recordElement.getQualifiedName().toString();
    }

    private static boolean isEnclosingClass(Element enclosingElement) {
        return enclosingElement.getKind() == ElementKind.CLASS
               || enclosingElement.getKind() == ElementKind.RECORD
               || enclosingElement.getKind() == ElementKind.INTERFACE
               || enclosingElement.getKind() == ElementKind.ENUM;
    }

}
