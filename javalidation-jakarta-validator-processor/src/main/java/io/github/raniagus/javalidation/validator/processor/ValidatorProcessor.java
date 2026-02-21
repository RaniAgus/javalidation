package io.github.raniagus.javalidation.validator.processor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.stream.Stream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.jspecify.annotations.Nullable;

@SupportedAnnotationTypes("*")
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

        return false;
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
    private List<RecordValidatorClassWriter> reconstructWriters() {
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
    public static RecordValidatorClassWriter fromFullName(String fullName) {
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

        return new RecordValidatorClassWriter(
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

    // -- Annotation scan --

    private List<ValidatorClassWriter> parseClassWriters(RoundEnvironment roundEnv) {
        return roundEnv.getRootElements().stream()
                .flatMap(element -> collectRecordsWithValidFields(element).stream())
                .distinct()
                .map(te -> te.getModifiers().contains(Modifier.SEALED)
                        ? parseSealedClassWriter(te)
                        : parseRecordClassWriter(te))
                .toList();
    }

    private List<TypeElement> collectRecordsWithValidFields(Element root) {
        List<TypeElement> result = new ArrayList<>();
        collectValidRecordsRecursively(root, result);
        return result;
    }

    private void collectValidRecordsRecursively(Element element, List<TypeElement> result) {
        if (!(element instanceof TypeElement typeElement)) return;

        // Scan methods for @Valid parameters
        typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD || e.getKind() == ElementKind.CONSTRUCTOR)
                .flatMap(e -> e instanceof ExecutableElement executableElement ?
                          executableElement.getParameters().stream()
                        : Stream.empty()
                )
                .filter(this::isAnnotatedWithValid)
                .forEach(param -> {
                    TypeElement referred = getReferredType(param.asType());
                    if (referred == null) return;

                    if (referred.getModifiers().contains(Modifier.SEALED)) {
                        referred.getPermittedSubclasses().stream()
                                .map(this::getReferredType)
                                .filter(Objects::nonNull)
                                .forEach(subtype -> {
                                    if (subtype.getKind() == ElementKind.RECORD) {
                                        collectNestedValidRecords(subtype, result);
                                        result.add(subtype);
                                        return;
                                    }

                                    processingEnv.getMessager().printMessage(
                                            Diagnostic.Kind.WARNING,
                                            "Permitted subtype " + subtype + " is not a record, skipping",
                                            param
                                    );
                                });
                        result.add(referred);
                        return;
                    }

                    if (referred.getKind() == ElementKind.RECORD) {
                        collectNestedValidRecords(referred, result);
                        result.add(referred);
                        return;
                    }

                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.WARNING,
                            "@Valid can only be applied to records, but it was applied to " + referred,
                            param
                    );
                });

        // Recurse into nested types
        typeElement.getEnclosedElements().forEach(e -> collectValidRecordsRecursively(e, result));
    }

    private void collectNestedValidRecords(TypeElement recordElement, List<TypeElement> result) {
        recordElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.RECORD_COMPONENT)
                .map(e -> (VariableElement) e)
                .forEach(component -> {
                    // @Valid directly on the field type
                    TypeElement referred = getReferredType(component.asType());
                    if (referred != null && referred.getKind() == ElementKind.RECORD
                            && isAnnotatedWithValid(component)) {
                        result.add(referred);
                        collectNestedValidRecords(referred, result); // recurse
                    }

                    // @Valid on type argument e.g. List<@Valid TransferRequestDTO>
                    getValidTypeArguments(component.asType()).forEach(argType -> {
                        TypeElement argElement = getReferredType(argType);
                        if (argElement != null && argElement.getKind() == ElementKind.RECORD) {
                            result.add(argElement);
                            collectNestedValidRecords(argElement, result); // recurse
                        }
                    });
                });
    }

    private List<TypeMirror> getValidTypeArguments(TypeMirror type) {
        if (!(type instanceof DeclaredType declared)) return List.of();
        return declared.getTypeArguments().stream()
                .filter(arg -> arg.getAnnotationMirrors().stream()
                        .anyMatch(a -> a.getAnnotationType()
                                .asElement()
                                .getSimpleName()
                                .contentEquals("Valid")))
                .map(TypeMirror.class::cast)
                .toList();
    }

    private boolean isAnnotatedWithValid(TypeMirror type) {
        return type.getAnnotationMirrors().stream()
                .anyMatch(a -> a.getAnnotationType()
                        .asElement()
                        .getSimpleName()
                        .contentEquals("Valid"));
    }

    private boolean isAnnotatedWithValid(VariableElement param) {
        boolean onElement = param.getAnnotationMirrors().stream()
                .anyMatch(a -> a.getAnnotationType()
                        .asElement()
                        .getSimpleName()
                        .contentEquals("Valid"));

        boolean onType = param.asType().getAnnotationMirrors().stream()
                .anyMatch(a -> a.getAnnotationType()
                        .asElement()
                        .getSimpleName()
                        .contentEquals("Valid"));

        return onElement || onType;
    }

    // -- Class writer --

    private ValidatorClassWriter parseSealedClassWriter(TypeElement sealedInterface) {
        List<ValidatorClassWriter> permittedWriters = sealedInterface.getPermittedSubclasses().stream()
                .map(this::getReferredType)
                .filter(Objects::nonNull)
                .filter(te -> te.getKind() == ElementKind.RECORD)
                .map(this::parseRecordClassWriter)
                .toList();

        return new SealedValidatorClassWriter(
                processingEnv.getElementUtils().getPackageOf(sealedInterface).getQualifiedName().toString(),
                getValidatorName(sealedInterface),
                getEnclosingClassPrefix(sealedInterface, "."),
                getRecordName(sealedInterface),
                getRecordFullName(sealedInterface),
                getRecordImportName(sealedInterface),
                permittedWriters
        );
    }

    private ValidatorClassWriter parseRecordClassWriter(TypeElement typeElement) {
        return new RecordValidatorClassWriter(
                processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString(),
                getValidatorName(typeElement),
                getEnclosingClassPrefix(typeElement, "."),
                getRecordName(typeElement),
                getRecordFullName(typeElement),
                getRecordImportName(typeElement),
                parseFieldWriters(typeElement)
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
        if (component.asType().getKind().isPrimitive()) {
            return new FieldWriter.PrimitiveWriter(
                    component.getSimpleName().toString(),
                    parseNullUnsafeWriters(component.asType())
            );
        }

        return new FieldWriter.ObjectWriter(
                component.getSimpleName().toString(),
                parseNullSafeWriter(component.asType()),
                parseNullUnsafeWriters(component.asType())
        );
    }

    private @Nullable NullSafeWriter parseNullSafeWriter(TypeMirror type) {
        if (shouldSkip(type)) {
            return null;
        }
        return JakartaAnnotationParser.parseNullSafeWriter(new TypeAdapter(type, processingEnv));
    }

    private List<NullUnsafeWriter> parseNullUnsafeWriters(TypeMirror type) {
        if (shouldSkip(type)) {
            return List.of();
        }

        return Stream.<NullUnsafeWriter>concat(
                JakartaAnnotationParser.parseNullUnsafeWriters(new TypeAdapter(type, processingEnv)),
                Stream.of(parseNested(type), parseIterable(type), parseMap(type)).filter(Objects::nonNull)
        ).toList();
    }

    // -- Nested --

    private @Nullable NullUnsafeWriter parseNested(TypeMirror type) {
        if (!isAnnotatedWithValid(type)) {
            return null;
        }

        Element referredType = getReferredType(type);
        if (referredType == null || referredType.getKind() != ElementKind.RECORD) {
            return null;
        }

        return new NullUnsafeWriter.Validate(
                getRecordImportName(referredType),
                getEnclosingClassPrefix(referredType, "."),
                getRecordName(referredType),
                getValidatorName(referredType),
                getValidatorFullName(referredType)
        );
    }

    // -- Iterable --

    private @Nullable NullUnsafeWriter parseIterable(TypeMirror fieldType) {
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

        return new NullUnsafeWriter.IterableWriter(nullSafeWriter, nullUnsafeWriters);
    }

    private boolean isIterable(TypeMirror type) {
        Types types = processingEnv.getTypeUtils();
        Elements elements = processingEnv.getElementUtils();

        TypeElement iterableElement = elements.getTypeElement("java.lang.Iterable");
        TypeMirror iterableType = types.erasure(iterableElement.asType());

        return types.isAssignable(types.erasure(type), iterableType);
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

    // -- Map --

    private @Nullable NullUnsafeWriter parseMap(TypeMirror fieldType) {
        if (!isMap(fieldType)) {
            return null;
        }

        var mapType = getMapType(fieldType);
        if (mapType == null) {
            return null;
        }

        var keyNullSafeWriter = parseNullSafeWriter(mapType.getKey());
        var keyNullUnsafeWriters = parseNullUnsafeWriters(mapType.getKey());
        var valueNullSafeWriter = parseNullSafeWriter(mapType.getValue());
        var valueNullUnsafeWriters = parseNullUnsafeWriters(mapType.getValue());

        if (keyNullSafeWriter == null && keyNullUnsafeWriters.isEmpty()
                && valueNullSafeWriter == null && valueNullUnsafeWriters.isEmpty()) {
            return null;
        }

        return new NullUnsafeWriter.MapWriter(
                keyNullSafeWriter, keyNullUnsafeWriters,
                valueNullSafeWriter, valueNullUnsafeWriters
        );
    }

    private boolean isMap(TypeMirror type) {
        Types types = processingEnv.getTypeUtils();
        Elements elements = processingEnv.getElementUtils();

        TypeElement mapElement = elements.getTypeElement("java.util.Map");
        TypeMirror mapType = types.erasure(mapElement.asType());

        return types.isAssignable(types.erasure(type), mapType);
    }

    private Map.@Nullable Entry<TypeMirror, TypeMirror> getMapType(TypeMirror type) {
        if (!(type instanceof DeclaredType declared)) {
            return null;
        }

        List<? extends TypeMirror> args = declared.getTypeArguments();
        if (args.size() < 2) {
            return null;
        }

        return Map.entry(args.getFirst(), args.get(1));
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

    private boolean shouldSkip(TypeMirror type) {
        return type.getAnnotationMirrors().stream()
                .anyMatch(a -> a.getAnnotationType().asElement().getSimpleName().contentEquals("SkipValidate"));
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
