package io.github.raniagus.javalidation.validator.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;

public class IterableValidationsTest {

    @Test
    void shouldGenerateValidatorForPrimitiveIterable() {
        // Arrange - create source files in memory
        JavaFileObject sourceFile = JavaFileObjects.forSourceString("test.UserRequest", """
                package test;
                
                import io.github.raniagus.javalidation.validator.*;
                import jakarta.validation.constraints.*;
                import java.util.List;
                
                @Validate
                public record UserRequest(
                    @NotNull List<@Size(min = 3, max = 10) String> tags
                ) {}
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
                        import org.jspecify.annotations.Nullable;

                        @Generated("io.github.raniagus.javalidation.processor.ValidatorProcessor")
                        public class UserRequestValidator implements Validator<UserRequest> {

                            @Override
                            public ValidationErrors validate(@Nullable UserRequest root) {
                                Validation rootValidation = Validation.create();

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

                                return rootValidation.finish();
                            }
                        }
                        """
                ));
    }

    @Test
    void shouldGenerateValidatorForValidatedIterable() {
        // Arrange - create source files in memory
        JavaFileObject sourceFile = JavaFileObjects.forSourceString("test.UserRequest", """
                package test;
                
                import io.github.raniagus.javalidation.validator.*;
                import jakarta.validation.constraints.*;
                import java.util.List;
                
                @Validate
                public record UserRequest(
                    @NotNull List<@NotNull Person> friends
                ) {
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
                        import org.jspecify.annotations.Nullable;
                        import test.UserRequest;
                        import test.UserRequest$PersonValidator;

                        @Generated("io.github.raniagus.javalidation.processor.ValidatorProcessor")
                        public class UserRequestValidator implements Validator<UserRequest> {
                            private final Validator<UserRequest.Person> friendsItemValidator = new UserRequest$PersonValidator();

                            @Override
                            public ValidationErrors validate(@Nullable UserRequest root) {
                                Validation rootValidation = Validation.create();
                    
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
    }


    @Test
    void shouldGenerateValidatorForNestedIterable() {
        // Arrange - create source files in memory
        JavaFileObject sourceFile = JavaFileObjects.forSourceString("test.UserRequest", """
                package test;
                
                import io.github.raniagus.javalidation.validator.*;
                import jakarta.validation.constraints.*;
                import java.util.List;
                
                @Validate
                public record UserRequest(
                    List<@NotEmpty List<@NotNull Integer>> scores
                ) {}
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
                        import org.jspecify.annotations.Nullable;

                        @Generated("io.github.raniagus.javalidation.processor.ValidatorProcessor")
                        public class UserRequestValidator implements Validator<UserRequest> {
                            @Override
                            public ValidationErrors validate(@Nullable UserRequest root) {
                                Validation rootValidation = Validation.create();

                                var scores = root.scores();
                                var scoresValidation = Validation.create();
                                if (scores != null) {
                                    int scoresIndex = 0;
                                    for (var scoresItem : scores) {
                                        var scoresItemValidation = Validation.create();
                                        if (scoresItem == null || scoresItem.isEmpty()) {
                                            scoresItemValidation.addRootError("must not be empty");
                                        }
                                        if (scoresItem != null) {
                                            int scoresItemIndex = 0;
                                            for (var scoresItemItem : scoresItem) {
                                                var scoresItemItemValidation = Validation.create();
                                                if (scoresItemItem == null) {
                                                    scoresItemItemValidation.addRootError("must not be null");
                                                }
                                                scoresItemValidation.addAll(scoresItemItemValidation.finish(), new Object[]{scoresItemIndex++});
                                            }
            
                                        }
                                        scoresValidation.addAll(scoresItemValidation.finish(), new Object[]{scoresIndex++});
                                    }
            
                                }
                                rootValidation.addAll(scoresValidation.finish(), new Object[]{"scores"});

                                return rootValidation.finish();
                            }
                        }
                        """
                ));
    }
}
