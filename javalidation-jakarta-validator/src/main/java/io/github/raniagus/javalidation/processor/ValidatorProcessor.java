package io.github.raniagus.javalidation.processor;

import io.github.raniagus.javalidation.annotation.Validate;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.NoSuchFileException;
import java.util.LinkedHashSet;
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
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.jspecify.annotations.Nullable;

@SupportedAnnotationTypes("io.github.raniagus.javalidation.annotation.Validate")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class ValidatorProcessor extends AbstractProcessor {
    private static final String REGISTRY_RESOURCE = "META-INF/io/github/raniagus/javalidation/validator/validators.list";

    private final Set<String> discoveredClassNames = new LinkedHashSet<>();
    private boolean generated = false;
    private boolean loaded = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!generated && !loaded) {
            loadPersistedClassNames();
            loaded = true;
        }

        List<ValidatorClassWriter> classWriters = parseClassWriters(roundEnv);

        for (ValidatorClassWriter classWriter : classWriters) {
            writeClass(classWriter);
            discoveredClassNames.add(classWriter.fullName());
        }

        if (roundEnv.processingOver() && !generated) {
            persistClassNames();
            writeClass(new ValidatorsClassWriter(reconstructWriters()));
            generated = true;
        }

        return true;
    }

    // -- Persistence --

    private void loadPersistedClassNames() {
        try {
            FileObject resource = processingEnv.getFiler().getResource(
                    StandardLocation.CLASS_OUTPUT, "", REGISTRY_RESOURCE);
            try (BufferedReader reader = new BufferedReader(resource.openReader(true))) {
                reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isBlank())
                        .forEach(discoveredClassNames::add);
            }
        } catch (FileNotFoundException | NoSuchFileException e) {
            // First compilation — no registry yet, that's fine
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.WARNING,
                    "Could not read validator registry: " + e.getMessage()
            );
        }
    }

    private void persistClassNames() {
        try {
            FileObject resource = processingEnv.getFiler().createResource(
                    StandardLocation.CLASS_OUTPUT, "", REGISTRY_RESOURCE);
            try (Writer writer = resource.openWriter()) {
                for (String name : discoveredClassNames) {
                    writer.write(name);
                    writer.write("\n");
                }
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.WARNING,
                    "Could not write validator registry: " + e.getMessage()
            );
        }
    }

    // -- Reconstruction --

    /**
     * Turns the persisted fully qualified validator class names back into
     * ValidatorClassWriter instances so ValidatorsClassWriter can use them.
     * ValidatorClassWriter must be reconstructable from its fullName() alone.
     */
    private List<ValidatorClassWriter> reconstructWriters() {
        return discoveredClassNames.stream()
                .map(ValidatorProcessor::fromFullName)
                .toList();
    }


    /**
     * Reconstruct a ValidatorClassWriter from its persisted fullName() for use
     * in ValidatorsClassWriter only — fieldWriters will be empty since they are
     * not needed there (only name-related fields are used).
     * fullName() format: "{packageName}.{className}"
     * className is either "FooValidator" or "EnclosingClass$FooValidator"
     */
    public static ValidatorClassWriter fromFullName(String fullName) {
        int lastDot = fullName.lastIndexOf('.');
        String packageName = fullName.substring(0, lastDot);
        String className = fullName.substring(lastDot + 1); // e.g. "FooValidator" or "Bar$FooValidator"

        int dollarSign = className.indexOf('$');
        boolean isNested = dollarSign != -1;

        String enclosingClassPrefix = isNested
                ? className.substring(0, dollarSign) + "."      // "Bar."
                : "";
        String recordName = (isNested
                ? className.substring(dollarSign + 1) // "Foo" from "Bar$FooValidator"
                : className)                                    // "Foo" from "FooValidator"
                .replace("Validator", "");
        String recordFullName = isNested
                ? enclosingClassPrefix + recordName             // "Bar.Foo"
                : recordName;                                   // "Foo"

        String recordImportName = isNested
                ? packageName + "." + className.substring(0, dollarSign)  // "com.example.Bar"
                : packageName + "." + recordName;                         // "com.example.Foo"

        return new ValidatorClassWriter(
                packageName, className, enclosingClassPrefix,
                recordName, recordFullName, recordImportName,
                List.of()
        );
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
                parseNullSafeWriter(component.asType()),
                parseNullUnsafeWriters(component.asType())
        );
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

        TypeMirror itemType = getIterableItemType(fieldType);
        if (itemType == null) {
            return null;
        }

        var nullSafeWriter = parseNullSafeWriter(itemType);
        var nullUnsafeWriters = parseNullUnsafeWriters(itemType);

        if (nullSafeWriter == null && nullUnsafeWriters.isEmpty()) {
            return null;
        }

        return new ValidationWriter.IterableWriter(nullSafeWriter, nullUnsafeWriters);
    }

    private ValidationWriter.@Nullable NullSafeWriter parseNullSafeWriter(TypeMirror type) {
        return JakartaAnnotationParser.parseNullSafeWriter(new TypeAdapter(type, processingEnv));
    }

    private List<ValidationWriter.NullUnsafeWriter> parseNullUnsafeWriters(TypeMirror type) {
        return Stream.<ValidationWriter.NullUnsafeWriter>concat(
                JakartaAnnotationParser.parseNullUnsafeWriters(new TypeAdapter(type, processingEnv)),
                Stream.of(
                        parseValidateAnnotation(getReferredType(type)),
                        parseIterable(type)
                ).filter(Objects::nonNull)
        ).toList();
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

    private @Nullable TypeMirror getIterableItemType(TypeMirror type) {
        if (!(type instanceof DeclaredType declared)) {
            return null;
        }

        List<? extends TypeMirror> args = declared.getTypeArguments();
        if (args.isEmpty()) {
            return null;
        }

        return args.getFirst();
    }

}
