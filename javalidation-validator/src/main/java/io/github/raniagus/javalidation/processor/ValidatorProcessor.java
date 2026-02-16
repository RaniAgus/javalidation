package io.github.raniagus.javalidation.processor;

import static io.github.raniagus.javalidation.processor.JakartaAnnotationParser.*;

import io.github.raniagus.javalidation.annotation.Validate;
import io.github.raniagus.javalidation.format.BracketNotationFormatter;
import io.github.raniagus.javalidation.format.PropertyPathNotationFormatter;
import io.github.raniagus.javalidation.format.DotNotationFormatter;
import io.github.raniagus.javalidation.format.FieldKeyFormatter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
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

@SupportedAnnotationTypes("io.github.raniagus.javalidation.annotation.Validate")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class ValidatorProcessor extends AbstractProcessor {
    private static final String OPTIONS_PREFIX = "io.github.raniagus.javalidation.";

    private final List<ValidatorClassWriter> WRITERS = Collections.synchronizedList(new ArrayList<>());
    private boolean generated = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        String keyNotation = processingEnv.getOptions().getOrDefault(OPTIONS_PREFIX + "key-notation", "property_path");
        FieldKeyFormatter fieldKeyFormatter = switch (keyNotation) {
            case "property_path" -> new PropertyPathNotationFormatter();
            case "brackets" -> new BracketNotationFormatter();
            case "dots" -> new DotNotationFormatter();
            default -> throw new IllegalArgumentException("Invalid key notation: " + keyNotation);
        };

        List<ValidatorClassWriter> classWriters = parseClassWriters(roundEnv);

        for (ValidatorClassWriter classWriter : classWriters) {
            writeClass(classWriter, fieldKeyFormatter);
            WRITERS.add(classWriter);
        }

        if (roundEnv.processingOver() && !generated) {
            writeClass(new ValidatorsClassWriter(WRITERS), fieldKeyFormatter);
            generated = true;
        }

        return true;
    }

    // -- Writing --

    private void writeClass(ClassWriter classWriter, FieldKeyFormatter formatter) {
        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(classWriter.fullName());
            try (Writer writer = file.openWriter()) {
                classWriter.write(new ValidationOutput(writer, formatter));
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
        return roundEnv.getElementsAnnotatedWith(Validate.class).stream()
                .flatMap(element -> {
                    if (!(element instanceof TypeElement recordElement)) {
                        return Stream.empty();
                    }

                    if (recordElement.getKind() != ElementKind.RECORD) {
                        processingEnv.getMessager().printMessage(
                                Diagnostic.Kind.ERROR, "@Validate can only be applied to records, but it was applied to " + recordElement);
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
                getRecordImportName(recordElement),
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
        if (referredType == null || referredType.getAnnotation(Validate.class) == null) {
            return null;
        }

        return new ValidationWriter.Validator(
                getRecordImportName(referredType),
                getEnclosingClassPrefix(referredType, "."),
                getRecordName(referredType),
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

    private static String getRecordImportName(TypeElement recordElement) {
        Element enclosingElement = recordElement.getEnclosingElement();
        if (isEnclosingClass(enclosingElement)) {
            return enclosingElement.toString();
        }
        return recordElement.getQualifiedName().toString();
    }

    private static boolean isEnclosingClass(Element enclosingElement) {
        return enclosingElement.getKind() == ElementKind.CLASS
               || enclosingElement.getKind() == ElementKind.RECORD
               || enclosingElement.getKind() == ElementKind.INTERFACE
               || enclosingElement.getKind() == ElementKind.ENUM;
    }

}
