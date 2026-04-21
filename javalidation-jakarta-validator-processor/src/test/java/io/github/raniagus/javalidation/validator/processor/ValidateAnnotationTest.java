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
                        import io.github.raniagus.javalidation.validator.InitializableValidator;
                        import io.github.raniagus.javalidation.validator.ValidatorsHolder;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;
                        
                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class SimpleRecordValidator implements InitializableValidator<SimpleRecord> {

                            @Override
                            public void initialize(ValidatorsHolder holder) {
                            }

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
                            private static final ValidatorsHolder HOLDER;
                        
                            private Validators() {}
                        
                            static {
                                HOLDER = new ValidatorsHolder(Map.ofEntries(
                                        Map.entry(SimpleRecord.class, new SimpleRecordValidator())
                                ));
                                HOLDER.initialize();
                            }
                        
                            public static boolean hasValidator(Class<?> clazz) {
                                return HOLDER.hasValidator(clazz);
                            }
                        
                            public static <T> ValidationErrors validate(T instance) {
                                 return HOLDER.validate(instance);
                            }
                        
                            public static <T> Validator<T> getValidator(Class<T> clazz) {
                                 return HOLDER.getValidator(clazz);
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
                        import io.github.raniagus.javalidation.validator.InitializableValidator;
                        import io.github.raniagus.javalidation.validator.Validator;
                        import io.github.raniagus.javalidation.validator.ValidatorsHolder;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;
                        import other.UserAddress;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class UserRequestValidator implements InitializableValidator<UserRequest> {
                            private Validator<UserAddress> addressValidator;

                            @Override
                            public void initialize(ValidatorsHolder holder) {
                                addressValidator = holder.getValidator(UserAddress.class);
                            }

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
                        import io.github.raniagus.javalidation.validator.InitializableValidator;
                        import io.github.raniagus.javalidation.validator.ValidatorsHolder;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class UserAddressValidator implements InitializableValidator<UserAddress> {
        
                            @Override
                            public void initialize(ValidatorsHolder holder) {
                            }

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
                            private static final ValidatorsHolder HOLDER;
                        
                            private Validators() {}
                        
                            static {
                                HOLDER = new ValidatorsHolder(Map.ofEntries(
                                        Map.entry(UserAddress.class, new UserAddressValidator())
                                      , Map.entry(UserRequest.class, new UserRequestValidator())
                                ));
                                HOLDER.initialize();
                            }

                            public static boolean hasValidator(Class<?> clazz) {
                                return HOLDER.hasValidator(clazz);
                            }

                            public static <T> ValidationErrors validate(T instance) {
                                return HOLDER.validate(instance);
                            }
                        
                            public static <T> Validator<T> getValidator(Class<T> clazz) {
                                 return HOLDER.getValidator(clazz);
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
                        import io.github.raniagus.javalidation.validator.InitializableValidator;
                        import io.github.raniagus.javalidation.validator.Validator;
                        import io.github.raniagus.javalidation.validator.ValidatorsHolder;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class UserRequestValidator implements InitializableValidator<UserRequest> {
                            private Validator<UserRequest.UserAddress> addressValidator;

                            @Override
                            public void initialize(ValidatorsHolder holder) {
                                addressValidator = holder.getValidator(UserRequest.UserAddress.class);
                            }
                        
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
                        import io.github.raniagus.javalidation.validator.InitializableValidator;
                        import io.github.raniagus.javalidation.validator.ValidatorsHolder;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class UserRequest$UserAddressValidator implements InitializableValidator<UserRequest.UserAddress> {

                            @Override
                            public void initialize(ValidatorsHolder holder) {
                            }

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
                            private static final ValidatorsHolder HOLDER;
                        
                            private Validators() {}
                        
                            static {
                                HOLDER = new ValidatorsHolder(Map.ofEntries(
                                        Map.entry(UserRequest.UserAddress.class, new UserRequest$UserAddressValidator())
                                      , Map.entry(UserRequest.class, new UserRequestValidator())
                                ));
                                HOLDER.initialize();
                            }
                        
                            public static boolean hasValidator(Class<?> clazz) {
                                return HOLDER.hasValidator(clazz);
                            }
                        
                            public static <T> ValidationErrors validate(T instance) {
                                 return HOLDER.validate(instance);
                            }
                        
                            public static <T> Validator<T> getValidator(Class<T> clazz) {
                                 return HOLDER.getValidator(clazz);
                            }
                        }
                        """));
    }


    @Test
    void shouldGenerateValidatorForAnnotatedRecordWithCyclicNestedRecord() {
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
                    public record UserAddress(String street, String city, @Valid UserRequest parent) {}
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
                        import io.github.raniagus.javalidation.validator.InitializableValidator;
                        import io.github.raniagus.javalidation.validator.Validator;
                        import io.github.raniagus.javalidation.validator.ValidatorsHolder;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class UserRequestValidator implements InitializableValidator<UserRequest> {
                            private Validator<UserRequest.UserAddress> addressValidator;

                            @Override
                            public void initialize(ValidatorsHolder holder) {
                                addressValidator = holder.getValidator(UserRequest.UserAddress.class);
                            }
                        
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
                        import io.github.raniagus.javalidation.validator.InitializableValidator;
                        import io.github.raniagus.javalidation.validator.Validator;
                        import io.github.raniagus.javalidation.validator.ValidatorsHolder;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class UserRequest$UserAddressValidator implements InitializableValidator<UserRequest.UserAddress> {
                            private Validator<UserRequest> parentValidator;

                            @Override
                            public void initialize(ValidatorsHolder holder) {
                                parentValidator = holder.getValidator(UserRequest.class);
                            }

                            @Override
                            public void validate(Validation validation, UserRequest.UserAddress root) {
                                validation.withField("parent", () -> {
                                    var parent = root.parent();
                                    if (parent == null) return;
                                    parentValidator.validate(validation, parent);
                                });
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
                            private static final ValidatorsHolder HOLDER;
                        
                            private Validators() {}
                        
                            static {
                                HOLDER = new ValidatorsHolder(Map.ofEntries(
                                        Map.entry(UserRequest.UserAddress.class, new UserRequest$UserAddressValidator())
                                      , Map.entry(UserRequest.class, new UserRequestValidator())
                                ));
                                HOLDER.initialize();
                            }
                        
                            public static boolean hasValidator(Class<?> clazz) {
                                return HOLDER.hasValidator(clazz);
                            }
                        
                            public static <T> ValidationErrors validate(T instance) {
                                 return HOLDER.validate(instance);
                            }
                        
                            public static <T> Validator<T> getValidator(Class<T> clazz) {
                                 return HOLDER.getValidator(clazz);
                            }
                        }
                        """));
    }

    @Test
    void shouldGenerateValidatorForSealedInterface() {
        JavaFileObject sealedFile = JavaFileObjects.forSourceString("test.Shape", """
                package test;
    
                public sealed interface Shape permits Circle, Rectangle {}
                """
        );

        JavaFileObject circleFile = JavaFileObjects.forSourceString("test.Circle", """
                package test;
            
                import jakarta.validation.Valid;
            
                public record Circle(double radius, @Valid Center center) implements Shape {}
                """
        );

        JavaFileObject centerFile = JavaFileObjects.forSourceString("test.Center", """
                package test;
            
                public record Center(double x, double y) {}
                """
        );

        JavaFileObject rectangleFile = JavaFileObjects.forSourceString("test.Rectangle", """
                package test;
    
                public record Rectangle(double width, double height) implements Shape {}
                """
        );

        JavaFileObject triggerFile = JavaFileObjects.forSourceString("test.ShapeService", """
                package test;
    
                import jakarta.validation.Valid;
    
                public class ShapeService {
                    public void create(@Valid Shape input) {}
                }
                """
        );

        Compilation compilation = javac()
                .withProcessors(new ValidatorProcessor())
                .compile(sealedFile, circleFile, centerFile, rectangleFile, triggerFile);

        assertThat(compilation).succeeded();

        assertThat(compilation)
                .generatedSourceFile("test.CircleValidator")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("test.CircleValidator", """
                        package test;
    
                        import io.github.raniagus.javalidation.Validation;
                        import io.github.raniagus.javalidation.validator.InitializableValidator;
                        import io.github.raniagus.javalidation.validator.Validator;
                        import io.github.raniagus.javalidation.validator.ValidatorsHolder;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;
    
                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class CircleValidator implements InitializableValidator<Circle> {
                            private Validator<Center> centerValidator;
            
                            @Override
                            public void initialize(ValidatorsHolder holder) {
                                centerValidator = holder.getValidator(Center.class);
                            }
            
                            @Override
                            public void validate(Validation validation, Circle root) {
                                validation.withField("center", () -> {
                                    var center = root.center();
                                    if (center == null) return;
                                    centerValidator.validate(validation, center);
                                });
                            }
                        }
                        """
                ));

        assertThat(compilation)
                .generatedSourceFile("test.CenterValidator")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("test.CenterValidator", """
                        package test;

                        import io.github.raniagus.javalidation.Validation;
                        import io.github.raniagus.javalidation.validator.InitializableValidator;
                        import io.github.raniagus.javalidation.validator.ValidatorsHolder;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;
            
                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class CenterValidator implements InitializableValidator<Center> {

                            @Override
                            public void initialize(ValidatorsHolder holder) {
                            }

                            @Override
                            public void validate(Validation validation, Center root) {
            
                            }
                        }
                        """
                ));

        assertThat(compilation)
                .generatedSourceFile("test.RectangleValidator")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("test.RectangleValidator", """
                        package test;
    
                        import io.github.raniagus.javalidation.Validation;
                        import io.github.raniagus.javalidation.validator.InitializableValidator;
                        import io.github.raniagus.javalidation.validator.ValidatorsHolder;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;
    
                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class RectangleValidator implements InitializableValidator<Rectangle> {
        
                            @Override
                            public void initialize(ValidatorsHolder holder) {
                            }

                            @Override
                            public void validate(Validation validation, Rectangle root) {
                            }
                        }
                        """
                ));

        assertThat(compilation)
                .generatedSourceFile("test.ShapeValidator")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("test.ShapeValidator", """
                    package test;

                    import io.github.raniagus.javalidation.Validation;
                    import io.github.raniagus.javalidation.validator.InitializableValidator;
                    import io.github.raniagus.javalidation.validator.Validator;
                    import io.github.raniagus.javalidation.validator.ValidatorsHolder;
                    import javax.annotation.processing.Generated;
                    import org.jspecify.annotations.NullMarked;

                    @NullMarked
                    @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                    public class ShapeValidator implements InitializableValidator<Shape> {
                        private Validator<Circle> circleValidator;
                        private Validator<Rectangle> rectangleValidator;

                        @Override
                        public void initialize(ValidatorsHolder holder) {
                            circleValidator = holder.getValidator(Circle.class);
                            rectangleValidator = holder.getValidator(Rectangle.class);
                        }

                        @Override
                        public void validate(Validation validation, Shape root) {
                            switch (root) {
                                case Circle circle -> circleValidator.validate(validation, circle);
                                case Rectangle rectangle -> rectangleValidator.validate(validation, rectangle);
                            }
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
                    import test.Center;
                    import test.CenterValidator;
                    import test.Circle;
                    import test.CircleValidator;
                    import test.Rectangle;
                    import test.RectangleValidator;
                    import test.Shape;
                    import test.ShapeValidator;

                    @NullMarked
                    @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                    public final class Validators {
                        private static final ValidatorsHolder HOLDER;

                        private Validators() {}

                        static {
                            HOLDER = new ValidatorsHolder(Map.ofEntries(
                                    Map.entry(Center.class, new CenterValidator())
                                  , Map.entry(Circle.class, new CircleValidator())
                                  , Map.entry(Rectangle.class, new RectangleValidator())
                                  , Map.entry(Shape.class, new ShapeValidator())
                            ));
                            HOLDER.initialize();
                        }

                        public static boolean hasValidator(Class<?> clazz) {
                            return HOLDER.hasValidator(clazz);
                        }

                        public static <T> ValidationErrors validate(T instance) {
                             return HOLDER.validate(instance);
                        }

                        public static <T> Validator<T> getValidator(Class<T> clazz) {
                             return HOLDER.getValidator(clazz);
                        }
                    }
                    """
                ));
    }
}
