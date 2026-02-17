package io.github.raniagus.javalidation.processor;

import io.github.raniagus.javalidation.annotation.Validate;
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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.jspecify.annotations.Nullable;

@SupportedAnnotationTypes("io.github.raniagus.javalidation.annotation.Validate")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class ValidatorProcessor extends AbstractProcessor {
    private final List<ValidatorClassWriter> WRITERS = Collections.synchronizedList(new ArrayList<>());
    private boolean generated = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<ValidatorClassWriter> classWriters = parseClassWriters(roundEnv);

        for (ValidatorClassWriter classWriter : classWriters) {
            writeClass(classWriter);
            WRITERS.add(classWriter);
        }

        if (roundEnv.processingOver() && !generated) {
            writeClass(new ValidatorsClassWriter(WRITERS));
            generated = true;
        }

        return true;
    }

    // -- Writing --

    private void writeClass(ClassWriter classWriter) {
        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(classWriter.fullName());
            try (Writer writer = file.openWriter()) {
                classWriter.write(new ValidationOutput(writer));
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
                parseNullSafeWriters(component),
                parseNullUnsafeWriters(component)
        );
    }

    private ValidationWriter.@Nullable NullSafeWriter parseNullSafeWriters(RecordComponentElement component) {
        return JakartaAnnotationParser.parseNullSafeWriters(component.getAccessor());
    }

    private List<ValidationWriter.NullUnsafeWriter> parseNullUnsafeWriters(RecordComponentElement component) {
        return Stream.<ValidationWriter.NullUnsafeWriter>concat(
                JakartaAnnotationParser.parseNullUnsafeWriters(component.getAccessor()),
                Stream.of(
                        parseValidateAnnotation(getReferredType(component)),
                        parseIterable(component.getAccessor().getReturnType())
                ).filter(Objects::nonNull)
        ).toList();
    }

    private ValidationWriter.@Nullable NullUnsafeWriter parseValidateAnnotation(@Nullable Element referredType) {
        if (referredType == null || referredType.getAnnotation(Validate.class) == null) {
            return null;
        }

        return new ValidationWriter.Validate(
                getRecordImportName(referredType),
                getEnclosingClassPrefix(referredType, "."),
                getRecordName(referredType),
                getValidatorName(referredType),
                getValidatorFullName(referredType)
        );
    }

    private ValidationWriter.@Nullable NullUnsafeWriter parseIterable(TypeMirror fieldType) {
        if (!isIterable(fieldType)) {
            return null;
        }

        DeclaredType itemType = getIterableItemType(fieldType);
        if (itemType == null) {
            return null;
        }

        return new ValidationWriter.IterableWriter(
                parseNullSafeWriters(itemType),
                parseNullUnsafeWriters(itemType)
        );
    }

    private ValidationWriter.@Nullable NullSafeWriter parseNullSafeWriters(DeclaredType elementType) {
        return JakartaAnnotationParser.parseNullSafeWriters(elementType);
    }

    private List<ValidationWriter.NullUnsafeWriter> parseNullUnsafeWriters(DeclaredType itemType) {
        return Stream.<ValidationWriter.NullUnsafeWriter>concat(
                JakartaAnnotationParser.parseNullUnsafeWriters(itemType),
                Stream.of(
                        parseValidateAnnotation(getReferredType(itemType)),
                        parseIterable(itemType)
                ).filter(Objects::nonNull)
        ).toList();
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

    private static String getValidatorName(Element recordElement) {
        return getEnclosingClassPrefix(recordElement, "$") + getRecordName(recordElement) + "Validator";
    }

    private static String getEnclosingClassPrefix(Element recordElement, String prefix) {
        Element enclosingElement = recordElement.getEnclosingElement();
        if (isEnclosingClass(enclosingElement)) {
            return enclosingElement.getSimpleName() + prefix;
        }
        return "";
    }

    private static String getValidatorFullName(Element recordElement) {
        Element enclosingElement = recordElement.getEnclosingElement();
        if (isEnclosingClass(enclosingElement)) {
            return enclosingElement + "$" + getRecordName(recordElement) + "Validator";
        }

        return recordElement + "Validator";
    }

    private static String getRecordName(Element recordElement) {
        return recordElement.getSimpleName().toString();
    }

    private static String getRecordFullName(TypeElement recordElement) {
        return recordElement.getQualifiedName().toString();
    }

    private static String getRecordImportName(Element recordElement) {
        Element enclosingElement = recordElement.getEnclosingElement();
        if (isEnclosingClass(enclosingElement)) {
            return enclosingElement.toString();
        }
        return recordElement.toString();
    }

    private static boolean isEnclosingClass(Element enclosingElement) {
        return enclosingElement.getKind() == ElementKind.CLASS
               || enclosingElement.getKind() == ElementKind.RECORD
               || enclosingElement.getKind() == ElementKind.INTERFACE
               || enclosingElement.getKind() == ElementKind.ENUM;
    }

    private boolean isIterable(TypeMirror type) {
        Types types = processingEnv.getTypeUtils();
        Elements elements = processingEnv.getElementUtils();

        TypeElement iterableElement = elements.getTypeElement("java.lang.Iterable");
        TypeMirror iterableType = types.erasure(iterableElement.asType());

        return types.isAssignable(types.erasure(type), iterableType);
    }

    private @Nullable DeclaredType getIterableItemType(TypeMirror type) {
        if (!(type instanceof DeclaredType declared)) {
            return null;
        }

        List<? extends TypeMirror> args = declared.getTypeArguments();
        if (args.isEmpty()) {
            return null;
        }

        if (!(args.getFirst() instanceof DeclaredType first)) {
            return null;
        }

        return first;
    }

    private @Nullable TypeElement getReferredType(TypeMirror mirror) {
        if (!(mirror instanceof DeclaredType declared)) {
            return null;
        }

        Element el = declared.asElement();
        if (!(el instanceof TypeElement te)) {
            return null;
        }

        return te;
    }

}
