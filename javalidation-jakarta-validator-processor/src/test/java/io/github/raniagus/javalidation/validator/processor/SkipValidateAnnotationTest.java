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

                public record SkippedRecord(@NotNull Name name) {
                    public record Name(@NotNull String value) {}
                }
                """
        );

        JavaFileObject triggerFile = JavaFileObjects.forSourceString("test.SimpleService", """
                package test;
    
                import jakarta.validation.*;
    
                public class SimpleService {
                    public void doSomething(@Valid SkippedRecord input) {}
                }
                """
        );

        Compilation compilation = javac()
                .withProcessors(new ValidatorProcessor())
                .compile(sourceFile, triggerFile);

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
                                validation.withField("name", () -> {
                                    var name = root.name();
                                    if (name == null) {
                                        validation.addError("must not be null");
                                        return;
                                    }
                                });
                            }
                        }
                        """
                ));
    }
}
