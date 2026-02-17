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
                
                import io.github.raniagus.javalidation.annotation.*;
                
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
                        import org.jspecify.annotations.Nullable;
                        
                        public class SimpleRecordValidator implements Validator<SimpleRecord> {
                           @Override
                           public ValidationErrors validate(@Nullable SimpleRecord root) {
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
                
                import io.github.raniagus.javalidation.annotation.*;
                import jakarta.validation.constraints.*;
                
                @Validate
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
                        import org.jspecify.annotations.Nullable;
                        import test.UserAddress;
                        import test.UserAddressValidator;
                        
                        public class UserRequestValidator implements Validator<UserRequest> {
                            private final Validator<UserAddress> addressValidator = new UserAddressValidator();

                            @Override
                            public ValidationErrors validate(@Nullable UserRequest root) {
                                Validation rootValidation = Validation.create();

                                var username = root.username();
                                var usernameValidation = Validation.create();
                                if (username == null) {
                                    usernameValidation.addRootError("must not be null");
                                }
                                if (username != null) {
                                    if (username.length() < 3 || username.length() > 50) {
                                        usernameValidation.addRootError("size must be between {0} and {1}", 3, 50);
                                    }
                                }
                                rootValidation.addAll(usernameValidation.finish(), new Object[]{"username"});

                                var email = root.email();
                                var emailValidation = Validation.create();
                                if (email != null) {
                                    if (!email.matches("^[^@]+@[^@]+\\\\.[^@]+$")) {
                                        emailValidation.addRootError("must be a well-formed email address");
                                    }
                                }
                                rootValidation.addAll(emailValidation.finish(), new Object[]{"email"});

                                var age = root.age();
                                var ageValidation = Validation.create();
                                if (age != null) {
                                    if (age < 18) {
                                        ageValidation.addRootError("must be greater than or equal to {0}", 18);
                                    }
                                }
                                rootValidation.addAll(ageValidation.finish(), new Object[]{"age"});

                                var address = root.address();
                                var addressValidation = Validation.create();
                                if (address == null) {
                                    addressValidation.addRootError("must not be null");
                                }
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
                        import org.jspecify.annotations.Nullable;
                        
                        public class UserAddressValidator implements Validator<UserAddress> {
                            @Override
                            public ValidationErrors validate(@Nullable UserAddress root) {
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
                
                import io.github.raniagus.javalidation.annotation.*;
                import jakarta.validation.constraints.*;
                import java.util.List;
                
                @Validate
                public record UserRequest(
                    @NotNull @Size(min = 3, max = 50) String username,
                    @Email String email,
                    @Min(18) Integer age,
                    @NotNull UserAddress address,
                    @NotNull List<@Size(min = 3, max = 10) String> tags,
                    @NotNull List<@NotNull Person> friends
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
                        import org.jspecify.annotations.Nullable;
                        import test.UserRequest;
                        import test.UserRequest$PersonValidator;
                        import test.UserRequest$UserAddressValidator;
                        
                        public class UserRequestValidator implements Validator<UserRequest> {
                            private final Validator<UserRequest.UserAddress> addressValidator = new UserRequest$UserAddressValidator();
                            private final Validator<UserRequest.Person> friendsItemValidator = new UserRequest$PersonValidator();

                            @Override
                            public ValidationErrors validate(@Nullable UserRequest root) {
                                Validation rootValidation = Validation.create();
                        
                                var username = root.username();
                                var usernameValidation = Validation.create();
                                if (username == null) {
                                    usernameValidation.addRootError("must not be null");
                                }
                                if (username != null) {
                                    if (username.length() < 3 || username.length() > 50) {
                                        usernameValidation.addRootError("size must be between {0} and {1}", 3, 50);
                                    }
                                }
                                rootValidation.addAll(usernameValidation.finish(), new Object[]{"username"});
                        
                                var email = root.email();
                                var emailValidation = Validation.create();
                                if (email != null) {
                                    if (!email.matches("^[^@]+@[^@]+\\\\.[^@]+$")) {
                                        emailValidation.addRootError("must be a well-formed email address");
                                    }
                                }
                                rootValidation.addAll(emailValidation.finish(), new Object[]{"email"});
                        
                                var age = root.age();
                                var ageValidation = Validation.create();
                                if (age != null) {
                                    if (age < 18) {
                                        ageValidation.addRootError("must be greater than or equal to {0}", 18);
                                    }
                                }
                                rootValidation.addAll(ageValidation.finish(), new Object[]{"age"});
                        
                                var address = root.address();
                                var addressValidation = Validation.create();
                                if (address == null) {
                                    addressValidation.addRootError("must not be null");
                                }
                                if (address != null) {
                                    addressValidation.addAll(addressValidator.validate(address));
                                }
                                rootValidation.addAll(addressValidation.finish(), new Object[]{"address"});
                        
                                var tags = root.tags();
                                var tagsValidation = Validation.create();
                                if (tags == null) {
                                    tagsValidation.addRootError("must not be null");
                                }
                                if (tags != null) {
                                    int tagsIndex = 0;
                                    for (var tagsItem : tags) {
                                        var tagsItemValidation = Validation.create();
                                        if (tagsItem != null) {
                                            if (tagsItem.length() < 3 || tagsItem.length() > 10) {
                                                tagsItemValidation.addRootError("size must be between {0} and {1}", 3, 10);
                                            }
                                        }
                                        tagsValidation.addAll(tagsItemValidation.finish(), new Object[]{tagsIndex++});
                                    }
                    
                                }
                                rootValidation.addAll(tagsValidation.finish(), new Object[]{"tags"});
                    
                                var friends = root.friends();
                                var friendsValidation = Validation.create();
                                if (friends == null) {
                                    friendsValidation.addRootError("must not be null");
                                }
                                if (friends != null) {
                                    int friendsIndex = 0;
                                    for (var friendsItem : friends) {
                                        var friendsItemValidation = Validation.create();
                                        if (friendsItem == null) {
                                            friendsItemValidation.addRootError("must not be null");
                                        }
                                        if (friendsItem != null) {
                                            friendsItemValidation.addAll(friendsItemValidator.validate(friendsItem));
                                        }
                                        friendsValidation.addAll(friendsItemValidation.finish(), new Object[]{friendsIndex++});
                                    }
                    
                                }
                                rootValidation.addAll(friendsValidation.finish(), new Object[]{"friends"});
                        
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
                        import org.jspecify.annotations.Nullable;
                        
                        public class UserRequest$UserAddressValidator implements Validator<UserRequest.UserAddress> {
                            @Override
                            public ValidationErrors validate(UserRequest.@Nullable UserAddress root) {
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
                        import test.UserRequest;
                        import test.UserRequest$PersonValidator;
                        import test.UserRequest$UserAddressValidator;
                        import test.UserRequestValidator;
                        
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
