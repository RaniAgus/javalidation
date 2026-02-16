package io.github.raniagus.javalidation.validator;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import io.github.raniagus.javalidation.processor.ValidatorProcessor;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

class ValidatorProcessorTest {

    @Test
    void shouldReportErrorForNonRecordType() {
        JavaFileObject sourceFile = JavaFileObjects.forSourceString("test.InvalidClass", """
                package test;
                
                import io.github.raniagus.javalidation.annotation.*;
                
                @Validator
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
                .hadErrorContaining("@Validator can only be applied to records");
    }

    @Test
    void shouldHandleRecordWithNoValidationAnnotations() {
        // Arrange - create source files in memory
        JavaFileObject sourceFile = JavaFileObjects.forSourceString("test.SimpleRecord", """
                package test;
                
                import io.github.raniagus.javalidation.annotation.*;
                
                @Validator
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
                        import org.jspecify.annotations.Nullable;
                        
                        public class SimpleRecordValidator implements Validator<SimpleRecord> {
                           @Override
                           public ValidationErrors validate(@Nullable SimpleRecord obj0) {
                               Validation validation = Validation.create();
                               return validation.finish();
                           }
                        }
                        """
                ));
        assertThat(compilation)
                .generatedSourceFile("io.github.raniagus.javalidation.validator.Validators")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("io.github.raniagus.javalidation.validator.Validators", """
                        package io.github.raniagus.javalidation.validator;
                        
                        import io.github.raniagus.javalidation.validator.Validator;
                        import java.util.Map;
                        import test.SimpleRecord;
                        import test.SimpleRecordValidator;
                        
                        public final class Validators {
                            private static final Map<Class<?>, Validator<?>> CACHE;
                        
                            private Validators() {}
                        
                            static {
                                CACHE = Map.ofEntries(
                                    Map.entry(SimpleRecord.class, new SimpleRecordValidator())
                                );
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
                
                import io.github.raniagus.javalidation.annotation.*;
                import jakarta.validation.constraints.*;
                
                @Validator
                public record UserRequest(
                    @NotNull @Size(min = 3, max = 50) String username,
                    @Email String email,
                    @Min(18) Integer age,
                    @NotNull UserAddress address
                ) {}
                """
        );

        JavaFileObject sourceFile2 = JavaFileObjects.forSourceString("test.UserAddress", """
                package test;
                
                import io.github.raniagus.javalidation.annotation.*;
                import jakarta.validation.constraints.*;
                
                @Validator
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
                        import org.jspecify.annotations.Nullable;
                        import test.UserAddress;
                        import test.UserAddressValidator;
                        
                        public class UserRequestValidator implements Validator<UserRequest> {
                            private final Validator<UserAddress> addressValidator = new UserAddressValidator();

                            @Override
                            public ValidationErrors validate(@Nullable UserRequest obj0) {
                                Validation validation = Validation.create();
                                if (obj0.username() == null) {
                                    validation.addFieldError("username", "must not be null");
                                }
                                if (obj0.username() != null) {
                                    if (obj0.username().length() < 3 || obj0.username().length() > 50) {
                                        validation.addFieldError("username", "size must be between {0} and {1}", 3, 50);
                                    }
                                }
                                if (obj0.email() != null) {
                                    if (!obj0.email().matches("^[^@]+@[^@]+\\\\.[^@]+$")) {
                                        validation.addFieldError("email", "must be a well-formed email address");
                                    }
                                }
                                if (obj0.age() != null) {
                                    if (obj0.age() < 18) {
                                        validation.addFieldError("age", "must be greater than or equal to {0}", 18);
                                    }
                                }
                                if (obj0.address() == null) {
                                    validation.addFieldError("address", "must not be null");
                                }
                                if (obj0.address() != null) {
                                    validation.addAll(addressValidator.validate(obj0.address()), new Object[]{"address"});
                                }
                                return validation.finish();
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
                        import org.jspecify.annotations.Nullable;
                        
                        public class UserAddressValidator implements Validator<UserAddress> {
                            @Override
                            public ValidationErrors validate(@Nullable UserAddress obj0) {
                                Validation validation = Validation.create();
                                return validation.finish();
                            }
                        }
                        """
                ));
        assertThat(compilation)
                .generatedSourceFile("io.github.raniagus.javalidation.validator.Validators")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("io.github.raniagus.javalidation.validator.Validators", """
                        package io.github.raniagus.javalidation.validator;
                        
                        import io.github.raniagus.javalidation.validator.Validator;
                        import java.util.Map;
                        import test.UserAddress;
                        import test.UserAddressValidator;
                        import test.UserRequest;
                        import test.UserRequestValidator;
                        
                        public final class Validators {
                            private static final Map<Class<?>, Validator<?>> CACHE;
                        
                            private Validators() {}
                        
                            static {
                                CACHE = Map.ofEntries(
                                        Map.entry(UserRequest.class, new UserRequestValidator())
                                      , Map.entry(UserAddress.class, new UserAddressValidator())
                                );
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
                
                import io.github.raniagus.javalidation.annotation.*;
                import jakarta.validation.constraints.*;
                
                @Validator
                public record UserRequest(
                    @NotNull @Size(min = 3, max = 50) String username,
                    @Email String email,
                    @Min(18) Integer age,
                    @NotNull UserAddress address
                ) {
                    @Validator
                    public record UserAddress(String street, String city) {}
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
                        import org.jspecify.annotations.Nullable;
                        import test.UserRequest;
                        import test.UserRequest$UserAddressValidator;
                        
                        public class UserRequestValidator implements Validator<UserRequest> {
                            private final Validator<UserRequest.UserAddress> addressValidator = new UserRequest$UserAddressValidator();

                            @Override
                            public ValidationErrors validate(@Nullable UserRequest obj0) {
                                Validation validation = Validation.create();
                                if (obj0.username() == null) {
                                    validation.addFieldError("username", "must not be null");
                                }
                                if (obj0.username() != null) {
                                    if (obj0.username().length() < 3 || obj0.username().length() > 50) {
                                        validation.addFieldError("username", "size must be between {0} and {1}", 3, 50);
                                    }
                                }
                                if (obj0.email() != null) {
                                    if (!obj0.email().matches("^[^@]+@[^@]+\\\\.[^@]+$")) {
                                        validation.addFieldError("email", "must be a well-formed email address");
                                    }
                                }
                                if (obj0.age() != null) {
                                    if (obj0.age() < 18) {
                                        validation.addFieldError("age", "must be greater than or equal to {0}", 18);
                                    }
                                }
                                if (obj0.address() == null) {
                                    validation.addFieldError("address", "must not be null");
                                }
                                if (obj0.address() != null) {
                                    validation.addAll(addressValidator.validate(obj0.address()), new Object[]{"address"});
                                }
                                return validation.finish();
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
                        import org.jspecify.annotations.Nullable;
                        
                        public class UserRequest$UserAddressValidator implements Validator<UserRequest.UserAddress> {
                            @Override
                            public ValidationErrors validate(UserRequest.@Nullable UserAddress obj0) {
                                Validation validation = Validation.create();
                                return validation.finish();
                            }
                        }
                        """
                ));
        assertThat(compilation)
                .generatedSourceFile("io.github.raniagus.javalidation.validator.Validators")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("io.github.raniagus.javalidation.validator.Validators", """
                        package io.github.raniagus.javalidation.validator;
                        
                        import io.github.raniagus.javalidation.validator.Validator;
                        import java.util.Map;
                        import test.UserRequest;
                        import test.UserRequest$UserAddressValidator;
                        import test.UserRequestValidator;
                        
                        public final class Validators {
                            private static final Map<Class<?>, Validator<?>> CACHE;
                        
                            private Validators() {}
                        
                            static {
                                CACHE = Map.ofEntries(
                                        Map.entry(UserRequest.class, new UserRequestValidator())
                                      , Map.entry(UserRequest.UserAddress.class, new UserRequest$UserAddressValidator())
                                );
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
