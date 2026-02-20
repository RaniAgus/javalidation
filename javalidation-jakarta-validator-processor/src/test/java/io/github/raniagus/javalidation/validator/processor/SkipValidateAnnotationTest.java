package io.github.raniagus.javalidation.validator.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

class SkipValidateAnnotationTest {

    @Test
    void shouldSkipValidationOnField() {
        JavaFileObject sourceFile = JavaFileObjects.forSourceString("test.SkippedRecord", """
                package test;

                import io.github.raniagus.javalidation.validator.*;
                import jakarta.validation.constraints.*;

                @Validate
                public record SkippedRecord(@SkipValidate @NotNull String name) {}
                """
        );

        Compilation compilation = javac()
                .withProcessors(new ValidatorProcessor())
                .compile(sourceFile);

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("test.SkippedRecordValidator")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("test.SkippedRecordValidator", """
                        package test;

                        import io.github.raniagus.javalidation.Validation;
                        import io.github.raniagus.javalidation.validator.Validator;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class SkippedRecordValidator implements Validator<SkippedRecord> {
                            @Override
                            public void validate(Validation validation, SkippedRecord root) {

                            }
                        }
                        """
                ));
    }

    @Test
    void shouldSkipValidationOnTypeArgument() {
        JavaFileObject sourceFile = JavaFileObjects.forSourceString("test.SkippedIterableRecord", """
                package test;

                import io.github.raniagus.javalidation.validator.*;
                import jakarta.validation.constraints.*;
                import java.util.List;

                @Validate
                public record SkippedIterableRecord(List<@SkipValidate @NotNull String> items) {}
                """
        );

        Compilation compilation = javac()
                .withProcessors(new ValidatorProcessor())
                .compile(sourceFile);

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("test.SkippedIterableRecordValidator")
                .hasSourceEquivalentTo(JavaFileObjects.forSourceString("test.SkippedIterableRecordValidator", """
                        package test;

                        import io.github.raniagus.javalidation.Validation;
                        import io.github.raniagus.javalidation.validator.Validator;
                        import javax.annotation.processing.Generated;
                        import org.jspecify.annotations.NullMarked;

                        @NullMarked
                        @Generated("io.github.raniagus.javalidation.validator.processor.ValidatorProcessor")
                        public class SkippedIterableRecordValidator implements Validator<SkippedIterableRecord> {
                            @Override
                            public void validate(Validation validation, SkippedIterableRecord root) {

                            }
                        }
                        """
                ));
    }

    @Test
    void withoutSkipValidate_constraintsAreApplied() {
        JavaFileObject sourceFile = JavaFileObjects.forSourceString("test.NotSkippedRecord", """
                package test;

                import io.github.raniagus.javalidation.validator.*;
                import jakarta.validation.constraints.*;

                @Validate
                public record NotSkippedRecord(@NotNull String name) {}
                """
        );

        Compilation compilation = javac()
                .withProcessors(new ValidatorProcessor())
                .compile(sourceFile);

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("test.NotSkippedRecordValidator")
                .contentsAsUtf8String()
                .contains("must not be null");
    }
}
