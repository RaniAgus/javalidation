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
                
                import io.github.raniagus.javalidation.validator.*;
                
                @Validate
                public class InvalidClass {
                    private String field;
                }
                """
        );

        Compilation compilation = javac()
                .withProcessors(new ValidatorProcessor())
                .compile(sourceFile);

        assertThat(compilation).failed();
        assertThat(compilation)
                .hadErrorContaining("@Validate can only be applied to records");
    }

    @Test
    void shouldHandleRecordWithNoValidationAnnotations() {
        // Arrange - create source files in memory
        JavaFileObject sourceFile = JavaFileObjects.forSourceString("test.SimpleRecord", """
                package test;
                
                import io.github.raniagus.javalidation.validator.*;
                
                @Validate
                public record SimpleRecord(String name, int age) {}
                """
        );

        // Act - compile with your processor
        Compilation compilation = javac()
                .withProcessors(new ValidatorProcessor())
                .compile(sourceFile);

        // Assert - compilation succeeded
        assertThat(compilation).succeeded();

        // Assert - all generated files match expected
        assertThat(compilation)
                .generatedSourceFile("test.SimpleRecordValidator")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("test.SimpleRecordValidator", """
                        package test;
                        
                        import io.github.raniagus.javalidation.Validation;
                        import io.github.raniagus.javalidation.ValidationErrors;
                        import io.github.raniagus.javalidation.validator.Validator;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;
                        
                        @NullMarked
@Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class SimpleRecordValidator implements Validator<SimpleRecord> {
                           @Override
                           public ValidationErrors validate(SimpleRecord root) {
                               Validation rootValidation = Validation.create();
                               return rootValidation.finish();
                           }
                        }
                        """
                ));
        assertThat(compilation)
                .generatedSourceFile("io.github.raniagus.javalidation.validator.Validators")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("io.github.raniagus.javalidation.validator.Validators", """
                        package io.github.raniagus.javalidation.validator;
                        
                        import io.github.raniagus.javalidation.ValidationErrors;
                        import io.github.raniagus.javalidation.validator.Validator;
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
                
                import io.github.raniagus.javalidation.validator.*;
                import jakarta.validation.constraints.*;
                
                @Validate
                public record UserRequest(
                    String username,
                    String email,
                    Integer age,
                    UserAddress address
                ) {}
                """
        );

        JavaFileObject sourceFile2 = JavaFileObjects.forSourceString("test.UserAddress", """
                package test;
                
                import io.github.raniagus.javalidation.validator.*;
                import jakarta.validation.constraints.*;
                
                @Validate
                public record UserAddress(String street, String city) {}
                """
        );

        // Act - compile with your processor
        Compilation compilation = javac()
                .withProcessors(new ValidatorProcessor())
                .compile(sourceFile1, sourceFile2);

        // Assert - compilation succeeded
        assertThat(compilation).succeeded();

        // Assert - all generated files match expected
        assertThat(compilation)
                .generatedSourceFile("test.UserRequestValidator")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("test.UserRequestValidator", """
                        package test;
                        
                        import io.github.raniagus.javalidation.Validation;
                        import io.github.raniagus.javalidation.ValidationErrors;
                        import io.github.raniagus.javalidation.validator.Validator;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;
                        import test.UserAddress;
                        import test.UserAddressValidator;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class UserRequestValidator implements Validator<UserRequest> {
                            private final Validator<UserAddress> addressValidator = new UserAddressValidator();

                            @Override
                            public ValidationErrors validate(UserRequest root) {
                                Validation rootValidation = Validation.create();

                                var address = root.address();
                                var addressValidation = Validation.create();
                                if (address != null) {
                                    addressValidation.addAll(addressValidator.validate(address));
                                }
                                rootValidation.addAll(addressValidation.finish(), new Object[]{"address"});

                                return rootValidation.finish();
                            }
                        }
                        """
                ));
        assertThat(compilation)
                .generatedSourceFile("test.UserAddressValidator")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("test.UserAddressValidator", """
                        package test;
                        
                        import io.github.raniagus.javalidation.Validation;
                        import io.github.raniagus.javalidation.ValidationErrors;
                        import io.github.raniagus.javalidation.validator.Validator;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class UserAddressValidator implements Validator<UserAddress> {
                            @Override
                            public ValidationErrors validate(UserAddress root) {
                                Validation rootValidation = Validation.create();
                                return rootValidation.finish();
                            }
                        }
                        """
                ));
        assertThat(compilation)
                .generatedSourceFile("io.github.raniagus.javalidation.validator.Validators")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("io.github.raniagus.javalidation.validator.Validators", """
                        package io.github.raniagus.javalidation.validator;
                        
                        import io.github.raniagus.javalidation.ValidationErrors;
                        import io.github.raniagus.javalidation.validator.Validator;
                        import java.util.Map;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;
                        import test.UserAddress;
                        import test.UserAddressValidator;
                        import test.UserRequest;
                        import test.UserRequestValidator;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public final class Validators {
                            private static final Map<Class<?>, Validator<?>> CACHE;
                        
                            private Validators() {}
                        
                            static {
                                CACHE = Map.ofEntries(
                                        Map.entry(UserRequest.class, new UserRequestValidator())
                                      , Map.entry(UserAddress.class, new UserAddressValidator())
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
                
                import io.github.raniagus.javalidation.validator.*;
                import jakarta.validation.constraints.*;
                import java.util.List;
                
                @Validate
                public record UserRequest(
                    String username,
                    String email,
                    Integer age,
                    UserAddress address
                ) {
                    @Validate
                    public record UserAddress(String street, String city) {}

                    @Validate
                    public record Person(String name) {}
                }
                """
        );

        // Act - compile with your processor
        Compilation compilation = javac()
                .withProcessors(new ValidatorProcessor())
                .compile(sourceFile);

        // Assert - compilation succeeded
        assertThat(compilation).succeeded();

        // Assert - all generated files match expected
        assertThat(compilation)
                .generatedSourceFile("test.UserRequestValidator")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("test.UserRequestValidator", """
                        package test;
                        
                        import io.github.raniagus.javalidation.Validation;
                        import io.github.raniagus.javalidation.ValidationErrors;
                        import io.github.raniagus.javalidation.validator.Validator;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;
                        import test.UserRequest;
                        import test.UserRequest$UserAddressValidator;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class UserRequestValidator implements Validator<UserRequest> {
                            private final Validator<UserRequest.UserAddress> addressValidator = new UserRequest$UserAddressValidator();

                            @Override
                            public ValidationErrors validate(UserRequest root) {
                                Validation rootValidation = Validation.create();

                                var address = root.address();
                                var addressValidation = Validation.create();
                                if (address != null) {
                                    addressValidation.addAll(addressValidator.validate(address));
                                }
                                rootValidation.addAll(addressValidation.finish(), new Object[]{"address"});

                                return rootValidation.finish();
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
                        import io.github.raniagus.javalidation.ValidationErrors;
                        import io.github.raniagus.javalidation.validator.Validator;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class UserRequest$UserAddressValidator implements Validator<UserRequest.UserAddress> {
                            @Override
                            public ValidationErrors validate(UserRequest.UserAddress root) {
                                Validation rootValidation = Validation.create();
                                return rootValidation.finish();
                            }
                        }
                        """
                ));
        assertThat(compilation)
                .generatedSourceFile("io.github.raniagus.javalidation.validator.Validators")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("io.github.raniagus.javalidation.validator.Validators", """
                        package io.github.raniagus.javalidation.validator;
                        
                        import io.github.raniagus.javalidation.ValidationErrors;
                        import io.github.raniagus.javalidation.validator.Validator;
                        import java.util.Map;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;
                        import test.UserRequest;
                        import test.UserRequest$PersonValidator;
                        import test.UserRequest$UserAddressValidator;
                        import test.UserRequestValidator;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public final class Validators {
                            private static final Map<Class<?>, Validator<?>> CACHE;
                        
                            private Validators() {}
                        
                            static {
                                CACHE = Map.ofEntries(
                                        Map.entry(UserRequest.class, new UserRequestValidator())
                                      , Map.entry(UserRequest.UserAddress.class, new UserRequest$UserAddressValidator())
                                      , Map.entry(UserRequest.Person.class, new UserRequest$PersonValidator())
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
