package io.github.raniagus.javalidation.validator.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

class ValidateAnnotationTest {

    @Test
    void shouldReportErrorForNonRecordType() {
        JavaFileObject sourceFile = JavaFileObjects.forSourceString("test.InvalidClass", """
                package test;

                public class InvalidClass {
                    private String field;
                }
                """
        );

        JavaFileObject triggerFile = JavaFileObjects.forSourceString("test.SimpleService", """
                package test;
    
                import jakarta.validation.*;
    
                public class SimpleService {
                    public void doSomething(@Valid InvalidClass input) {}
                }
                """
        );

        Compilation compilation = javac()
                .withProcessors(new ValidatorProcessor())
                .compile(sourceFile, triggerFile);

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .hadWarningContaining("@Valid can only be applied to records, but it was applied to test.InvalidClass");
    }

    @Test
    void shouldHandleRecordWithNoValidationAnnotations() {
        // Arrange - create source files in memory
        JavaFileObject recordFile = JavaFileObjects.forSourceString("test.SimpleRecord", """
                package test;
    
                public record SimpleRecord(String name, int age) {}
                """
        );

        JavaFileObject triggerFile = JavaFileObjects.forSourceString("test.SimpleService", """
                package test;
    
                import jakarta.validation.*;
    
                public class SimpleService {
                    public void doSomething(@Valid SimpleRecord input) {}
                }
                """
        );

        // Act - compile with your processor
        Compilation compilation = javac()
                .withProcessors(new ValidatorProcessor())
                .compile(recordFile, triggerFile);

        // Assert - compilation succeeded
        assertThat(compilation).succeeded();

        // Assert - all generated files match expected
        assertThat(compilation)
                .generatedSourceFile("test.SimpleRecordValidator")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("test.SimpleRecordValidator", """
                        package test;
                        
                        import io.github.raniagus.javalidation.Validation;
                        import io.github.raniagus.javalidation.validator.Validator;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;
                        
                        @NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class SimpleRecordValidator implements Validator<SimpleRecord> {
                           @Override
                           public void validate(Validation validation, SimpleRecord root) {
                        
                           }
                        }
                        """
                ));
        assertThat(compilation)
                .generatedSourceFile("io.github.raniagus.javalidation.validator.Validators")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("io.github.raniagus.javalidation.validator.Validators", """
                        package io.github.raniagus.javalidation.validator;
                        
                        import io.github.raniagus.javalidation.ValidationErrors;
                        import java.util.Map;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;
                        import test.SimpleRecord;
                        import test.SimpleRecordValidator;
                        
                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public final class Validators {
                            private static final Map<Class<?>, Validator<?>> CACHE;
                        
                            private Validators() {}
                        
                            static {
                                CACHE = Map.ofEntries(
                                    Map.entry(SimpleRecord.class, new SimpleRecordValidator())
                                );
                            }

                            public static boolean hasValidator(Class<?> clazz) {
                                return CACHE.containsKey(clazz);
                            }
                        
                            @SuppressWarnings("unchecked")
                            public static <T> ValidationErrors validate(T instance) {
                                Validator<T> validator = getValidator((Class<T>) instance.getClass());
                                return validator.validate(instance);
                            }

                            @SuppressWarnings("unchecked")
                            public static <T> Validator<T> getValidator(Class<T> clazz) {
                                 Validator<?> validator = CACHE.get(clazz);
                                 if (validator == null) {
                                     throw new IllegalArgumentException(
                                         "No validator registered for " + clazz.getName()
                                     );
                                 }
                                 return (Validator<T>) validator;
                            }
                        }
                        """));
    }

    @Test
    void shouldGenerateValidatorForAnnotatedRecord() {
        // Arrange - create source files in memory
        JavaFileObject sourceFile1 = JavaFileObjects.forSourceString("test.UserRequest", """
                package test;

                import jakarta.validation.*;
                import jakarta.validation.constraints.*;
                import other.UserAddress;

                public record UserRequest(
                    String username,
                    String email,
                    Integer age,
                    @Valid UserAddress address
                ) {}
                """
        );

        JavaFileObject sourceFile2 = JavaFileObjects.forSourceString("other.UserAddress", """
                package other;

                public record UserAddress(String street, String city) {}
                """
        );

        JavaFileObject triggerFile = JavaFileObjects.forSourceString("test.UserService", """
                package test;
        
                import jakarta.validation.Valid;
        
                public class UserService {
                    public void create(@Valid UserRequest request) {}
                }
                """
        );

        // Act - compile with your processor
        Compilation compilation = javac()
                .withProcessors(new ValidatorProcessor())
                .compile(sourceFile1, sourceFile2, triggerFile);

        // Assert - compilation succeeded
        assertThat(compilation).succeeded();

        // Assert - all generated files match expected
        assertThat(compilation)
                .generatedSourceFile("test.UserRequestValidator")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("test.UserRequestValidator", """
                        package test;
                        
                        import io.github.raniagus.javalidation.Validation;
                        import io.github.raniagus.javalidation.validator.Validator;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;
                        import other.UserAddress;
                        import other.UserAddressValidator;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class UserRequestValidator implements Validator<UserRequest> {
                            private final Validator<UserAddress> addressValidator = new UserAddressValidator();

                            @Override
                            public void validate(Validation validation, UserRequest root) {
                                validation.withField("address", () -> {
                                    var address = root.address();
                                    if (address == null) return;
                                    addressValidator.validate(validation, address);
                                });
                            }
                        }
                        """
                ));
        assertThat(compilation)
                .generatedSourceFile("other.UserAddressValidator")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("other.UserAddressValidator", """
                        package other;
                        
                        import io.github.raniagus.javalidation.Validation;
                        import io.github.raniagus.javalidation.validator.Validator;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class UserAddressValidator implements Validator<UserAddress> {
                            @Override
                            public void validate(Validation validation, UserAddress root) {

                            }
                        }
                        """
                ));
        assertThat(compilation)
                .generatedSourceFile("io.github.raniagus.javalidation.validator.Validators")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("io.github.raniagus.javalidation.validator.Validators", """
                        package io.github.raniagus.javalidation.validator;
                        
                        import io.github.raniagus.javalidation.ValidationErrors;
                        import java.util.Map;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;
                        import other.UserAddress;
                        import other.UserAddressValidator;
                        import test.UserRequest;
                        import test.UserRequestValidator;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public final class Validators {
                            private static final Map<Class<?>, Validator<?>> CACHE;
                        
                            private Validators() {}
                        
                            static {
                                CACHE = Map.ofEntries(
                                        Map.entry(UserAddress.class, new UserAddressValidator())
                                      , Map.entry(UserRequest.class, new UserRequestValidator())
                                );
                            }

                            public static boolean hasValidator(Class<?> clazz) {
                                return CACHE.containsKey(clazz);
                            }

                            @SuppressWarnings("unchecked")
                            public static <T> ValidationErrors validate(T instance) {
                                Validator<T> validator = getValidator((Class<T>) instance.getClass());
                                return validator.validate(instance);
                            }
                        
                            @SuppressWarnings("unchecked")
                            public static <T> Validator<T> getValidator(Class<T> clazz) {
                                 Validator<?> validator = CACHE.get(clazz);
                                 if (validator == null) {
                                     throw new IllegalArgumentException(
                                         "No validator registered for " + clazz.getName()
                                     );
                                 }
                                 return (Validator<T>) validator;
                            }
                        }
                        """));
    }

    @Test
    void shouldGenerateValidatorForAnnotatedRecordWithNestedRecord() {
        // Arrange - create source files in memory
        JavaFileObject sourceFile = JavaFileObjects.forSourceString("test.UserRequest", """
                package test;

                import jakarta.validation.*;
                import jakarta.validation.constraints.*;
                import java.util.List;

                public record UserRequest(
                    String username,
                    String email,
                    Integer age,
                    @Valid UserAddress address
                ) {
                    public record UserAddress(String street, String city) {}

                    public record Person(String name) {}
                }
                """
        );

        JavaFileObject triggerFile = JavaFileObjects.forSourceString("test.UserService", """
            package test;
    
            import jakarta.validation.Valid;
    
            public class UserService {
                public void create(@Valid UserRequest request) {}
            }
            """
        );

        // Act - compile with your processor
        Compilation compilation = javac()
                .withProcessors(new ValidatorProcessor())
                .compile(sourceFile, triggerFile);

        // Assert - compilation succeeded
        assertThat(compilation).succeeded();

        // Assert - all generated files match expected
        assertThat(compilation)
                .generatedSourceFile("test.UserRequestValidator")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("test.UserRequestValidator", """
                        package test;
                        
                        import io.github.raniagus.javalidation.Validation;
                        import io.github.raniagus.javalidation.validator.Validator;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class UserRequestValidator implements Validator<UserRequest> {
                            private final Validator<UserRequest.UserAddress> addressValidator = new UserRequest$UserAddressValidator();

                            @Override
                            public void validate(Validation validation, UserRequest root) {
                                validation.withField("address", () -> {
                                    var address = root.address();
                                    if (address == null) return;
                                    addressValidator.validate(validation, address);
                                });
                            }
                        }
                        """
                ));
        assertThat(compilation)
                .generatedSourceFile("test.UserRequest$UserAddressValidator")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString(
                        "test.UserRequest$UserAddressValidator",
                        """
                        package test;
                        
                        import io.github.raniagus.javalidation.Validation;
                        import io.github.raniagus.javalidation.validator.Validator;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class UserRequest$UserAddressValidator implements Validator<UserRequest.UserAddress> {
                            @Override
                            public void validate(Validation validation, UserRequest.UserAddress root) {

                            }
                        }
                        """
                ));
        assertThat(compilation)
                .generatedSourceFile("io.github.raniagus.javalidation.validator.Validators")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("io.github.raniagus.javalidation.validator.Validators", """
                        package io.github.raniagus.javalidation.validator;
                        
                        import io.github.raniagus.javalidation.ValidationErrors;
                        import java.util.Map;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;
                        import test.UserRequest;
                        import test.UserRequest$UserAddressValidator;
                        import test.UserRequestValidator;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public final class Validators {
                            private static final Map<Class<?>, Validator<?>> CACHE;
                        
                            private Validators() {}
                        
                            static {
                                CACHE = Map.ofEntries(
                                        Map.entry(UserRequest.UserAddress.class, new UserRequest$UserAddressValidator())
                                      , Map.entry(UserRequest.class, new UserRequestValidator())
                                );
                            }

                            public static boolean hasValidator(Class<?> clazz) {
                                return CACHE.containsKey(clazz);
                            }

                            @SuppressWarnings("unchecked")
                            public static <T> ValidationErrors validate(T instance) {
                                Validator<T> validator = getValidator((Class<T>) instance.getClass());
                                return validator.validate(instance);
                            }
                        
                            @SuppressWarnings("unchecked")
                            public static <T> Validator<T> getValidator(Class<T> clazz) {
                                 Validator<?> validator = CACHE.get(clazz);
                                 if (validator == null) {
                                     throw new IllegalArgumentException(
                                         "No validator registered for " + clazz.getName()
                                     );
                                 }
                                 return (Validator<T>) validator;
                            }
                        }
                        """));
    }
}
